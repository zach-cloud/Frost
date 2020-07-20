package model;

public class BlockTableEntry {

    private int blockOffset;
    private int blockSize;
    private int fileSize;
    private int flags;

    /** Calculated from flags */
    private boolean isFile;
    private boolean singleUnit;
    private boolean keyAdjusted;
    private boolean encrypted;
    private boolean compressed;
    private boolean imploded;

    public BlockTableEntry(int blockOffset, int blockSize, int fileSize, int flags) {
        this.blockOffset = blockOffset;
        this.blockSize = blockSize;
        this.fileSize = fileSize;
        this.flags = flags;
        isFile = (flags & 0x80000000) != 0;
        singleUnit = (flags & 0x01000000) != 0;
        keyAdjusted = (flags & 0x00020000) != 0;
        encrypted = (flags & 0x00010000) != 0;
        compressed = (flags & 0x00000200) != 0;
        imploded = (flags & 0x00000100) != 0;
        if(keyAdjusted && !encrypted) {
            throw new IllegalArgumentException("Block cannot be key adjusted and not encrypted");
        }
        if(!isFile) {
            if(blockSize > 0) {
                throw new IllegalArgumentException("Block is not a file but has size");
            }
            if(singleUnit || keyAdjusted || encrypted || compressed || imploded) {
                throw new IllegalArgumentException("Block is not a file but has flags");
            }
        }
    }

    /**
     * 00h: int32 BlockOffset : Offset of the beginning of the block, relative to the beginning of the archive.
     * 04h: int32 BlockSize : Size of the block in the archive.
     * 08h: int32 FileSize : Size of the file data stored in the block. Only valid if the block is a file; otherwise meaningless, and should be 0. If the file is compressed, this is the size of the uncompressed file data.
     * 0Ch: int32 Flags : Bit mask of the flags for the block. The following values are conclusively identified:
     * 	80000000h: Block is a file, and follows the file data format; otherwise, block is free space or unused. If the block is not a file, all other flags should be cleared, and FileSize should be 0.
     * 	01000000h: File is stored as a single unit, rather than split into sectors.
     * 	00020000h: The file's encryption key is adjusted by the block offset and file size (explained in detail in the File Data section). File must be encrypted.
     * 	00010000h: File is encrypted.
     * 	00000200h: File is compressed. File cannot be imploded.
     * 	00000100h: File is imploded. File cannot be compressed.
     */



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
}
