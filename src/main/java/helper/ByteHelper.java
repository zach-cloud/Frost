package helper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Helper functions for byte operations.
 */
public class ByteHelper {

    /**
     * Extracts a number of bytes from the byte array.
     *
     * @param src   Source byte array
     * @param start Start position to extract bytes
     * @param len   Length of byte array to extract
     * @return      Extracted byte array of length len
     */
    public static byte[] extractBytes(byte[] src, int start, int len) {
        if(start + len > src.length) {
            throw new IllegalArgumentException("Start + len is out of range " +
                    "on source array (length = " + src.length + ")");
        }
        byte[] extracted = new byte[len];
        int pos = 0;
        for(int i = start; i < start + len; i++) {
            extracted[pos] = src[i];
            pos++;
        }
        return extracted;
    }

    /**
     * Converts a 4-byte array into an int32
     *
     * @param src       Source array
     * @param byteOrder Byte order
     * @return          Integer result
     */
    public static int byteToInt(byte[] src, ByteOrder byteOrder) {
        if(src.length != 4) {
            throw new IllegalArgumentException("Attempted to convert size " +
                    src.length + " to int (invalid size)");
        }
        return ByteBuffer.wrap(src).order(byteOrder).getInt();
    }

    /**
     * Converts a 4-byte array into an int32
     * Little endian order
     *
     * @param src       Source array
     * @return          Integer result
     */
    public static int byteToInt(byte[] src) {
        return byteToInt(src, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Converts a 2-byte array into an int16
     *
     * @param src       Source array
     * @param byteOrder Byte order
     * @return          Short result
     */
    public static short byteToShort(byte[] src, ByteOrder byteOrder) {
        if(src.length != 2) {
            throw new IllegalArgumentException("Attempted to convert size " +
                    src.length + " to short (invalid size)");
        }
        return ByteBuffer.wrap(src).order(byteOrder).getShort();
    }

    /**
     * Converts a 4-byte array into an int32
     * Little endian order
     *
     * @param src       Source array
     * @return          Integer result
     */
    public static short byteToShort(byte[] src) {
        return byteToShort(src, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Trims bytes off the beginning of the data.
     *
     * @param rawData   Original bytes
     * @param offset    Offset to trim from
     * @return          Shorter byte array (length - offset)
     */
    public static byte[] trimBytes(byte[] rawData, int offset) {
        byte[] newBytes = new byte[rawData.length - offset];
        for(int i = offset; i < rawData.length; i++) {
            newBytes[i - offset] = rawData[i];
        }
        return newBytes;
    }

    /**
     * Combines two byte arrays together
     *
     * @param originalBytes Byte array (part 1)
     * @param toAppend      Byte array (part 2 to add)
     * @return              Result array, part 1 + part 2
     */
    public static byte[] combineBytes(byte[] originalBytes, byte[] toAppend) {
        int totalLength = originalBytes.length + toAppend.length;
        byte[] newBytes = new byte[totalLength];
        for(int i = 0; i < originalBytes.length; i++) {
            newBytes[i] = originalBytes[i];
        }
        for(int i = 0; i < toAppend.length; i++) {
            newBytes[i + originalBytes.length] = toAppend[i];
        }
        return newBytes;
    }

    public static String bytesToString(byte[] byteArray) {
        StringBuilder builder = new StringBuilder();
        for(byte b: byteArray) {
            builder.append(b).append(",");
        }
        if(builder.length() > 0) {
            builder.setLength(builder.length() - 1);
        }
        return builder.toString();
    }
}
