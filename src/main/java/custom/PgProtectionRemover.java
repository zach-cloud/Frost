package custom;

import model.BlockTable;
import model.BlockTableEntry;
import model.HashTable;
import model.HashTableEntry;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
            if(!entry.isEncrypted()
                && !entry.isKeyAdjusted()
                && entry.isCompressed()
                && entry.isFile()
                && entry.getBlockSize() == -1
                && entry.getFileSize() < 1000000) {
                validBlocks.add(entry);
            }
        }

        System.out.println("Found valid blocks");

        int knownIndex = -1;
        for (BlockTableEntry entry : blockTable.getEntries()) {
            if (entry.getFileSize() == 795) {
                knownIndex = entry.getCallbackId();
                break;
            }
        }
        boolean foundValidEntry = false;

        for (HashTableEntry entry : hashTable.getEntries()) {
            if(entry.getFilePathHashA() == hashA &&
            entry.getFilePathHashB() == hashB) {
                System.out.println("OK");
            }
            if (entry.getFileBlockIndex() == knownIndex) {
                foundValidEntry = true;
            }
        }
        System.out.println(knownIndex);
    }

    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(870877111);
        byte[] stuff = buffer.array();
        System.out.println(hex(stuff));
    }

    public static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            result.append(String.format("%02x", aByte));
            // upper case
            // result.append(String.format("%02X", aByte));
        }
        return result.toString();
    }
}
