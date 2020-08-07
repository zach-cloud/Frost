package model;

import frost.FrostSecurity;
import interfaces.IByteSerializable;
import frost.FrostConstants;
import interfaces.IReadable;
import reader.BinaryReader;
import settings.MpqContext;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public final class FileDataEntry implements IReadable, IByteSerializable {

    private int initialPosition;
    private int archiveOffset;
    private ArchiveHeader header;
    private BlockTableEntry blockTableEntry;
    private HashTableEntry hashTableEntry;
    private FrostSecurity frostSecurity;

    private String fileName = "(unknown)";

    private boolean isComplete;

    private List<FileSectorEntry> newSectors;

    private int sectorsInFile;

    private int[] sectorOffsetTable;
    private int[] originalOffsetTable;

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
    public FileDataEntry(int archiveOffset, FrostSecurity frostSecurity, int initialPosition, ArchiveHeader header, BlockTableEntry blockTableEntry, HashTableEntry hashTableEntry, MpqContext context) {
        this.frostSecurity = frostSecurity;
        this.initialPosition = initialPosition;
        this.archiveOffset = archiveOffset;
        this.newSectors = new ArrayList<>();
        this.header = header;
        this.blockTableEntry = blockTableEntry;
        this.hashTableEntry = hashTableEntry;
        this.context = context;
        sectorsInFile = blockTableEntry.getFileSize() / header.getSectorSize();
        if(sectorsInFile < 0) {
            context.getLogger().warn("Negative sector count: " + sectorsInFile);
        }
        if (blockTableEntry.getFileSize() % header.getSectorSize() != 0) {
            // One sector holds remainder
            sectorsInFile++;
        }
        this.sectorOffsetTable = new int[sectorsInFile + 1];
        this.originalOffsetTable = new int[sectorsInFile + 1];
    }

    private void read(BinaryReader reader, int key) {
        if(blockTableEntry.getFileSize() <= 0) {
            // A worthless block. Let's skip it.
            isComplete = false;
            return;
        }
        reader.setPosition(initialPosition);

        try {
           if (!(blockTableEntry.isCompressed() || blockTableEntry.isImploded())) {
               context.getLogger().debug("Reading data with no offset table");
               readUncompressedFileData(reader, key);
            } else {
                // For this one, we need to read the sector offset table.
                context.getLogger().debug("Reading data with an offset table");
                readCompressedFiledata(reader, key);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void setSingleSectorData(byte[] data) {
        this.sectorsInFile = 0;
        this.originalOffsetTable = new int[0];
        this.sectorOffsetTable = new int[0]; // We do not need a sector offset table.
        FileSectorEntry entry = new FileSectorEntry(0, data.length, blockTableEntry.getBlockOffset(),
                data.length, data.length, false, false, -1,null,  context, frostSecurity);
        entry.setSingleSectorData(data);
        newSectors.add(entry);
        isComplete = true;
        entry.setRead(true);
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

    private void readUncompressedFileData(BinaryReader reader, int key) throws IOException {
        if (blockTableEntry.isEncrypted() && key == -1) {
            this.reader = reader; // Now we need to save the reader.
            return;
            // We'll need to do this later...
            // Should find a cleaner way of doing this.
        }

        int currentPosition = 0;
        int remainingSize = blockTableEntry.getFileSize();

        // While unlikely, it's possible that an uncompressed file can be large enough
        // to be split into multiple sectors.
        for(int i = 0; i < sectorsInFile - 1; i++) {
            int start = currentPosition;
            int end = currentPosition + header.getSectorSize();
            currentPosition = end;
            remainingSize -= header.getSectorSize();

            FileSectorEntry entry = new FileSectorEntry(start, end,
                    archiveOffset + blockTableEntry.getBlockOffset(),
                    blockTableEntry.getFileSize(), blockTableEntry.getFileSize(),
                    false, blockTableEntry.isEncrypted(),
                    key, reader, context, frostSecurity);
            newSectors.add(entry);
        }
        int fileEnd = currentPosition + remainingSize;
        FileSectorEntry entry = new FileSectorEntry(currentPosition, fileEnd,
                archiveOffset + blockTableEntry.getBlockOffset(),
                blockTableEntry.getFileSize(), blockTableEntry.getFileSize(),
                false, blockTableEntry.isEncrypted(),
                key, reader, context, frostSecurity);
        newSectors.add(entry);

        isComplete = true;
    }

    private void readCompressedFiledata(BinaryReader reader, int key) throws IOException {

        if (blockTableEntry.isEncrypted() && key == -1) {
            this.reader = reader; // Now we need to save the reader.
            return;
            // We'll need to do this later...
            // Should find a cleaner way of doing this.
        }

        int totalReadBytes = 0;

        // Build the offset table
        for (int i = 0; i < sectorsInFile + 1; i++) {
            sectorOffsetTable[i] = reader.readInt();
            originalOffsetTable[i] = sectorOffsetTable[i];
        }

        if (blockTableEntry.isEncrypted()) {
            sectorOffsetTable = frostSecurity.decrypt(sectorOffsetTable, key - 1);
        }

        // Use the offset table to compute each sector position and size
        for (int i = 0; i < sectorsInFile; i++) {
            int start = sectorOffsetTable[i];
            int end = sectorOffsetTable[i + 1];

            // Easily computed as end - start since that is the amount of bytes we'll read
            int compressedSectorSize = end - start; // TODO
            // If this isn't the final sector, then the size is SECTOR_SIZE_BYTES
            // If it is the final sector... we need to accumulate bytes throughout
            // and total them up, then do fileSize - readBytes
            int realSectorSize = 0; // TODO
            if (i != sectorsInFile - 1) {
                realSectorSize = header.getSectorSize();
                totalReadBytes += header.getSectorSize();
            } else {
                // The file size minus the read bytes provides us
                // the actual file size of the final sector
                realSectorSize = blockTableEntry.getFileSize() - totalReadBytes;
            }

            // Even if the compression flag is set to TRUE, we need to check the sizes.
            // If the compressed size is larger or equal to the uncompressed size,
            // it will NOT actually compress the data! We can treat it as uncompressed.
            boolean isActuallyCompressed = compressedSectorSize < realSectorSize;
            FileSectorEntry entry = new FileSectorEntry(start, end,
                    archiveOffset + blockTableEntry.getBlockOffset(),
                    compressedSectorSize, realSectorSize, isActuallyCompressed, blockTableEntry.isEncrypted(),
                    key, reader, context, frostSecurity);
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

    public byte[] getFileBytes(String fileName) {
        this.fileName = fileName;
        if(blockTableEntry.getFileSize() <= 0) {
            // Extract the empty file, I guess?
            return new byte[0];
        }
        int key = -1;
        if (blockTableEntry.isEncrypted() && !isComplete) {
            if (fileName.contains("\\")) {
                fileName = fileName.substring(1 + fileName.lastIndexOf("\\"));
            }
            key = frostSecurity.hashAsInt(fileName, FrostConstants.MPQ_HASH_FILE_KEY);
            context.getLogger().debug("Calculated key for fileName=" + fileName + " as " + key);
            if (blockTableEntry.isKeyAdjusted()) {
                key = (key + blockTableEntry.getBlockOffset()) ^ blockTableEntry.getFileSize();
            }
            context.getLogger().debug("Adjusted key to: " + key);
        }
        if (isComplete) {
            context.getLogger().info("Extracting: " + fileName);
            context.getLogger().debug("File has " + blockTableEntry.getFileSize() + " bytes");
            ByteBuffer fileBytes = ByteBuffer.allocate(blockTableEntry.getFileSize());
            int sectorCount = 0;
            for (FileSectorEntry sector : newSectors) {
                context.getLogger().debug("Reading a sector...");
                sector.readRawData(sectorCount);
                sector.addBytes(fileBytes);
                context.getLogger().debug("Expanded to " + fileBytes.array().length + " bytes");
                sectorCount++;
            }
            return fileBytes.array();
        } else {
            read(this.reader, key);
            if (!isComplete) {
                // For some reason, coulnd't read completely... avoid infinite loop.
                context.getErrorHandler().handleCriticalError("Could not " +
                        "complete file data entry for " + fileName);
                return new byte[0];
            }
            return getFileBytes(fileName);
        }
    }

    public int getInitialPosition() {
        return initialPosition;
    }

    public void setInitialPosition(int initialPosition) {
        this.initialPosition = initialPosition;
    }

    public int getArchiveOffset() {
        return archiveOffset;
    }

    public void setArchiveOffset(int archiveOffset) {
        this.archiveOffset = archiveOffset;
    }

    public ArchiveHeader getHeader() {
        return header;
    }

    public void setHeader(ArchiveHeader header) {
        this.header = header;
    }

    public void setBlockTableEntry(BlockTableEntry blockTableEntry) {
        this.blockTableEntry = blockTableEntry;
    }

    public void setHashTableEntry(HashTableEntry hashTableEntry) {
        this.hashTableEntry = hashTableEntry;
    }

    public FrostSecurity getFrostSecurity() {
        return frostSecurity;
    }

    public void setFrostSecurity(FrostSecurity frostSecurity) {
        this.frostSecurity = frostSecurity;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public List<FileSectorEntry> getNewSectors() {
        return newSectors;
    }

    public void setNewSectors(List<FileSectorEntry> newSectors) {
        this.newSectors = newSectors;
    }

    public int getSectorsInFile() {
        return sectorsInFile;
    }

    public void setSectorsInFile(int sectorsInFile) {
        this.sectorsInFile = sectorsInFile;
    }

    public int[] getSectorOffsetTable() {
        return sectorOffsetTable;
    }

    public void setSectorOffsetTable(int[] sectorOffsetTable) {
        this.sectorOffsetTable = sectorOffsetTable;
    }

    public MpqContext getContext() {
        return context;
    }

    public void setContext(MpqContext context) {
        this.context = context;
    }

    public BinaryReader getReader() {
        return reader;
    }

    public void setReader(BinaryReader reader) {
        this.reader = reader;
    }

    public void readAll() {
        for (int i = 0; i < newSectors.size(); i++) {
            FileSectorEntry sector = newSectors.get(i);
            if (!sector.isRead()) {
                sector.readRawData(i);
            }
        }
    }

    /**
     * Converts this object into a byte array which represents
     * the same state as the object.
     *
     * @return  Byte array of object.
     */
    @Override
    public byte[] toBytes() {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(blockTableEntry.getBlockSize());
            context.getLogger().debug("Allocated " + blockTableEntry.getBlockSize() + " bytes for file " + fileName
                    + "(pos=" + blockTableEntry.getBlockOffset() +"-" + (blockTableEntry.getBlockOffset() +
                    blockTableEntry.getBlockSize()) + ")");
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            for (int i = 0; i < originalOffsetTable.length; i++) {
                buffer.putInt(originalOffsetTable[i]);
            }

            for (int i = 0; i < newSectors.size(); i++) {
                FileSectorEntry sector = newSectors.get(i);
                if (!sector.isRead()) {
                    sector.readRawData(i);
                }
                buffer.put(sector.toBytes());
            }

            return buffer.array();
        } catch (BufferOverflowException ex) {
            ex.printStackTrace();
            context.getErrorHandler().handleCriticalError
                    ("Ran out of space for file");
            return new byte[0];
        }
    }

    public int getByteSize() {
        if(blockTableEntry.isSingleUnit()) {
            return blockTableEntry.getBlockSize();
        } else {
            return blockTableEntry.getBlockSize() + (4 * (sectorsInFile+1));
        }
    }

    public void setOffsetPosition(int newFileOffset) {
        initialPosition = newFileOffset;
        for(FileSectorEntry sector : newSectors) {
            sector.setOffset(newFileOffset);
        }
    }
}
