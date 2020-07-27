package encryption;

import exception.EncryptionException;
import exception.HashingException;
import interfaces.IStormCrypt;
import model.BlockTableEntry;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static encryption.StormConstants.MPQ_HASH_FILE_KEY;
import static encryption.StormConstants.MPQ_HASH_TABLE_OFFSET;
import static helper.ByteHelper.extractBytes;

/**
 * MPQ StormSecurity
 * Based on provided C++ code.
 *
 * Note that these encryption/hashing algorithms are
 * extremely insecure and should NEVER be used for any
 * sort of security use case.
 *
 * This is intended to ONLY be used to interface with the
 * MPQ file format which uses these algorithms internally.
 */
public class StormSecurity implements IStormCrypt {

    private enum OperationType {
        ENCRYPT,
        DECRYPT
    }

    /* Defaults to little endian */
    private ByteOrder byteOrder;

    private int ENCRYPTION_TABLE_SIZE = 0x500;
    private long SEED_INITIAL_VALUE = 0x00100001;
    private int INITIAL_ENCRYPT_SEED = 0xEEEEEEEE;

    /* StormSecurity table that is set on class startup. */
    private long[] encryptionTable;

    /**
     * Creates a new StormSecurity with default parameters
     * and little endian byteorder.
     */
    public StormSecurity() {
        this(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Creates a new StormEncryption with default parameters
     * and specified byte order
     *
     * @param byteOrder Byte order to use (little or big)
     */
    public StormSecurity(ByteOrder byteOrder) {
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

    /**
     * Hashes the String using Storm algorithm and returns the key
     * as an integer (rather than long)
     *
     * @param s         String to hash
     * @param hashType  Hash type (see constants of this class; 0-3)
     * @return          Hash value as int
     */
    public int hashAsInt(String s, int hashType) {
        return (int)(hashString(s, hashType));
    }

    /**
     * Hashes the String using Storm algorithm and returns the key
     * as an long
     *
     * @param s         String to hash
     * @param hashType  Hash type (see constants of this class; 0-3)
     * @return          Hash value as long
     */
    public long hashString(String s, int hashType) {
        if(s == null) {
            throw new HashingException("Cannot hash a null String");
        }
        if(hashType > MPQ_HASH_FILE_KEY || hashType < MPQ_HASH_TABLE_OFFSET) {
            throw new HashingException("Invalid hash type: " + hashType);
        }
        long seed1 = 0x7FED7FEDL;
        long seed2 = 0xEEEEEEEEL;
        int ch;

        s = s.toUpperCase();
        for(char c : s.toCharArray()) {
            ch = c;
            seed1 = encryptionTable[(hashType * 0x100) + ch] ^ (seed1 + seed2);
            seed2 = ch + seed1 + seed2 + (seed2 << 5) + 3;
        }
        return seed1;
    }

    /**
     * Hashes the String using Storm algorithm and returns the key
     * as an integer (rather than long)
     *
     * @param filePath  Filename to hash
     * @param entry     Block table entry corresponding to file
     * @return File key as long
     */
    public long computeFileKey(String filePath, BlockTableEntry entry) {
        if(filePath == null) {
            throw new HashingException("Cannot hash a null file path");
        }
        filePath = filePath.replace("\\", "");
        long fileKey = hashString(filePath, MPQ_HASH_FILE_KEY);
        if(entry.isKeyAdjusted()) {
            fileKey = (fileKey + entry.getBlockOffset()) ^ entry.getFileSize();
        }
        return fileKey;
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
     * Encrypts the specified integer array using the Storm encryption algorithm
     * If input array is null, returns null
     *
     * @param src   Integer source array
     * @param key   Key to encrypt with
     * @return      Encrypted integer array
     */
    @Override
    public int[] encrypt(int[] src, int key) {
        if(src == null) {
            return null;
        }
        int len = src.length;
        int[] encryptedArray = new int[len];
        int seed = INITIAL_ENCRYPT_SEED;
        for(int i = 0; i < len; i++) {
            seed += encryptionTable[0x400 + (key & 0xFF)];
            int base = key + seed;
            int current = src[i];
            int res = current ^ base;
            key = (~key << 21) + 0x11111111 | key >>> 11;
            seed = (current + seed + (seed << 5) + 3);
            encryptedArray[i] = res;
        }
        return encryptedArray;
    }

    /**
     * Encrypts a single integer.
     *
     * @param src   Source integer to encrypt
     * @param key   Key to encrypt with
     * @return      Encrypted integer
     */
    public int encrypt(int src, int key) {
        int[] srcArray = new int[1];
        srcArray[0] = src;
        return encrypt(srcArray,key)[0];
    }

    /**
     * Decrypts a single integer.
     *
     * @param src   Source integer to decrypt
     * @param key   Key to decrypt with
     * @return      Decrypted integer
     */
    public int decrypt(int src, int key) {
        int[] srcArray = new int[1];
        srcArray[0] = src;
        return decrypt(srcArray,key)[0];
    }

    /**
     * Decrypts integer array using Storm crypto algorithm.
     * If input array is null, returns null
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
     * Generically encrypts/decrypts bytes.
     *
     * @param src   Source bytes array
     * @param key   Key to decrypt with
     * @param operationType Encrypt or decrypt
     * @return      Modified bytes array
     */
    private byte[] modifyBytes(byte[] src, int key, OperationType operationType) {
        // Perform some checks...
        if(src == null) {
            return null;
        }
        if(src.length % 4 != 0) {
            // TODO: Don't throw exception.. just add on the final bytes to the end of it.
            throw new EncryptionException("Length of input bytes " +
                    "was not divisible by 4 (was " + src.length + ")");
        }
        // First, convert the byte array into an integer array that can
        // be used for the interface of Storm crypto
        int[] convertedNumericValues = transform(src);
        int[] modified = null;
        if(operationType == OperationType.ENCRYPT) {
            modified = encrypt(convertedNumericValues, key);
        } else if(operationType == OperationType.DECRYPT) {
            modified = decrypt(convertedNumericValues, key);
        } else {
            throw new EncryptionException("Unknown encryption type: " + operationType.name());
        }
        // Cast back into byte array
        return intArrayToBytes(modified);
    }

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
    public byte[] decryptBytes(byte[] src, int key) {
        return modifyBytes(src, key, OperationType.DECRYPT);
    }

    /**
     * Encrypts the specified bytes array.
     * Uses the Storm algorithm
     * Byte array must be divisible by 4 and contain
     * integers in each four position (0-3, 4-7, etc)
     * If a null array is provided, returns a null array.
     *
     * @param src   Source bytes array
     * @param key   Key to encrypt with
     * @return      Encrypt bytes array
     */
    @Override
    public byte[] encryptBytes(byte[] src, int key) {
        return modifyBytes(src, key, OperationType.ENCRYPT);
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

    /**
     * Encrypts the specified byte buffer
     * Uses the Storm algorithm
     * Byte buffer size must be divisible by 4 and
     * contain integers in each four position (0-3, 4-6, etc)
     * If a null buffer is provided, returns a null buffer
     *
     * @param src Source buffer
     * @param key Key to encrypt with
     * @return Encrypted buffer
     */
    @Override
    public ByteBuffer encryptBuffer(ByteBuffer src, int key) {
        return ByteBuffer.wrap(encryptBytes(src.array(), key)).order(byteOrder);
    }

    public static void main(String[] args) {
        int test = 518313807;
        System.out.println(new StormSecurity().decrypt(test, 758105132));

    }
}
