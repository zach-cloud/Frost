package model;


import encryption.StormCrypt;

import java.util.ArrayList;
import java.util.List;

import static helper.ByteHelper.*;

public class BlockTable {

    private static final int BLOCK_TABLE_KEY = -326913117;
    private static final int BLOCK_TABLE_ENTRY_SIZE = 16; // 16 bytes = 4 * int32
    private List<BlockTableEntry> entries;

    public BlockTable(StormCrypt stormCrypt, EncryptedBlockTable encryptedBlockTable) {
        entries = new ArrayList<>();
        byte[] encryptedData = encryptedBlockTable.getEncryptedData();
        byte[] decryptedData = stormCrypt.decryptBytes(encryptedData, BLOCK_TABLE_KEY);
        if(decryptedData.length % 16 != 0) {
            throw new IllegalArgumentException("Could not convert decrypted bytes " +
                    "into table entries (size = " + decryptedData.length + ")");
        }

        for(int i = 0 ; i < decryptedData.length / 16; i++) {
            byte[] blockOffset = extractBytes(decryptedData, i * 16, 4);
            byte[] blockSize = extractBytes(decryptedData, 4 + (i * 16), 4);
            byte[] fileSize = extractBytes(decryptedData, 8 + (i * 16), 4);
            byte[] flags = extractBytes(decryptedData, 12 + (i * 16), 4);
            BlockTableEntry entry = new BlockTableEntry(byteToInt(blockOffset),
                    byteToInt(blockSize), byteToInt(fileSize), byteToInt(flags));
            entries.add(entry);
        }
        System.out.println("Decrypted data");
    }
}
