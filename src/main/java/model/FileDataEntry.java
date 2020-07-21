package model;

import interfaces.IReadable;
import reader.BinaryReader;

import java.io.IOException;

public class FileDataEntry implements IReadable {

    private static int SECTOR_SIZE_BYTES = 4096;

    private ArchiveHeader header;
    private BlockTableEntry blockTableEntry;
    private HashTableEntry hashTableEntry;

    private int sectorsInFile = 0;

    private int[] sectorOffsetTable;
    private byte compressionMask;

    /**
     * Makes a new file data entry
     *
     * @param header          MPQ header
     * @param blockTableEntry Associated block table entry
     * @param hashTableEntry  Associated hash table entry
     */
    public FileDataEntry(ArchiveHeader header, BlockTableEntry blockTableEntry, HashTableEntry hashTableEntry) {
        this.header = header;
        this.blockTableEntry = blockTableEntry;
        this.hashTableEntry = hashTableEntry;
        sectorsInFile = blockTableEntry.getFileSize() / SECTOR_SIZE_BYTES;
        if (blockTableEntry.getFileSize() % SECTOR_SIZE_BYTES != 0) {
            // One sector holds remainder
            sectorsInFile++;
        }
        this.sectorOffsetTable = new int[sectorsInFile + 1];
    }

    /**
     * Reads from the binary reader into this model object
     *
     * @param reader Binary reader
     */
    @Override
    public void read(BinaryReader reader) {
        try {
            for (int i = 0; i < sectorsInFile + 1; i++) {
                sectorOffsetTable[i] = reader.readInt();
            }
            if(blockTableEntry.isCompressed()) {
                compressionMask = reader.readByte();
            }
            System.out.println();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /*
    2.6 FILE DATA
The data for each file is composed of the following structure:
00h: int32(SectorsInFile + 1) SectorOffsetTable :

Offsets to the start of each sector, relative to the beginning of the file data.
The last entry contains the file size, making it possible to easily calculate the size
of any given sector. This table is not present if this information can be calculated (see details below).
immediately following SectorOffsetTable: SECTOR Sectors(SectorsInFile) :
Data of each sector in the file, packed end to end (see details below).

Normally, file data is split up into sectors, for simple streaming. All sectors,
save for the last, will contain as many bytes of file data as specified in the
archive header's SectorSizeShift; the last sector may contain less than this,
depending on the size of the entire file's data. If the file is compressed or
imploded, the sector will be smaller or the same size as the file data it contains.
Individual sectors in a compressed or imploded file may be stored uncompressed;
this occurs if and only if the file data the sector contains could not be compressed
by the algorithm(s) used (if the compressed sector size was greater than or equal
to the size of the file data), and is indicated by the sector's size in SectorOffsetTable
being equal to the size of the file data in the sector (which may be calculated from the FileSize).

The format of each sector depends on the kind of sector it is. Uncompressed sectors are simply the
the raw file data contained in the sector. Imploded sectors are the raw compressed data
following compression with the implode algorithm (these sectors can only be in imploded files).
Compressed sectors (only found in compressed - not imploded - files) are compressed with one or
more compression algorithms, and have the following structure:
00h: byte CompressionMask : Mask of the compression types applied to this sector. If multiple
compression types are used, they are applied in the order listed below, and decompression is
performed in the opposite order. This byte counts towards the total sector size, meaning that
the sector will be stored uncompressed if the data cannot be compressed by at least two bytes;
as well, this byte is encrypted with the sector data, if applicable. The following compression
types are defined (for implementations of these algorithms, see StormLib):
	40h: IMA ADPCM mono
	80h: IMA ADPCM stereo
	01h: Huffman encoded
	02h: Deflated (see ZLib)
	08h: Imploded (see PKWare Data Compression Library)
	10h: BZip2 compressed (see BZip2)
01h: byte(SectorSize - 1) SectorData : The compressed data for the sector.

If the file is stored as a single unit (indicated in the file's Flags),
there is effectively only a single sector, which contains the entire file data.

If the file is encrypted, each sector (after compression/implosion, if applicable)
is encrypted with the file's key. The base key for a file is determined by a
hash of the file name stripped of the directory (i.e. the key for a file named
"directory\file" would be computed as the hash of "file"). If this key is adjusted,
as indicated in the file's Flags, the final key is calculated as
((base key + BlockOffset - ArchiveOffset) XOR FileSize)
(StormLib incorrectly uses an AND in place of the XOR). Each sector is encrypted using the key +
 the 0-based index of the sector in the file. The SectorOffsetTable, if present, is encrypted using the key - 1.

The SectorOffsetTable is omitted when the sizes and offsets of all sectors in the file are calculatable
from the FileSize. This can happen in several circumstances. If the file is not compressed/imploded
, then the size and offset of all sectors is known, based on the archive's SectorSizeShift.
If the file is stored as a single unit compressed/imploded, then the SectorOffsetTable is
omitted, as the single file "sector" corresponds to BlockSize and FileSize, as mentioned
previously. However, the SectorOffsetTable will be present if the file is compressed/imploded
and the file is not stored as a single unit, even if there is only a single sector in the file
 (the size of the file is less than or equal to the archive's sector size).

     */
}
