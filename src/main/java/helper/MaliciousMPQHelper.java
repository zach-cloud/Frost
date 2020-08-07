package helper;

import reader.BinaryReader;
import frost.FrostConstants;

/**
 * Helper class to reverse the effects of malicious MPQs.
 */
public final class MaliciousMPQHelper {

    /**
     * MPQs may have an invalid block table size set, where the size is much
     * greater than the remaining available bytes.
     * This method fixes the block table size.
     *
     * @param originalBlockTableSize    Original size that may or may not be valid
     * @param archiveSize               Total archive size
     * @param blockTableOffset          Block table offset in archive
     * @return                          Valid block table size
     */
    public static int fixBlockTableSize(int originalBlockTableSize,
                                  int archiveSize, int blockTableOffset) {
        int totalBytesAvailable = archiveSize - blockTableOffset;
        int desiredBytes = FrostConstants.BYTES_PER_BLOCK_TABLE_ENTRY * originalBlockTableSize;
        if(desiredBytes > totalBytesAvailable
                || desiredBytes < 0) {
            originalBlockTableSize =  totalBytesAvailable / FrostConstants.BYTES_PER_BLOCK_TABLE_ENTRY;
        }
        return originalBlockTableSize;
    }

    /**
     * MPQs may have an invalid archive size, where the archive size is
     * too large for the contents of the actual file.
     *
     * @param originalArchiveSize   Archive size that may or may not be valid
     * @param reader                Reader that is processing the file
     * @param headerOffset          Offset to start of header
     * @return                      Valid archive size
     */
    public static int fixArchiveSize(int originalArchiveSize,
                                     BinaryReader reader, int headerOffset) {
        int availableSize = reader.getSize() - headerOffset;
        if(availableSize >= originalArchiveSize) {
            return originalArchiveSize;
        }
        return availableSize;
    }
}
