package helper;

import storm.StormConstants;

/**
 * Helper class to reverse the effects of malicious MPQs.
 */
public class MaliciousMPQHelper {

    public static int fixBlockTableSize(int originalBlockTableSize,
                                  int archiveSize, int blockTableOffset) {
        int totalBytesAvailable = archiveSize - blockTableOffset;
        int desiredBytes = StormConstants.BYTES_PER_BLOCK_TABLE_ENTRY * originalBlockTableSize;
        if(desiredBytes > totalBytesAvailable
                || desiredBytes < 0) {
            originalBlockTableSize =  totalBytesAvailable / StormConstants.BYTES_PER_BLOCK_TABLE_ENTRY;
        }
        return originalBlockTableSize;
    }
}
