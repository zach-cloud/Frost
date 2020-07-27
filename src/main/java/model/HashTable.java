package model;

import encryption.StormSecurity;
import settings.MpqContext;

import java.util.ArrayList;
import java.util.List;

import static encryption.StormConstants.HASH_TABLE_ENCRYPTION_KEY;
import static helper.ByteHelper.*;

public class HashTable {

    private static final int HASH_TABLE_ENTRY_SIZE = 16; // 16 bytes = 4 * int32
    private List<HashTableEntry> entries;

    private MpqContext context;

    /**
     * Decrypts provided block table and parses the entries.
     *
     * @param stormSecurity            Encryption module with little endian order
     * @param encryptedHashTable    Encrypted hash table (read from file)
     */
    public HashTable(StormSecurity stormSecurity, EncryptedHashTable encryptedHashTable, MpqContext context) {
        entries = new ArrayList<>();
        this.context = context;
        byte[] encryptedData = encryptedHashTable.getEncryptedData();
        context.getLogger().debug("Attempting to decrypt hash table... key=" + HASH_TABLE_ENCRYPTION_KEY);
        byte[] decryptedData = stormSecurity.decryptBytes(encryptedData, HASH_TABLE_ENCRYPTION_KEY);
        context.getLogger().debug("Decrypted bytes into: " + decryptedData.length);
        if(decryptedData.length % HASH_TABLE_ENTRY_SIZE != 0) {
            context.getErrorHandler().handleCriticalError("Could not convert decrypted bytes " +
                    "into table entries (size = " + decryptedData.length + ")");
        }

        for(int i = 0 ; i < decryptedData.length / HASH_TABLE_ENTRY_SIZE; i++) {
            byte[] filePathHashA = extractBytes(decryptedData, i * HASH_TABLE_ENTRY_SIZE, 4);
            byte[] filePathHashB = extractBytes(decryptedData, 4 + (i * HASH_TABLE_ENTRY_SIZE), 4);
            byte[] language = extractBytes(decryptedData, 8 + (i * HASH_TABLE_ENTRY_SIZE), 2);
            byte[] platform = extractBytes(decryptedData, 10 + (i * HASH_TABLE_ENTRY_SIZE), 2);
            byte[] fileBlockIndex = extractBytes(decryptedData, 12 + (i * HASH_TABLE_ENTRY_SIZE), 4);
            HashTableEntry entry = new HashTableEntry(byteToInt(filePathHashA), byteToInt(filePathHashB),
                    byteToShort(language), byteToShort(platform), byteToInt(fileBlockIndex), context);
            entries.add(entry);
        }
        context.getLogger().info("Hash table has " + entries.size() + " entries");
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
}
