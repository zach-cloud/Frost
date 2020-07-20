package encryption;

import exception.EncryptionException;
import interfaces.IStormCrypt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static helper.ByteHelper.extractBytes;

/**
 * MPQ StormCrypt
 * Based on provided C++ code.
 */
public class StormCrypt implements IStormCrypt {

    private ByteOrder byteOrder;

    private int ENCRYPTION_TABLE_SIZE = 0x500;
    private long SEED_INITIAL_VALUE = 0x00100001;
    private int INITIAL_ENCRYPT_SEED = 0xEEEEEEEE;

    /* StormCrypt table that is set on class startup. */
    private long[] encryptionTable;

    /**
     * Creates a new StormCrypt with default parameters
     * and little endian byteorder.
     */
    public StormCrypt() {
        this(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Creates a new StormEncryption with default parameters
     * and specified byte order
     *
     * @param byteOrder Byte order to use (little or big)
     */
    public StormCrypt(ByteOrder byteOrder) {
        this.encryptionTable = new long[ENCRYPTION_TABLE_SIZE];
        this.initializeEncryptionTable();
        this.byteOrder = byteOrder;
    }

    /**
     * Saves numbers into the encryption table for future use.
     * See Storm documentation.
     */
    private void initializeEncryptionTable() {
        long seed = SEED_INITIAL_VALUE;
        int index1;
        int index2;
        int i;
        for(index1 = 0; index1 < 0x100; index1++) {
            for (index2 = index1, i = 0; i < 5; i++, index2 += 0x100) {
                long temp1;
                long temp2;

                seed  = (seed * 125 + 3) % 0x2AAAAB;
                temp1 = (seed & 0xFFFF) << 0x10;

                seed  = (seed * 125 + 3) % 0x2AAAAB;
                temp2 = (seed & 0xFFFF);

                encryptionTable[index2] = (temp1 | temp2);
            }
        }
    }


    public long[] encrypt(long[] data, int length, long key) {
       return null;
    }

    /**
     * Transforms a byte array with length divisible by 4 into an array
     * Converts each integer contained in the byte array into an int
     * For example bytes 0-3, 4-7, etc.
     *
     * @param src   Source byte array
     * @return      Converted integer array
     */
    private int[] transform(byte[] src) {
        int convertedLength = src.length / 4;
        int[] converted = new int[convertedLength];
        for(int i = 0; i < convertedLength; i++) {
            byte[] extracted = extractBytes(src, i*4, 4);
            converted[i] = java.nio.ByteBuffer.wrap(extracted).order(byteOrder).getInt();
        }
        return converted;
    }

    /**
     * Decrypts integer array using Storm crypto algorithm.
     * If input array is null, returns null;
     *
     * @param src   Integer source array
     * @param key   Key to encrypt with
     * @return      Decrypted integer array
     */
    public int[] decrypt(int[] src, int key) {
        if(src == null) {
            return null;
        }
        int len = src.length;
        int[] decryptedArray = new int[len];
        int seed = INITIAL_ENCRYPT_SEED;
        for(int i = 0; i < len; i++) {
            seed += encryptionTable[(0x400 + (key & 0xFF))];
            int base = key + seed;
            int currentValue = src[i];
            int decryptResult = currentValue ^ base;
            key = (~key << 21) + 0x11111111 | key >>> 11;
            seed = (decryptResult + seed + (seed << 5) + 3);
            decryptedArray[i] = decryptResult;
        }
        return decryptedArray;
    }

    /**
     * Converts integer array into byte equivalent
     * With the byte array length = ints len * 4
     *
     * @param ar    Origin int array
     * @return      Result bytes array
     */
    private byte[] intArrayToBytes(int[] ar) {
        int len = ar.length * 4; // 32 bit ints
        byte[] data = new byte[len];
        for(int i = 0; i < len; i += 4) {
            byte[] bytes = ByteBuffer.allocate(4).putInt(ar[i/4]).order(byteOrder).array();
            for(int j = 3; j >= 0; j--) {
                data[i + (3 - j)] = bytes[j];
            }
        }
        return data;
    }

    /**
     * Decrypts the specified bytes array.
     * Uses the Storm algorithm
     * Byte array must be divisible by 4 and contain
     * integers in each four position (0-3, 4-7, etc)
     * If a null array is provided, returns a null array.
     *
     * @param src   Source bytes array
     * @param key   Key to encrypt with
     * @return      Decrypted bytes array
     */
    public byte[] decryptBytes(byte[] src, int key) {
        // Perform some checks...
        if(src == null) {
            return null;
        }
        if(src.length % 4 != 0) {
            throw new EncryptionException("Length of input bytes " +
                    "was not divisible by 4 (was " + src.length + ")");
        }
        // First, convert the byte array into an integer array that can
        // be used for the interface of Storm crypto
        int[] convertedNumericValues = transform(src);
        int[] decrypted = decrypt(convertedNumericValues, key);
        // Cast back into byte array
        return intArrayToBytes(decrypted);
    }

    public byte[] encryptBytes(byte[] originData, int key) {
        return null;
    }

    /**
     * Decrypts the specified byte buffer
     * Uses the Storm algorithm
     * Byte buffer size must be divisible by 4 and
     * contain integers in each four position (0-3, 4-6, etc)
     * If a null buffer is provided, returns a null buffer
     *
     * @param src Source buffer
     * @param key Key to decrypt with
     * @return Decrypted buffer
     */
    @Override
    public ByteBuffer decryptBuffer(ByteBuffer src, int key) {
        return ByteBuffer.wrap(decryptBytes(src.array(), key)).order(byteOrder);
    }
}
