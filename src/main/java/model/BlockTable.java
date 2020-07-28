package model;


import storm.StormSecurity;
import settings.MpqContext;

import java.util.ArrayList;
import java.util.List;

import static storm.StormConstants.BLOCK_TABLE_ENCRYPTION_KEY;
import static helper.ByteHelper.*;

public class BlockTable {

    private static final int BLOCK_TABLE_ENTRY_SIZE = 16; // 16 bytes = 4 * int32
    private List<BlockTableEntry> entries;

    private MpqContext context;

    /**
     * Decrypts provided block table and parses the entries.
     *
     * @param stormSecurity            Encryption module with little endian order
     * @param encryptedBlockTable   Encrypted block table (read from file)
     */
    public BlockTable(StormSecurity stormSecurity, EncryptedBlockTable encryptedBlockTable, MpqContext context) {
        this.context = context;
        entries = new ArrayList<>();
        byte[] encryptedData = encryptedBlockTable.getEncryptedData();
        context.getLogger().debug("Attempting to decrypt block table... key=" + BLOCK_TABLE_ENCRYPTION_KEY);
        byte[] decryptedData = stormSecurity.decryptBytes(encryptedData, BLOCK_TABLE_ENCRYPTION_KEY);
        context.getLogger().debug("Decrypted bytes into: " + decryptedData.length);
        if(decryptedData.length % BLOCK_TABLE_ENTRY_SIZE != 0) {
            context.getErrorHandler().handleCriticalError("Could not convert decrypted bytes " +
                    "into table entries (size = " + decryptedData.length + ")");
        }
        for(int i = 0 ; i < decryptedData.length / BLOCK_TABLE_ENTRY_SIZE; i++) {
            byte[] blockOffset = extractBytes(decryptedData, i * BLOCK_TABLE_ENTRY_SIZE, 4);
            byte[] blockSize = extractBytes(decryptedData, 4 + (i * BLOCK_TABLE_ENTRY_SIZE), 4);
            byte[] fileSize = extractBytes(decryptedData, 8 + (i * BLOCK_TABLE_ENTRY_SIZE), 4);
            byte[] flags = extractBytes(decryptedData, 12 + (i * BLOCK_TABLE_ENTRY_SIZE), 4);
            BlockTableEntry entry = new BlockTableEntry(byteToInt(blockOffset),
                    byteToInt(blockSize), byteToInt(fileSize), byteToInt(flags), context);
            entries.add(entry);
        }
        context.getLogger().info("Block table has " + entries.size() + " entries");
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

    public static int getBlockTableEntrySize() {
        return BLOCK_TABLE_ENTRY_SIZE;
    }

    public MpqContext getContext() {
        return context;
    }

    public void setContext(MpqContext context) {
        this.context = context;
    }
}
