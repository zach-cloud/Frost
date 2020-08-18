package compression;

import interfaces.IGenericCompression;
import com.github.zachcloud.reader.BinaryReader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Based off source: https://chromium.googlesource.com/chromiumos/third_party/alsa-lib/+/808ce5deb3550e522a0c3087f1c851bdf87d3464/src/pcm/pcm_adpcm.c
 * And: https://github.com/inwc3/JMPQ3/blob/master/src/main/java/systems/crigges/jmpq3/compression/ADPCM.java
 */
public final class AdpcmCompression implements IGenericCompression {

    private final int totalChannels;

    private static final int WRITE_CURRENT_VALUE_MAGNITUDE = 0;
    private static final int ADVANCE_PERIOD_MAGNITUDE = 1;
    private static final int ADVANCE_CHANNEL_MAGNITUDE = 2;

    private static final int MAX_CHANNELS = 2;
    /* First table lookup for Ima-ADPCM quantizer */
    private static final int[] INDEX_ADJUST = {-1, -1, -1, -1, 2, 4, 6, 8};
    /* Second table lookup for Ima-ADPCM quantizer */
    private static final short[] STEP_SIZE = {
            7, 8, 9, 10, 11, 12, 13, 14, 16, 17,
            19, 21, 23, 25, 28, 31, 34, 37, 41, 45,
            50, 55, 60, 66, 73, 80, 88, 97, 107, 118,
            130, 143, 157, 173, 190, 209, 230, 253, 279, 307,
            337, 371, 408, 449, 494, 544, 598, 658, 724, 796,
            876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066,
            2272, 2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358,
            5894, 6484, 7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899,
            15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767
    };

    private static final int SIGN_FLAG = 0x80;
    private static final int MAGNITUDE_FLAG = 0x7F;
    private static final int DIRECTION_FLAG = 0x40;
    private static final int STEP_CHANGE_FLAG = 0x1F;

    /**
     * Creates a new Adpcm compression
     *
     * @param totalChannels What totalChannels to use (1 = MONO, 2 = STEREO)
     */
    public AdpcmCompression(int totalChannels) {
        if (totalChannels > MAX_CHANNELS) {
            throw new IllegalArgumentException("Invalid totalChannels: " + totalChannels);
        }
        this.totalChannels = totalChannels;
    }

    /**
     * Applies this compression type.
     *
     * @param src Source (uncompressed) data
     * @return Compressed data
     */
    @Override
    public byte[] apply(byte[] src) {
        // we don't care about this...
        return null;
    }

    /**
     * Reverses this compression type.
     *
     * @param src  Source (compressed) data
     * @param dest Array to write compressed data into
     *             Depending on what compression type this is,
     *             the array may need to be instantiated to the
     *             size of the uncompressed data!
     * @return Uncompressed data
     */
    @Override
    public byte[] undo(byte[] src, byte[] dest) {
        try {
            BinaryReader reader = new BinaryReader(src);
            ByteBuffer outputBuffer = ByteBuffer.allocate(dest.length);

            // Shift right by 8 bits (divides by 256)
            // This will later be used to calculate the new step
            // from our current step - this is the second input
            // to the shift right logical, i.e. division by 2^stepShift
            short stepShift = (short) (reader.readShort() >>> 8);

            // Each totalChannels the user wants is initialized
            // The step gets set to 0x2C (initial value) in the constructor
            List<AdpcmChannel> channels = new ArrayList<>();
            for (int i = 0; i < totalChannels; i++) {
                channels.add(new AdpcmChannel());
            }

            // Initialize the state from the input and provide it back
            // to the output
            for (AdpcmChannel chan : channels) {
                // Cast since it returns unsigned 2 byte short as an int
                chan.setState((short) reader.readShort());
                outputBuffer.putShort(chan.getState());
            }

            int channelIndex = 0;
            for (int i = 0; i < src.length; i++) {
                byte current = src[i];
                AdpcmChannel currentchannel = channels.get(channelIndex);

                // Separate the sign and magnitude
                int sign = current & SIGN_FLAG;
                int magnitude = current & MAGNITUDE_FLAG;

                // The sign tells us what operation we should perform.
                if (sign != 0) {
                    if (magnitude == WRITE_CURRENT_VALUE_MAGNITUDE) {
                        if (currentchannel.getStep() != 0) {
                            currentchannel.setStep((short) -1);
                        }
                        // Write current
                        outputBuffer.putShort(currentchannel.getState());
                        channelIndex = advanceChannel(channelIndex);
                    } else if (magnitude == ADVANCE_PERIOD_MAGNITUDE) {
                        advanceStepForward(currentchannel, 8);
                    } else if (magnitude == ADVANCE_CHANNEL_MAGNITUDE) {
                        channelIndex = advanceChannel(channelIndex);
                    } else {
                        advanceStepBackwards(currentchannel, 8);
                    }
                } else {
                    int direction = current & DIRECTION_FLAG;
                    int stepChange = current & STEP_CHANGE_FLAG;

                    short initialStep = STEP_SIZE[currentchannel.getStep()];
                    short adjustedStep = (short) (initialStep >>> stepShift);
                    for (int j = 0; j < 6; j++) {
                        if ((current & 1 << j) != 0) { // Purpose unknown
                            currentchannel.setStep((short) (currentchannel.getStep() >> j));
                        }
                    }
                    int newValue = calculateNewValue(currentchannel, direction, adjustedStep);
                    // Clamp value, don't overflow.
                    currentchannel.setState(clampValue(newValue));
                    outputBuffer.putShort(currentchannel.getState());
                    advanceStep(currentchannel, INDEX_ADJUST[stepChange]);
                    channelIndex = advanceChannel(channelIndex);
                }
            }

            return outputBuffer.array();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private int calculateNewValue(AdpcmChannel currentchannel, int direction, short adjustedStep) {
        int finalValue = currentchannel.getState();
        if (direction == 0) {
            finalValue -= adjustedStep;
        } else {
            finalValue += adjustedStep;
        }
        return finalValue;
    }

    private short clampValue(int finalValue) {
        if (finalValue > Short.MAX_VALUE) {
            finalValue = Short.MAX_VALUE;
        } else if (finalValue < Short.MIN_VALUE) {
            finalValue = Short.MIN_VALUE;
        }
        return (short) finalValue;
    }

    private int advanceChannel(int currentChannelIndex) {
        return (currentChannelIndex + 1) % totalChannels; // Keep the current channel in bounds
    }

    private void advanceStepForward(AdpcmChannel channel, int amount) {
        channel.setStep((short) (channel.getStep() + amount));
        // If we reach or pass the final step size, we will stay at the final one.
        if (channel.getStep() > STEP_SIZE.length) {
            channel.setStep((short) (STEP_SIZE.length - 1));
        }
    }

    private void advanceStepBackwards(AdpcmChannel channel, int amount) {
        channel.setStep((short) (channel.getStep() - amount));
        // If we go below zero, go back to the first step
        if (channel.getStep() < 0) {
            channel.setStep((short) 0);
        }
    }

    private void advanceStep(AdpcmChannel channel, int amount) {
        if (amount > 0) {
            advanceStepForward(channel, amount);
        } else if (amount < 0) {
            advanceStepBackwards(channel, -1 * amount);
        }
    }
}
