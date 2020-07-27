package model;

import encryption.StormConstants;
import encryption.StormSecurity;
import interfaces.IReadable;
import reader.BinaryReader;
import settings.MpqContext;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class FileDataEntry implements IReadable {

    private static int SECTOR_SIZE_BYTES = 4096;

    private int initialPosition;
    private int archiveOffset;
    private ArchiveHeader header;
    private BlockTableEntry blockTableEntry;
    private HashTableEntry hashTableEntry;
    private StormSecurity stormSecurity;

    private boolean isComplete;

    private List<FileSector> sectors;
    private List<FileSectorEntry> newSectors;

    private int sectorsInFile;

    private int[] sectorOffsetTable;

    private MpqContext context;
    private BinaryReader reader;

    /**
     * Makes a new file data entry
     *
     * @param initialPosition First byte of file data
     * @param header          MPQ header
     * @param blockTableEntry Associated block table entry
     * @param hashTableEntry  Associated hash table entry
     */
    public FileDataEntry(int archiveOffset, StormSecurity stormSecurity, int initialPosition, ArchiveHeader header, BlockTableEntry blockTableEntry, HashTableEntry hashTableEntry, MpqContext context) {
        this.stormSecurity = stormSecurity;
        this.initialPosition = initialPosition;
        this.archiveOffset = archiveOffset;
        this.sectors = new ArrayList<>();
        this.newSectors = new ArrayList<>();
        this.header = header;
        this.blockTableEntry = blockTableEntry;
        this.hashTableEntry = hashTableEntry;
        this.context = context;
        sectorsInFile = blockTableEntry.getFileSize() / SECTOR_SIZE_BYTES;
        if (blockTableEntry.getFileSize() % SECTOR_SIZE_BYTES != 0) {
            // One sector holds remainder
            sectorsInFile++;
        }
        this.sectorOffsetTable = new int[sectorsInFile + 1];
    }

    private void read(BinaryReader reader, int key) {
        reader.setPosition(initialPosition);

        try {
            if(!blockTableEntry.isSingleUnit() || blockTableEntry.isCompressed() || blockTableEntry.isImploded()) {
                // For this one, we need to read the sector offset table.
                context.getLogger().debug("Reading data with offset table");
                readCompressedFiledata(reader, key);
            } else {
                context.getLogger().debug("Reading data without offset table");
                // TODO...
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Reads from the binary reader into this model object
     *
     * @param reader Binary reader
     */
    @Override
    public void read(BinaryReader reader) {
        read(reader, -1);
    }

    private void readCompressedFiledata(BinaryReader reader, int key) throws IOException {

        if(blockTableEntry.isEncrypted() && key == -1) {
            this.reader = reader; // Now we need to save the reader.
            return;
            // We'll need to do this later...
            // Should find a cleaner way of doing this.
        }

        int totalReadBytes = 0;

        // Build the offset table
        for(int i = 0; i < sectorsInFile + 1; i++) {
            sectorOffsetTable[i] = reader.readInt();
        }

        if(blockTableEntry.isEncrypted()) {
            sectorOffsetTable = stormSecurity.decrypt(sectorOffsetTable, key - 1);
        }

        // Use the offset table to compute each sector position and size
        for(int i = 0; i < sectorsInFile; i++) {
            int start = sectorOffsetTable[i];
            int end = sectorOffsetTable[i + 1];

            // Easily computed as end - start since that is the amount of bytes we'll read
            int compressedSectorSize = end - start; // TODO
            // If this isn't the final sector, then the size is SECTOR_SIZE_BYTES
            // If it is the final sector... we need to accumulate bytes throughout
            // and total them up, then do fileSize - readBytes
            int realSectorSize = 0; // TODO
            if(i != sectorsInFile - 1) {
                realSectorSize = SECTOR_SIZE_BYTES;
                totalReadBytes += SECTOR_SIZE_BYTES;
            } else {
                // The file size minus the read bytes provides us
                // the actual file size of the final sector
                realSectorSize = blockTableEntry.getFileSize() - totalReadBytes;
            }

            FileSectorEntry entry = new FileSectorEntry(start, end,
                    archiveOffset + blockTableEntry.getBlockOffset(),
                    compressedSectorSize, realSectorSize, true, blockTableEntry.isEncrypted(),
                    key, reader, context, stormSecurity);
            newSectors.add(entry);
        }

        isComplete = true;
    }

    public BlockTableEntry getBlockTableEntry() {
        return blockTableEntry;
    }

    public HashTableEntry getHashTableEntry() {
        return hashTableEntry;
    }

    public void extract(String fileName, File target) {
        int key = -1;
        if(blockTableEntry.isEncrypted()) {
            if(fileName.contains("\\")) {
                fileName = fileName.substring(fileName.indexOf("\\"));
            }
            key = stormSecurity.hashAsInt(fileName, StormConstants.MPQ_HASH_FILE_KEY);
            if(blockTableEntry.isKeyAdjusted()) {
                key = (key + blockTableEntry.getBlockOffset()) ^ blockTableEntry.getFileSize();
            }
        }
        if(isComplete) {
            context.getLogger().info("Extracting: " + fileName);
            context.getLogger().debug("File has " + blockTableEntry.getFileSize() + " bytes");
            ByteBuffer fileBytes = ByteBuffer.allocate(blockTableEntry.getFileSize());
            int sectorCount = 0;
            for(FileSectorEntry sector : newSectors) {
                context.getLogger().debug("Reading a sector...");
                sector.readRawData(sectorCount);
                sector.addBytes(fileBytes);
                sectorCount++;
            }
            try {
                context.getFileWriter().write(fileBytes.array(), target);
            } catch (IOException ex) {
                context.getErrorHandler().handleCriticalError(ex.getMessage());
            }
            context.getLogger().debug("Wrote to file: " + target.getAbsolutePath());

        } else {
            read(this.reader, key);
            if(!isComplete) {
                // For some reason, coulnd't read completely...
                context.getErrorHandler().handleCriticalError("Could not " +
                        "complete file data entry for " + fileName);
            }
            extract(fileName, target);
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
	08h: Imploded (see PKWare Data IGenericCompression Library)
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
