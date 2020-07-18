package model;

import interfaces.IReadable;
import reader.BinaryReader;

public class MpqObject implements IReadable {

    private ArchiveHeader archiveHeader;
    private BlockTable blockTable;
    private ExtendedAttributes extendedAttributes;
    private ExtendedBlockTable extendedBlockTable;
    private FileData fileData;
    private HashTable hashTable;
    private ListFile listFile;
    private StrongSignature strongSignature;
    private WeakSignature weakSignature;



    /**
     * Reads from the binary reader into this model object
     *
     * @param reader Binary reader
     */
    @Override
    public void read(BinaryReader reader) {
        // Read header - starts at the beginning of MPQ Archive part
        archiveHeader = new ArchiveHeader();
        archiveHeader.read(reader);
        // Save positions
        int headerStart = archiveHeader.getOffsetStart();
        int headerEnd = reader.getPosition();
        // Calculate some offsets
        int blockTableStart = archiveHeader.getBlockTableOffset() + headerStart;
        long hashTableStart = archiveHeader.getHashTableOffset() + headerStart;
        long extendedBlockTableOffset = archiveHeader.getExtendedBlockTableOffset() + headerStart;
        int hashTableOffsetHigh = archiveHeader.getHashTableOffsetHigh() + headerStart;
        int blockTableOffsetHigh = archiveHeader.getBlockTableOffsetHigh() + headerStart;
        // Read block table, starting at offset
        reader.setPosition(blockTableStart);
        blockTable = new BlockTable();
        blockTable.read(reader);


        fileData = new FileData();
        fileData.read(reader);
        hashTable = new HashTable();
        hashTable.read(reader);

        extendedBlockTable = new ExtendedBlockTable();
        extendedBlockTable.read(reader);
        strongSignature = new StrongSignature();
        strongSignature.read(reader);
    }
}
