package interfaces;

import java.nio.ByteBuffer;

public interface IStormCrypt {

    /**
     * Decrypts integer array using Storm crypto algorithm.
     * If input array is null, returns null;
     *
     * @param src   Integer source array
     * @param key   Key to encrypt with
     * @return      Decrypted integer array
     */
    int[] decrypt(int[] src, int key);

    /**
     * Decrypts the specified bytes array.
     * Uses the Storm algorithm
     * Byte array must be divisible by 4 and contain
     * integers in each four position (0-3, 4-7, etc)
     * If a null array is provided, returns a null array.
     *
     * @param src   Source bytes array
     * @param key   Key to decrypt with
     * @return      Decrypted bytes array
     */
    byte[] decryptBytes(byte[] src, int key);

    /**
     * Decrypts the specified byte buffer
     * Uses the Storm algorithm
     * Byte buffer size must be divisible by 4 and
     * contain integers in each four position (0-3, 4-6, etc)
     * If a null buffer is provided, returns a null buffer
     *
     * @param src   Source buffer
     * @param key   Key to decrypt with
     * @return      Decrypted buffer
     */
    ByteBuffer decryptBuffer(ByteBuffer src, int key);
}
