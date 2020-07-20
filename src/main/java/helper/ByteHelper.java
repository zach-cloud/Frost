package helper;

import exception.EncryptionException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
}
