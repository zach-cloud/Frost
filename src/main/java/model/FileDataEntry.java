package model;

import encryption.StormConstants;
import encryption.StormCrypt;
import interfaces.IReadable;
import reader.BinaryReader;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class FileDataEntry implements IReadable {

    private static int SECTOR_SIZE_BYTES = 4096;

    private int initialPosition;
    private int archiveOffset;
    private ArchiveHeader header;
    private BlockTableEntry blockTableEntry;
    private HashTableEntry hashTableEntry;
    private StormCrypt stormCrypt;

    private boolean isComplete;

    private List<FileSector> sectors;
    private List<FileSectorEntry> newSectors;

    private int sectorsInFile;

    private int[] sectorOffsetTable;

    /**
     * Makes a new file data entry
     *
     * @param initialPosition First byte of file data
     * @param header          MPQ header
     * @param blockTableEntry Associated block table entry
     * @param hashTableEntry  Associated hash table entry
     */
    public FileDataEntry(int archiveOffset, StormCrypt stormCrypt, int initialPosition, ArchiveHeader header, BlockTableEntry blockTableEntry, HashTableEntry hashTableEntry) {
        this.stormCrypt = stormCrypt;
        this.initialPosition = initialPosition;
        this.archiveOffset = archiveOffset;
        this.sectors = new ArrayList<>();
        this.newSectors = new ArrayList<>();
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
        reader.setPosition(initialPosition);

        try {
            if(!blockTableEntry.isSingleUnit() || blockTableEntry.isCompressed() || blockTableEntry.isImploded()) {
                // For this one, we need to read the sector offset table.
                System.out.println("Reading data with offset table");
                readCompressedFiledata(reader);
            } else {
                System.out.println("Reading data without offset table");
                // TODO...
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void readCompressedFiledata(BinaryReader reader) throws IOException {

        // TODO: Extend this to work with multiple sectors.
        int start = reader.readInt();
        int end = reader.readInt();

        FileSectorEntry entry = new FileSectorEntry(start, end, archiveOffset + blockTableEntry.getBlockOffset(), reader);
        newSectors.add(entry);

        isComplete = true;

//        int readBytes = 0;
//        int readTotalNormalSize = 0;
//        // Read the sector offset table
//        for (int i = 0; i < sectorsInFile + 1; i++) {
//            sectorOffsetTable[i] = reader.readInt();
//        }
//
//        if(!blockTableEntry.isEncrypted()) { // We need the key for decryption
//
//            // Read the full sectors
//            for (int i = 0; i < sectorsInFile - 1; i++) {
//                // Go to first part of this sector
//                int sizeToRead = sectorOffsetTable[i + 1] - sectorOffsetTable[i];
//                readSector(reader, initialPosition + sectorOffsetTable[i], sizeToRead, SECTOR_SIZE_BYTES);
//                readBytes += sizeToRead;
//                readTotalNormalSize += SECTOR_SIZE_BYTES;
//            }
//
//            // Read the final sector
//            int remainingNormalSize = blockTableEntry.getFileSize() - readTotalNormalSize;
//            int remainingBytes = sectorOffsetTable[sectorOffsetTable.length - 1] - readBytes;
//            readSector(reader, initialPosition + sectorOffsetTable[sectorsInFile - 1], remainingBytes, remainingNormalSize);
//            reader.setPosition(initialPosition + sectorOffsetTable[sectorsInFile - 1]);
//            readBytes += remainingBytes;
//
//            if (readBytes != sectorOffsetTable[sectorOffsetTable.length - 1]) {
//                throw new RuntimeException("Failed assertion: read bytes did not equal total bytes");
//            }
//            isComplete = true;
//        } else {
//            isComplete = false;
//        }
    }

    private void readSector(BinaryReader reader, int offset, int size, int normalSize) {
        FileSector sector = new FileSector(size, normalSize, offset, blockTableEntry);
        // We'll read it later.
        // We don't want to store such a large amount of data in memory!
        sector.readLater(reader);
        sectors.add(sector);
    }

    private void decrypt(String fileName) {
        if(fileName.contains("\\")) {
            fileName = fileName.substring(fileName.indexOf("\\"));
        }
        int key = stormCrypt.hashAsInt(fileName, StormConstants.MPQ_HASH_FILE_KEY);
        if(blockTableEntry.isKeyAdjusted()) {
            key = (key + blockTableEntry.getBlockOffset() - archiveOffset) ^ blockTableEntry.getFileSize();
        }
        // TODO...
    }

    public BlockTableEntry getBlockTableEntry() {
        return blockTableEntry;
    }

    public HashTableEntry getHashTableEntry() {
        return hashTableEntry;
    }

    public void extract(String fileName) {
        if(isComplete) {
            System.out.println("Extracting: " + fileName);
            for(FileSectorEntry sector : newSectors) {
                sector.readSelf();
            }
        } else {
            System.out.println("File cannot be extracted yet (not complete)");
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
