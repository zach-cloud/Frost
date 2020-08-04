package model;

import helper.MaliciousMPQHelper;
import interfaces.IReadable;
import interfaces.IByteSerializable;
import reader.BinaryReader;
import settings.MpqContext;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ArchiveHeader implements IReadable, IByteSerializable {

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
    // Calculated
    private int sectorSize; // How many bytes per sector

    private int discoveredHeaderSize;

    private MpqContext context;

    public ArchiveHeader(MpqContext context) {
        this.context = context;
    }

    public String toString() {
        return "magic=" + magic + ",headerSize=" + headerSize + "archiveSize=" + archiveSize + "format=" + format
                + "sectorSizeShift=" + sectorSizeShift + "hashTableOffset=" + hashTableOffset + "blockTableOffset" +
                blockTableOffset + "hashTableEntries=" + hashTableEntries + "blockTableEntries=" + blockTableEntries +
                "extendedBlockTableOffset=" + extendedBlockTableOffset + "hashTableOffsetHigh=" + hashTableOffsetHigh +
                "blockTableOffsetHigh=" + blockTableOffsetHigh;
    }

    /**
     * Reads from the binary reader into this model object
     *
     * @param reader Binary reader
     */
    @Override
    public void read(BinaryReader reader) {
        try {
            reader.goTo("MPQ");
            int start = reader.getPosition();
            offsetStart = reader.getPosition();
            magic = reader.readString(4);
            headerSize = reader.readInt();
            archiveSize = reader.readInt();
            archiveSize = MaliciousMPQHelper.fixArchiveSize(archiveSize, reader, offsetStart);
            format = reader.readShort();
            sectorSizeShift = reader.readShort();
            hashTableOffset = reader.readInt();
            blockTableOffset = reader.readInt();
            hashTableEntries = reader.readInt();
            blockTableEntries = reader.readInt();
            blockTableEntries = MaliciousMPQHelper.fixBlockTableSize(blockTableEntries, archiveSize, blockTableOffset);
            if (format == 1) {
                extendedBlockTableOffset = reader.readLong();
                hashTableOffsetHigh = reader.readShort();
                blockTableOffsetHigh = reader.readShort();
                discoveredHeaderSize = 2 + reader.getPosition() - start;
            } else {
                discoveredHeaderSize = 4 + reader.getPosition() - start;
            }

            sectorSize = 512 * (int) (Math.pow(2, sectorSizeShift));

            context.getLogger().debug("Header size: " + discoveredHeaderSize + " bytes");
            context.getLogger().debug("Sector size: " + sectorSize);
        } catch (Exception ex) {
            ex.printStackTrace();
            context.getErrorHandler().handleCriticalError("Failed reading: " + ex.getMessage());
        }
    }

    /**
     * Converts this object into a byte array which represents
     * the same state as the object.
     *
     * @return Byte array of object.
     */
    @Override
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(discoveredHeaderSize);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(magic.getBytes());
        buffer.putInt(headerSize);
        buffer.putInt(archiveSize);
        buffer.putShort((short) format);
        buffer.putShort((short) sectorSizeShift);
        buffer.putInt(hashTableOffset);
        buffer.putInt(blockTableOffset);
        buffer.putInt(hashTableEntries);
        buffer.putInt(blockTableEntries);

        if (format == 1) {
            buffer.putLong(extendedBlockTableOffset);
            buffer.putShort((short)hashTableOffsetHigh);
            buffer.putShort((short)blockTableOffsetHigh);
        }

        return buffer.array();
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

    public int getSectorSize() {
        return sectorSize;
    }

    public void setSectorSize(int sectorSize) {
        this.sectorSize = sectorSize;
    }

    public MpqContext getContext() {
        return context;
    }

    public void setContext(MpqContext context) {
        this.context = context;
    }
}
