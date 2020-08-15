package custom;

import frost.FrostSecurity;
import model.BlockTable;
import model.BlockTableEntry;
import model.HashTable;
import model.HashTableEntry;

import java.util.ArrayList;
import java.util.List;

import static frost.FrostConstants.MPQ_HASH_NAME_A;
import static frost.FrostConstants.MPQ_HASH_NAME_B;

public class HashTableScanner {

    private HashTable hashTable;
    private BlockTable blockTable;
    private FrostSecurity frostSecurity;

    public HashTableScanner(HashTable hashTable,
                            BlockTable blockTable,
                            FrostSecurity frostSecurity) {
        this.hashTable = hashTable;
        this.blockTable = blockTable;
        this.frostSecurity = frostSecurity;
    }

    public List<HashTableEntry> scan(String fileName, int fileSize) {
        List<HashTableEntry> entries = new ArrayList<>();
        int hashA = frostSecurity.hashAsInt(fileName, MPQ_HASH_NAME_A);
        int hashB = frostSecurity.hashAsInt(fileName, MPQ_HASH_NAME_B);
        for(HashTableEntry entry : hashTable.getEntries()) {
            if(entry.getFilePathHashA() == hashA &&
                    entry.getFilePathHashB() == hashB) {

                BlockTableEntry blockTableEntry = blockTable.get(entry.getFileBlockIndex());

                    if(blockTableEntry.getFileSize() == fileSize) {
                        entries.add(entry);
                    }

            }
        }
        return entries;
    }
}
