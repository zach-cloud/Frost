package model;

import interfaces.IReadable;
import reader.BinaryReader;

public class ArchiveHeader implements IReadable {

    private int offsetStart;
    private String magic = ""; //char4
    private int headerSize; //int32
    private int archiveSize; //int32
    private int format; //int16 (0 = original, 1 = burning crusade)
    private int sectorSizeShift; //int8
    private int hashTableOffset; //int32
    private int blockTableOffset; //int32
    private int hashTableEntries; //int32
    private int blockTableEntries; //int32
    // Only in burning crusade +
    private long extendedBlockTableOffset; //int64
    private int hashTableOffsetHigh; //int16
    private int blockTableOffsetHigh; //int16

    /**
     * 00h: char(4) Magic : Indicates that the file is a MoPaQ archive. Must be ASCII "MPQ" 1Ah.
     * 04h: int32 HeaderSize : Size of the archive header.
     * 08h: int32 ArchiveSize : Size of the whole archive, including the header. Does not include the strong digital signature, if present. This size is used, among other things, for determining the region to hash in computing the digital signature. This field is deprecated in the Burning Crusade MoPaQ format, and the size of the archive is calculated as the size from the beginning of the archive to the end of the hash table, block table, or extended block table (whichever is largest).
     * 0Ch: int16 FormatVersion : MoPaQ format version. MPQAPI will not open archives where this is negative. Known versions:
     * 	0000h: Original format. HeaderSize should be 20h, and large archives are not supported.
     * 	0001h: Burning Crusade format. Header size should be 2Ch, and large archives are supported.
     * 0Eh: int8 SectorSizeShift : Power of two exponent specifying the number of 512-byte disk sectors in each logical sector in the archive. The size of each logical sector in the archive is 512 * 2^SectorSizeShift. Bugs in the Storm library dictate that this should always be 3 (4096 byte sectors).
     * 10h: int32 HashTableOffset : Offset to the beginning of the hash table, relative to the beginning of the archive.
     * 14h: int32 BlockTableOffset : Offset to the beginning of the block table, relative to the beginning of the archive.
     * 18h: int32 HashTableEntries : Number of entries in the hash table. Must be a power of two, and must be less than 2^16 for the original MoPaQ format, or less than 2^20 for the Burning Crusade format.
     * 1Ch: int32 BlockTableEntries : Number of entries in the block table.
     * Fields only present in the Burning Crusade format and later:
     * 20h: int64 ExtendedBlockTableOffset : Offset to the beginning of the extended block table, relative to the beginning of the archive.
     * 28h: int16 HashTableOffsetHigh : High 16 bits of the hash table offset for large archives.
     * 2Ah: int16 BlockTableOffsetHigh : High 16 bits of the block table offset for large archives.
     */

    /**
     * Reads from the binary reader into this model object
     *
     * @param reader Binary reader
     */
    @Override
    public void read(BinaryReader reader) {
        try {
            reader.goTo("MPQ");
            offsetStart = reader.getPosition();
            magic = reader.readString(4);
            headerSize = reader.readInt();
            archiveSize = reader.readInt();
            format = reader.readShort();
            sectorSizeShift = reader.readShort();
            hashTableOffset = reader.readInt();
            blockTableOffset = reader.readInt();
            hashTableEntries = reader.readInt();
            blockTableEntries = reader.readInt();
            if(format == 1) {
                extendedBlockTableOffset = reader.readLong();
                hashTableOffsetHigh = reader.readShort();
                blockTableOffsetHigh = reader.readShort();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed reading: " + ex.getMessage());
        }
    }


    public String getMagic() {
        return magic;
    }

    public void setMagic(String magic) {
        this.magic = magic;
    }

    public int getHeaderSize() {
        return headerSize;
    }

    public void setHeaderSize(int headerSize) {
        this.headerSize = headerSize;
    }

    public int getArchiveSize() {
        return archiveSize;
    }

    public void setArchiveSize(int archiveSize) {
        this.archiveSize = archiveSize;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public int getSectorSizeShift() {
        return sectorSizeShift;
    }

    public void setSectorSizeShift(int sectorSizeShift) {
        this.sectorSizeShift = sectorSizeShift;
    }

    public int getHashTableOffset() {
        return hashTableOffset;
    }

    public void setHashTableOffset(int hashTableOffset) {
        this.hashTableOffset = hashTableOffset;
    }

    public int getBlockTableOffset() {
        return blockTableOffset;
    }

    public void setBlockTableOffset(int blockTableOffset) {
        this.blockTableOffset = blockTableOffset;
    }

    public int getHashTableEntries() {
        return hashTableEntries;
    }

    public void setHashTableEntries(int hashTableEntries) {
        this.hashTableEntries = hashTableEntries;
    }

    public int getBlockTableEntries() {
        return blockTableEntries;
    }

    public void setBlockTableEntries(int blockTableEntries) {
        this.blockTableEntries = blockTableEntries;
    }

    public long getExtendedBlockTableOffset() {
        return extendedBlockTableOffset;
    }

    public void setExtendedBlockTableOffset(long extendedBlockTableOffset) {
        this.extendedBlockTableOffset = extendedBlockTableOffset;
    }

    public int getHashTableOffsetHigh() {
        return hashTableOffsetHigh;
    }

    public void setHashTableOffsetHigh(int hashTableOffsetHigh) {
        this.hashTableOffsetHigh = hashTableOffsetHigh;
    }

    public int getBlockTableOffsetHigh() {
        return blockTableOffsetHigh;
    }

    public void setBlockTableOffsetHigh(int blockTableOffsetHigh) {
        this.blockTableOffsetHigh = blockTableOffsetHigh;
    }

    public int getOffsetStart() {
        return offsetStart;
    }

    public void setOffsetStart(int offsetStart) {
        this.offsetStart = offsetStart;
    }
}
