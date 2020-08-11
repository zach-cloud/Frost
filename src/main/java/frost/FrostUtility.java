package frost;

import model.HashTable;
import model.HashTableEntry;
import settings.MpqContext;

import java.util.ArrayList;
import java.util.List;

import static frost.FrostConstants.*;

public final class FrostUtility {

    private FrostSecurity frostSecurity;
    private MpqContext context;

    public FrostUtility(FrostSecurity frostSecurity, MpqContext context) {
        this.frostSecurity = frostSecurity;
        this.context = context;
    }

    /**
     * Finds a hash table entry for file name, or null if no such entry exists.
     *
     * @param hashTable Hash table to look through
     * @param fileName  File name to look for
     * @param lang      Language to look for (or ANY_LANGUAGE for any)
     * @param platform  Platform to look for (or ANY_PLATFORM for any)
     * @return Hash table entry, or null if not exists.
     */
    public HashTableEntry findEntry(HashTable hashTable, String fileName, short lang, short platform) {
        try {
            // Calculate hashes and find entry
            int hashA = frostSecurity.hashAsInt(fileName, MPQ_HASH_NAME_A);
            int hashB = frostSecurity.hashAsInt(fileName, MPQ_HASH_NAME_B);
            for(HashTableEntry currentEntry : hashTable.getEntries()) {
                if (currentEntry.getFileBlockIndex() != FrostConstants.MPQ_HASH_ENTRY_DELETED) {
                    if (currentEntry.getFilePathHashA() == hashA && currentEntry.getFilePathHashB() == hashB) {
                        if (currentEntry.getPlatform() == platform || platform == ANY_PLATFORM) {
                            if (currentEntry.getLanguage() == lang || lang == ANY_LANGUAGE) {
                                return currentEntry;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            context.getLogger().warn("Failed to hash " + fileName + " due to: " + ex.getMessage());
        }
        return null;
    }

    /**
     * Determines if file exists.
     *
     * @param hashTable Hash table to look through
     * @param fileName  File name to look for
     * @param lang      Language to look for (or ANY_LANGUAGE for any)
     * @param platform  Platform to look for (or ANY_PLATFORM for any)
     * @return True if exists, or false if not
     */
    public boolean hasFile(HashTable hashTable, String fileName, short lang, short platform) {
        return this.findEntry(hashTable, fileName, lang, platform) != null;
    }

    public List<HashTableEntry> findEntries(HashTable hashTable, String fileName, short lang, short platform) {
        try {
            List<HashTableEntry> entries = new ArrayList<>();
            // todo fix code duplication
            // Calculate hashes and find entry
            int hashA = frostSecurity.hashAsInt(fileName, MPQ_HASH_NAME_A);
            int hashB = frostSecurity.hashAsInt(fileName, MPQ_HASH_NAME_B);
            for(HashTableEntry currentEntry : hashTable.getEntries()) {
                if (currentEntry.getFileBlockIndex() != FrostConstants.MPQ_HASH_ENTRY_DELETED) {
                    if (currentEntry.getFilePathHashA() == hashA && currentEntry.getFilePathHashB() == hashB) {
                        if (currentEntry.getPlatform() == platform || platform == ANY_PLATFORM) {
                            if (currentEntry.getLanguage() == lang || lang == ANY_LANGUAGE) {
                                entries.add(currentEntry);
                            }
                        }
                    }
                }
            }
            return entries;
        } catch (Exception ex) {
            context.getLogger().warn("Failed to hash " + fileName + " due to: " + ex.getMessage());
        }
        return null;
    }
}
