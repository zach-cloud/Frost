package model;

import encryption.StormConstants;
import encryption.StormSecurity;
import helper.StormUtility;
import interfaces.IReadable;
import reader.BinaryReader;
import settings.MpqContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MpqObject implements IReadable {

    private StormUtility stormUtility;
    private StormSecurity stormSecurity;

    // Critical MPQ parts
    private ArchiveHeader archiveHeader;
    private EncryptedBlockTable encryptedBlockTable;
    private EncryptedHashTable encryptedHashTable;
    private BlockTable blockTable;
    private List<FileDataEntry> fileData;
    private HashTable hashTable;

    // Optional listfile
    private ListFile listFile;

    // Burning Crusade extensions
    private ExtendedAttributes extendedAttributes;
    private ExtendedBlockTable extendedBlockTable;

    // Signatures
    private StrongSignature strongSignature;
    private WeakSignature weakSignature;

    private MpqContext context;

    public MpqObject(MpqContext context) {
        this.context = context;
    }

    /**
     * Reads from the binary reader into this model object
     *
     * @param reader Binary reader
     */
    @Override
    public void read(BinaryReader reader) {
        // Initialize helper components
        this.stormSecurity = new StormSecurity();
        this.stormUtility = new StormUtility(stormSecurity, context);

        // Read header - starts at the beginning of MPQ Archive part
        archiveHeader = new ArchiveHeader(context);
        archiveHeader.read(reader);

        // Save positions
        int headerStart = archiveHeader.getOffsetStart();
        int headerEnd = reader.getPosition();

        // Calculate some offsets
        int blockTableStart = archiveHeader.getBlockTableOffset() + headerStart;
        int hashTableStart = archiveHeader.getHashTableOffset() + headerStart;
        long extendedBlockTableOffset = archiveHeader.getExtendedBlockTableOffset() + headerStart;
        int hashTableOffsetHigh = archiveHeader.getHashTableOffsetHigh() + headerStart;
        int blockTableOffsetHigh = archiveHeader.getBlockTableOffsetHigh() + headerStart;

        // Read block table, starting at offset
        reader.setPosition(blockTableStart);
        this.encryptedBlockTable = new EncryptedBlockTable(archiveHeader.getBlockTableEntries(), context);
        encryptedBlockTable.read(reader);
        this.blockTable = new BlockTable(stormSecurity, encryptedBlockTable, context);

        // Read the hash table, starting at offset
        reader.setPosition(hashTableStart);
        this.encryptedHashTable = new EncryptedHashTable(archiveHeader.getHashTableEntries(), context);
        encryptedHashTable.read(reader);
        this.hashTable = new HashTable(stormSecurity, encryptedHashTable, context);

        // For each entry in the hash table/block table, read the associated file data
        //stormUtility.findEntry(hashTable, "war3map.j", StormConstants.ANY_LANGUAGE, StormConstants.ANY_PLATFORM);
        this.fileData = new ArrayList<>();
        for(HashTableEntry hashTableEntry: hashTable.getEntries()) {
            // We only care about entries with contents
            if(hashTableEntry.getFileBlockIndex() != StormConstants.MPQ_HASH_ENTRY_DELETED &&
                    hashTableEntry.getFileBlockIndex() != StormConstants.MPQ_HASH_ENTRY_EMPTY) {
                // Get associated block table entry
                BlockTableEntry blockTableEntry = blockTable.get(hashTableEntry.getFileBlockIndex());
                FileDataEntry fileDataEntry = new FileDataEntry(headerStart, stormSecurity, blockTableEntry.getBlockOffset() + headerStart, archiveHeader, blockTableEntry, hashTableEntry, context);
                context.getLogger().debug("Reading block table entry position=" + hashTableEntry.getFileBlockIndex());
                if(hashTableEntry.getFileBlockIndex() == 18) {
                    System.out.println("Going to fail here...");
                }
                fileDataEntry.read(reader);
                fileData.add(fileDataEntry);
            }
        }
        context.getLogger().info("Successfully read " + fileData.size() + " files.");
        // TODO: Read the rest of this garbage. (extended stuff)
    }

    public boolean fileExists(String fileName) {
        HashTableEntry entry = stormUtility.findEntry(hashTable, fileName, StormConstants.ANY_LANGUAGE, StormConstants.ANY_PLATFORM);
        return entry != null;
    }

    public void extractFile(String fileName, File target) {
        HashTableEntry entry = stormUtility.findEntry(hashTable, fileName, StormConstants.ANY_LANGUAGE, StormConstants.ANY_PLATFORM);
        if(entry == null) {
            context.getErrorHandler().handleCriticalError("File does not exist: " + fileName);
        }
        for(FileDataEntry fileDataEntry: fileData) {
            if(fileDataEntry.getHashTableEntry() == entry) {
                fileDataEntry.extract(fileName, target);
            }
        }
    }

    public void extractFile(String fileName) {
        extractFile(fileName, new File("out/" + fileName));
    }

    public int getFileCount() {
        return blockTable.getEntries().size();
    }
}
