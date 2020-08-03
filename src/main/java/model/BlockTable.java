package model;


import interfaces.IByteSerializable;
import storm.StormSecurity;
import settings.MpqContext;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static storm.StormConstants.BLOCK_TABLE_ENCRYPTION_KEY;
import static helper.ByteHelper.*;
import static storm.StormConstants.BYTES_PER_BLOCK_TABLE_ENTRY;

public class BlockTable implements IByteSerializable {
    private List<BlockTableEntry> entries;
    private StormSecurity security;
    private MpqContext context;

    /**
     * Decrypts provided block table and parses the entries.
     *
     * @param stormSecurity            Encryption module with little endian order
     * @param encryptedBlockTable   Encrypted block table (read from file)
     */
    public BlockTable(StormSecurity stormSecurity, EncryptedBlockTable encryptedBlockTable, MpqContext context) {
        this.context = context;
        this.security = stormSecurity;
        entries = new ArrayList<>();
        byte[] encryptedData = encryptedBlockTable.getEncryptedData();
        context.getLogger().debug("Attempting to decrypt block table... key=" + BLOCK_TABLE_ENCRYPTION_KEY);
        byte[] decryptedData = stormSecurity.decryptBytes(encryptedData, BLOCK_TABLE_ENCRYPTION_KEY);
        context.getLogger().debug("Decrypted bytes into: " + decryptedData.length);
        if(decryptedData.length % BYTES_PER_BLOCK_TABLE_ENTRY != 0) {
            context.getErrorHandler().handleCriticalError("Could not convert decrypted bytes " +
                    "into table entries (size = " + decryptedData.length + ")");
        }
        for(int i = 0 ; i < decryptedData.length / BYTES_PER_BLOCK_TABLE_ENTRY; i++) {
            byte[] blockOffset = extractBytes(decryptedData, i * BYTES_PER_BLOCK_TABLE_ENTRY, 4);
            byte[] blockSize = extractBytes(decryptedData, 4 + (i * BYTES_PER_BLOCK_TABLE_ENTRY), 4);
            byte[] fileSize = extractBytes(decryptedData, 8 + (i * BYTES_PER_BLOCK_TABLE_ENTRY), 4);
            byte[] flags = extractBytes(decryptedData, 12 + (i * BYTES_PER_BLOCK_TABLE_ENTRY), 4);
            BlockTableEntry entry = new BlockTableEntry(byteToInt(blockOffset),
                    byteToInt(blockSize), byteToInt(fileSize), byteToInt(flags), context);
            entry.setCallbackId(i);
            entries.add(entry);
        }
        context.getLogger().info("Block table has " + entries.size() + " entries");
    }
    /**
     * Converts this object into a byte array which represents
     * the same state as the object.
     *
     * @return  Byte array of object.
     */
    @Override
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(BYTES_PER_BLOCK_TABLE_ENTRY * entries.size());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for(BlockTableEntry entry : entries) {
            buffer.put(entry.toBytes());
        }
        EncryptedBlockTable encryptedBlockTable = new EncryptedBlockTable(entries.size(), context);
        encryptedBlockTable.encrypt(buffer.array(),security);
        return encryptedBlockTable.toBytes();
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

    public MpqContext getContext() {
        return context;
    }

    public void setContext(MpqContext context) {
        this.context = context;
    }
}
