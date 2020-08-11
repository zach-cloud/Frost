package custom;

import model.BlockTable;
import model.BlockTableEntry;
import model.HashTable;
import model.HashTableEntry;

import java.util.ArrayList;
import java.util.List;

public class PgProtectionRemover {

    private int hashA = 870877111;
    private int hashB = -785055414;

    private static boolean isPowerOfTwo(int n) {
        if (n == 0)
            return false;

        return (int) (Math.ceil((Math.log(n) / Math.log(2)))) ==
                (int) (Math.floor(((Math.log(n) / Math.log(2)))));
    }

    /**
     * Checks for PG Protection.
     *
     * @param hashTableSize Archive hash table size
     * @return True if PG Protected. False if not.
     */
    public boolean pgProtectionChecker(int hashTableSize) {
        if (isPowerOfTwo(hashTableSize)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Removes PG Protection.
     *
     * @param blockTable Archive block table
     * @param hashTable  Archive hash table
     */
    public void removePgProtection(BlockTable blockTable, HashTable hashTable, int archiveSize) {
        // Find a valid block table entry
        List<BlockTableEntry> validBlocks = new ArrayList<>();
        for (BlockTableEntry entry : blockTable.getEntries()) {
            if(entry.isKeyAdjusted() &&
                    entry.getFileSize() < archiveSize &&
                    entry.getBlockSize() < archiveSize &&
                    entry.getFileSize() > 0) {
                validBlocks.add(entry);
            }
        }

        System.out.println("Found valid blocks");

        int knownIndex = -1;
        for (BlockTableEntry entry : blockTable.getEntries()) {
            if (entry.getFileSize() == 795 && entry.getFileSize() > 0) {
                knownIndex = entry.getCallbackId();
                break;
            }
        }
        boolean foundValidEntry = false;

        for (HashTableEntry entry : hashTable.getEntries()) {
            if (entry.getFileBlockIndex() == knownIndex
            && entry.getFilePathHashA() == hashA
            && entry.getFilePathHashB() == hashB) {
                foundValidEntry = true;
            }
        }
        System.out.println(knownIndex);
    }
}
