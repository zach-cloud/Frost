package frost;

import model.FileDataEntry;
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

    public FileDataEntry findFileData(String fileName, List<FileDataEntry> fileData) {
        int hashA = frostSecurity.hashAsInt(fileName, MPQ_HASH_NAME_A);
        int hashB = frostSecurity.hashAsInt(fileName, MPQ_HASH_NAME_B);
        for(FileDataEntry entry : fileData) {
            if(entry.getBlockTableEntry().getFileSize() == 795) {
                System.out.println("HERE");
            }
            if(entry.getHashTableEntry().getFilePathHashA() == hashA &&
                    entry.getHashTableEntry().getFilePathHashB() == hashB) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Finds a hash table entry for file name, or null if no such entry exists.
     * Brute force method.
     *
     * @param hashTable Hash table to look through
     * @param fileName  File name to look for
     * @param lang      Language to look for (or ANY_LANGUAGE for any)
     * @param platform  Platform to look for (or ANY_PLATFORM for any)
     * @return Hash table entry, or null if not exists.
     */
    public HashTableEntry findEntryV2(HashTable hashTable, String fileName, short lang, short platform) {
        int hashA = frostSecurity.hashAsInt(fileName, MPQ_HASH_NAME_A);
        int hashB = frostSecurity.hashAsInt(fileName, MPQ_HASH_NAME_B);
        HashTableEntry correctEntry = null;
        for(int i = 0; i < hashTable.getEntries().size(); i++) {
            HashTableEntry current = hashTable.get(i);
            if(current.getFilePathHashA() == hashA && current.getFilePathHashB() == hashB) {
                correctEntry = current;
                context.getLogger().debug("Found a matching entry for hashes (v2)");
            }
            context.getLogger().debug(i+"");
        }
        return correctEntry;
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
            // Find entry in hash table for file
            long initialEntry = frostSecurity.hashAsInt(fileName, MPQ_HASH_TABLE_OFFSET) & (hashTable.size() - 1);

            // Is there anything there?
            HashTableEntry entry = hashTable.get((int) initialEntry);
            if (entry.getFileBlockIndex() == FrostConstants.MPQ_HASH_ENTRY_EMPTY ||
                    entry.getFileBlockIndex() == MPQ_HASH_ENTRY_DELETED) {
                return null;
            }

            // Calculate hashes and find entry
            int hashA = frostSecurity.hashAsInt(fileName, MPQ_HASH_NAME_A);
            int hashB = frostSecurity.hashAsInt(fileName, MPQ_HASH_NAME_B);
            HashTableEntry currentEntry;
            int currentIndex = (int) initialEntry;
            while (currentIndex < hashTable.size()) {
                currentEntry = hashTable.get(currentIndex);
                if (currentEntry.getFileBlockIndex() != FrostConstants.MPQ_HASH_ENTRY_DELETED) {
                    if (currentEntry.getFilePathHashA() == hashA && currentEntry.getFilePathHashB() == hashB) {
                        if (currentEntry.getPlatform() == platform || platform == ANY_PLATFORM) {
                            if (currentEntry.getLanguage() == lang || lang == ANY_LANGUAGE) {
                                return currentEntry;
                            }
                        }
                    }
                }
                currentIndex++;
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

    public List<HashTableEntry> findAllEntries(HashTable hashTable, String fileName, short anyLanguage, short anyPlatform) {
        int hashA = frostSecurity.hashAsInt(fileName, MPQ_HASH_NAME_A);
        int hashB = frostSecurity.hashAsInt(fileName, MPQ_HASH_NAME_B);
        List<HashTableEntry> entries = new ArrayList<>();
        for(int i = 0; i < hashTable.getEntries().size(); i++) {
            HashTableEntry current = hashTable.get(i);
            if(current.getFilePathHashA() == hashA && current.getFilePathHashB() == hashB) {
                entries.add(current);
                context.getLogger().debug("Found a matching entry for hashes (all entries)");
            }
        }
        return entries;
    }

    /*
    bool FindFileInHashTable(const HashTableEntry *lpHashTable, unsigned long nHashTableSize, const char *lpszFilePath, unsigned short nLang, unsigned char nPlatform, unsigned long &iFileHashEntry)
{
	assert(lpHashTable);
	assert(nHashTableSize);
	assert(lpszFilePath);

	// Find the home entry in the hash table for the file
	unsigned long iInitEntry = HashString(lpszFilePath, MPQ_HASH_TABLE_OFFSET) & (nHashTableSize - 1);

	// Is there anything there at all?
	if (lpHashTable[iInitEntry].FileBlockIndex == MPQ_HASH_ENTRY_EMPTY)
		return false;

	// Compute the hashes to compare the hash table entry against
	unsigned long nNameHashA = HashString(lpszFilePath, MPQ_HASH_NAME_A),
		nNameHashB = HashString(lpszFilePath, MPQ_HASH_NAME_B),
		iCurEntry = iInitEntry;

	// Check each entry in the hash table till a termination point is reached
	do
	{
		if (lpHashTable[iCurEntry].FileBlockIndex != MPQ_HASH_ENTRY_DELETED)
		{
			if (lpHashTable[iCurEntry].FilePathHashA == nNameHashA
				&& lpHashTable[iCurEntry].FilePathHashB == nNameHashB
				&& lpHashTable[iCurEntry].Language == nLang
				&& lpHashTable[iCurEntry].Platform == nPlatform)
			{
				iFileHashEntry = iCurEntry;

				return true;
			}
		}

		iCurEntry = (iCurEntry + 1) & (nHashTableSize - 1);
	} while (iCurEntry != iInitEntry && lpHashTable[iCurEntry].FileBlockIndex != MPQ_HASH_ENTRY_EMPTY);

	return false;
}
     */
}
