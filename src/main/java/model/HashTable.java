package model;

import frost.FrostSecurity;
import interfaces.IByteSerializable;
import settings.MpqContext;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static frost.FrostConstants.BYTES_PER_HASH_TABLE_ENTRY;
import static frost.FrostConstants.HASH_TABLE_ENCRYPTION_KEY;
import static helper.ByteHelper.*;

public class HashTable implements IByteSerializable {

    private List<HashTableEntry> entries;

    private FrostSecurity security;
    private MpqContext context;

    /**
     * Decrypts provided block table and parses the entries.
     *
     * @param frostSecurity            Encryption module with little endian order
     * @param encryptedHashTable    Encrypted hash table (read from file)
     */
    public HashTable(FrostSecurity frostSecurity, EncryptedHashTable encryptedHashTable, MpqContext context) {
        entries = new ArrayList<>();
        this.context = context;
        this.security = frostSecurity;
        byte[] encryptedData = encryptedHashTable.getEncryptedData();
        context.getLogger().debug("Attempting to decrypt hash table... key=" + HASH_TABLE_ENCRYPTION_KEY);
        byte[] decryptedData = frostSecurity.decryptBytes(encryptedData, HASH_TABLE_ENCRYPTION_KEY);
        context.getLogger().debug("Decrypted bytes into: " + decryptedData.length);
        if(decryptedData.length % BYTES_PER_HASH_TABLE_ENTRY != 0) {
            context.getErrorHandler().handleCriticalError("Could not convert decrypted bytes " +
                    "into table entries (size = " + decryptedData.length + ")");
        }

        for(int i = 0 ; i < decryptedData.length / BYTES_PER_HASH_TABLE_ENTRY; i++) {
            byte[] filePathHashA = extractBytes(decryptedData, i * BYTES_PER_HASH_TABLE_ENTRY, 4);
            byte[] filePathHashB = extractBytes(decryptedData, 4 + (i * BYTES_PER_HASH_TABLE_ENTRY), 4);
            byte[] language = extractBytes(decryptedData, 8 + (i * BYTES_PER_HASH_TABLE_ENTRY), 2);
            byte[] platform = extractBytes(decryptedData, 10 + (i * BYTES_PER_HASH_TABLE_ENTRY), 2);
            byte[] fileBlockIndex = extractBytes(decryptedData, 12 + (i * BYTES_PER_HASH_TABLE_ENTRY), 4);
            HashTableEntry entry = new HashTableEntry(byteToInt(filePathHashA), byteToInt(filePathHashB),
                    byteToShort(language), byteToShort(platform), byteToInt(fileBlockIndex), context);
            entry.setCallbackId(i);
            entries.add(entry);
        }
        context.getLogger().info("Hash table has " + entries.size() + " entries");
    }

    /**
     * Converts this object into a byte array which represents
     * the same state as the object.
     *
     * @return  Byte array of object.
     */
    @Override
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(entries.size() * BYTES_PER_HASH_TABLE_ENTRY);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        for(HashTableEntry entry : entries) {
            buffer.put(entry.toBytes());
        }

        EncryptedHashTable encryptedHashTable = new EncryptedHashTable(entries.size(), context);
        encryptedHashTable.encrypt(buffer.array(),security);
        return encryptedHashTable.toBytes();
    }

    public List<HashTableEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<HashTableEntry> entries) {
        this.entries = entries;
    }

    public long size() {
        return entries.size();
    }

    public HashTableEntry get(int initialEntry) {
        return entries.get(initialEntry);
    }

    public MpqContext getContext() {
        return context;
    }

    public void setContext(MpqContext context) {
        this.context = context;
    }
}
