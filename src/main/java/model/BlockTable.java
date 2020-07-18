package model;

import interfaces.IReadable;
import reader.BinaryReader;

import java.io.IOException;

public class BlockTable implements IReadable {

    private int blockOffset;
    private int blockSize;
    private int fileSize;
    private int flags;
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

    /**
     * Reads from the binary reader into this model object
     *
     * @param reader Binary reader
     */
    @Override
    public void read(BinaryReader reader) {
        try {
            blockOffset = reader.readInt();
            blockSize = reader.readInt();
            fileSize = reader.readInt();
            flags = reader.readInt();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
