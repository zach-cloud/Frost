package model;

import interfaces.IByteSerializable;
import settings.MpqContext;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static frost.FrostConstants.BYTES_PER_BLOCK_TABLE_ENTRY;

/**
 * Represents a single entry in the Block Table.
 */
public final class BlockTableEntry implements IByteSerializable {

    /**
     * Basic block information
     */
    private int blockOffset;
    private int blockSize;
    private int fileSize;
    private int flags;

    /**
     * Array index callback for debugging
     */
    private int callbackId;

    /**
     * Calculated from flags
     */
    private boolean isFile;
    private boolean singleUnit;
    private boolean keyAdjusted;
    private boolean encrypted;
    private boolean compressed;
    private boolean imploded;

    private MpqContext context;

    public BlockTableEntry(int blockOffset, int blockSize, int fileSize,
                           int flags, MpqContext context) {
        this.blockOffset = blockOffset;
        this.blockSize = blockSize;
        this.fileSize = fileSize;
        this.flags = flags;
        this.context = context;
        calculateFlagValues();
        checkFlagValidity();
    }

    /**
     * Converts this object into a byte array which represents
     * the same state as the object.
     *
     * @return Byte array of object.
     */
    @Override
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(BYTES_PER_BLOCK_TABLE_ENTRY);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(blockOffset);
        buffer.putInt(blockSize);
        buffer.putInt(fileSize);
        buffer.putInt(flags);
        return buffer.array();
    }

    /**
     * Checks that the flag values are valid.
     * If not valid, logs a warning.
     */
    private void checkFlagValidity() {
        if (keyAdjusted && !encrypted) {
            context.getLogger().warn("Block cannot be key adjusted and not encrypted");
        }
        if (!isFile) {
            if (blockSize > 0) {
                context.getLogger().warn("Block is not a file but has size");
            }
            if (singleUnit || keyAdjusted || encrypted || compressed || imploded) {
                context.getLogger().warn("Block is not a file but has flags");
            }
        }
    }

    /**
     * Calculates the values of each individual flag from the original flag int
     */
    private void calculateFlagValues() {
        isFile = (flags & 0x80000000) != 0;
        singleUnit = (flags & 0x01000000) != 0;
        keyAdjusted = (flags & 0x00020000) != 0;
        encrypted = (flags & 0x00010000) != 0;
        compressed = (flags & 0x00000200) != 0;
        imploded = (flags & 0x00000100) != 0;
    }

    public int getBlockOffset() {
        return blockOffset;
    }

    public void setBlockOffset(int blockOffset) {
        this.blockOffset = blockOffset;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public boolean isFile() {
        return isFile;
    }

    public boolean isSingleUnit() {
        return singleUnit;
    }

    public boolean isKeyAdjusted() {
        return keyAdjusted;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public boolean isImploded() {
        return imploded;
    }

    public int getCallbackId() {
        return callbackId;
    }

    public void setCallbackId(int callbackId) {
        this.callbackId = callbackId;
    }
}
