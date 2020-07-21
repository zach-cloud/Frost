package model;


import encryption.StormCrypt;

import java.util.ArrayList;
import java.util.List;

import static encryption.StormConstants.BLOCK_TABLE_ENCRYPTION_KEY;
import static helper.ByteHelper.*;

public class BlockTable {

    private static final int BLOCK_TABLE_ENTRY_SIZE = 16; // 16 bytes = 4 * int32
    private List<BlockTableEntry> entries;

    /**
     * Decrypts provided block table and parses the entries.
     *
     * @param stormCrypt            Encryption module with little endian order
     * @param encryptedBlockTable   Encrypted block table (read from file)
     */
    public BlockTable(StormCrypt stormCrypt, EncryptedBlockTable encryptedBlockTable) {
        entries = new ArrayList<>();
        byte[] encryptedData = encryptedBlockTable.getEncryptedData();
        byte[] decryptedData = stormCrypt.decryptBytes(encryptedData, BLOCK_TABLE_ENCRYPTION_KEY);
        if(decryptedData.length % BLOCK_TABLE_ENTRY_SIZE != 0) {
            throw new IllegalArgumentException("Could not convert decrypted bytes " +
                    "into table entries (size = " + decryptedData.length + ")");
        }

        for(int i = 0 ; i < decryptedData.length / BLOCK_TABLE_ENTRY_SIZE; i++) {
            byte[] blockOffset = extractBytes(decryptedData, i * BLOCK_TABLE_ENTRY_SIZE, 4);
            byte[] blockSize = extractBytes(decryptedData, 4 + (i * BLOCK_TABLE_ENTRY_SIZE), 4);
            byte[] fileSize = extractBytes(decryptedData, 8 + (i * BLOCK_TABLE_ENTRY_SIZE), 4);
            byte[] flags = extractBytes(decryptedData, 12 + (i * BLOCK_TABLE_ENTRY_SIZE), 4);
            BlockTableEntry entry = new BlockTableEntry(byteToInt(blockOffset),
                    byteToInt(blockSize), byteToInt(fileSize), byteToInt(flags));
            entries.add(entry);
        }
        System.out.println("Decrypted data");
    }

    public BlockTableEntry get(int index) {
        return entries.get(index);
    }

    public List<BlockTableEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<BlockTableEntry> entries) {
        this.entries = entries;
    }
}
