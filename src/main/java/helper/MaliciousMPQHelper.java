package helper;

import reader.BinaryReader;
import frost.FrostConstants;

import static frost.FrostConstants.MPQ_HASH_ENTRY_DELETED;
import static frost.FrostConstants.MPQ_HASH_ENTRY_EMPTY;

/**
 * Helper class to reverse the effects of malicious MPQs.
 */
public final class MaliciousMPQHelper {

    /**
     * MPQs may have an invalid block/hash table size set,
     * where the size is much greater than the remaining
     * available bytes. This method fixes the block/hash
     * table size.
     *
     * @param tableSize   Original size that may or may not be valid
     * @param archiveSize Total archive size
     * @param tableOffset Block table offset in archive
     * @return Valid block table size
     */
    public static int fixTableSize(int tableSize,
                                   int archiveSize, int tableOffset) {
        int totalBytesAvailable = archiveSize - tableOffset;
        long desiredBytes = (long)FrostConstants.BYTES_PER_BLOCK_TABLE_ENTRY * (long)tableSize;
        if (desiredBytes > totalBytesAvailable
                || desiredBytes < 0) {
            tableSize = totalBytesAvailable / FrostConstants.BYTES_PER_BLOCK_TABLE_ENTRY;
        }
        return tableSize;
    }

    /**
     * MPQs may have an invalid archive size, where the archive size is
     * too large for the contents of the actual file.
     *
     * @param originalArchiveSize Archive size that may or may not be valid
     * @param reader              Reader that is processing the file
     * @param headerOffset        Offset to start of header
     * @return Valid archive size
     */
    public static int fixArchiveSize(int originalArchiveSize,
                                     BinaryReader reader, int headerOffset) {
        int availableSize = reader.getSize() - headerOffset;
        if (availableSize >= originalArchiveSize) {
            return originalArchiveSize;
        }
        return availableSize;
    }

    /**
     * Checks if this operation will overflow values.
     *
     * @param entryCount    Num. entries in the table
     * @param entrySize     Size per entry
     * @return              True if overflows, false if not.
     */
    public static boolean sizeCheck(int entryCount, int entrySize) {
        long calculatedValue = (long)entryCount * (long)entrySize;
        return (calculatedValue >= Integer.MAX_VALUE);
    }

    /**
     * Corrects malicious negative values.
     *
     * @param original  Original value
     * @return          Valid value
     */
    public static int fixNegativeValue(int original) {
        // If it's negative 1, it's more questionable.
        if(original < -1 && original != MPQ_HASH_ENTRY_EMPTY && original != MPQ_HASH_ENTRY_DELETED) {
            original += Integer.MAX_VALUE;
        }
        return original;
    }
}
