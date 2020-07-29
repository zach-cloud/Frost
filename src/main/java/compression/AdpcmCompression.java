package compression;

import interfaces.IGenericCompression;
import reader.BinaryReader;

import java.io.IOException;

/**
 * Based off source: https://chromium.googlesource.com/chromiumos/third_party/alsa-lib/+/808ce5deb3550e522a0c3087f1c851bdf87d3464/src/pcm/pcm_adpcm.c
 * And: https://github.com/inwc3/JMPQ3/blob/master/src/main/java/systems/crigges/jmpq3/compression/ADPCM.java
 */
public class AdpcmCompression implements IGenericCompression {

    private int channel;
    private static final int MAX_CHANNELS = 2;
    /* First table lookup for Ima-ADPCM quantizer */
    private static final int[] INDEX_ADJUST = {-1, -1, -1, -1, 2, 4, 6, 8};
    /* Second table lookup for Ima-ADPCM quantizer */
    private static final int[] STEP_SIZE = {
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

    /**
     * Creates a new Adpcm compression
     *
     * @param channel  What channel to use (1 = MONO, 2 = STEREO)
     */
    public AdpcmCompression(int channel) {
        if(channel > MAX_CHANNELS) {
            throw new IllegalArgumentException("Invalid channel: " + channel);
        }
        this.channel = channel;
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

            // Shift right by 8 bits (divides by 256)
            int shift = reader.readShort() >>> 8;

            return new byte[0];
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
