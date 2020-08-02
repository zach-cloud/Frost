package model;

import storm.StormConstants;
import storm.StormSecurity;
import storm.StormUtility;
import interfaces.IReadable;
import reader.BinaryReader;
import settings.MpqContext;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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

    // Optional listfile internal to archive
    private ListFile listfile;

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

        // Log some diagnostic data to debug.
        context.getLogger().debug("headerStart=" + headerStart + ",headerEnd="
                + headerEnd + ",blockTableStart=" + blockTableStart + ",hashTableStart="
                + hashTableStart + ",extendedBlockTableOffset=" + extendedBlockTableOffset
                + ",hashTableOffsetHigh=" + hashTableOffsetHigh + ",blockTableOffsetHigh="
                + blockTableOffsetHigh);

        readBlockTable(reader, blockTableStart);
        readHashTable(reader, hashTableStart);
        readFileData(reader, headerStart);
        context.getLogger().info("Successfully read " + fileData.size() + " files.");
        extractInternalListfile();
        // TODO: Read the rest of this garbage. (extended stuff)
    }

    private void readBlockTable(BinaryReader reader, int blockTableStart) {
        reader.setPosition(blockTableStart);
        this.encryptedBlockTable = new EncryptedBlockTable(archiveHeader.getBlockTableEntries(), context);
        encryptedBlockTable.read(reader);
        this.blockTable = new BlockTable(stormSecurity, encryptedBlockTable, context);
    }

    private void readHashTable(BinaryReader reader, int hashTableStart) {
        reader.setPosition(hashTableStart);
        this.encryptedHashTable = new EncryptedHashTable(archiveHeader.getHashTableEntries(), context);
        encryptedHashTable.read(reader);
        this.hashTable = new HashTable(stormSecurity, encryptedHashTable, context);
    }

    private void readFileData(BinaryReader reader, int headerStart) {
        this.fileData = new ArrayList<>();
        int lastValidEntry = blockTable.getEntries().size();

        for(HashTableEntry hashTableEntry: hashTable.getEntries()) {
            // We only care about entries with contents
            int blockTableIndex = hashTableEntry.getFileBlockIndex() % lastValidEntry;

            if(hashTableEntry.getCallbackId() == 25) {
                System.out.println("Here...");
            }
            if(hashTableEntry.getFileBlockIndex() != StormConstants.MPQ_HASH_ENTRY_DELETED &&
                    hashTableEntry.getFileBlockIndex() != StormConstants.MPQ_HASH_ENTRY_EMPTY) {
                // Get associated block table entry
                if(blockTableIndex < blockTable.getEntries().size()
                        && blockTableIndex >= 0) {
                    BlockTableEntry blockTableEntry = blockTable.get(blockTableIndex);
                    FileDataEntry fileDataEntry = new FileDataEntry(headerStart, stormSecurity,
                            blockTableEntry.getBlockOffset() + headerStart,
                            archiveHeader, blockTableEntry, hashTableEntry, context);
                    context.getLogger().debug("Reading block table entry position=" + hashTableEntry.getFileBlockIndex());
                    fileDataEntry.read(reader);
                    fileData.add(fileDataEntry);
                } else {
                    context.getLogger().warn("Invalid block table entry point: " + hashTableEntry.getFileBlockIndex() + ". Ignored.");
                }
            }
        }
    }

    /**
     * Extracts and reads the internal mpq listfile.
     */
    private void extractInternalListfile() {
        context.getLogger().info("Attempting to read internal listfile...");
        if(fileExists("(listfile)")) {
            // We want to do this in memory.
            String listfileData = new String(getFileBytes("(listfile)"));
            listfile = new ListFile(listfileData, this, context);
            addFileName("(listfile)");
            addFileName("(attributes)");
            context.getLogger().info("Internal listfile had " + listfile.getEntries().size()
                    + " entries out of " + blockTable.getEntries().size() + " total entries");
        } else {
            context.getLogger().info("No internal listfile found in archive");
            listfile = new ListFile(context);
        }
    }

    /**
     * Determines if the file exists.
     *
     * @param fileName  File name to check
     * @return          True if exists; false if not.
     */
    public boolean fileExists(String fileName) {
        HashTableEntry entry = stormUtility.findEntry
                (hashTable, fileName, StormConstants.ANY_LANGUAGE, StormConstants.ANY_PLATFORM);
        return entry != null;
    }

    /**
     * Locates the hash table entry for the file name
     * Throws critical error if it does not exist.
     *
     * @param fileName  File name of file
     * @return          Hash table entry.
     */
    private HashTableEntry findEntry(String fileName) {
        HashTableEntry entry = stormUtility.findEntry
                (hashTable, fileName, StormConstants.ANY_LANGUAGE, StormConstants.ANY_PLATFORM);
        if(entry == null) {
            context.getErrorHandler().handleCriticalError("File does not exist: " + fileName);
        }
        return entry;
    }

    private ByteBuffer reallocate(ByteBuffer original, int length) {
        ByteBuffer newBuffer = ByteBuffer.allocate(length);
        newBuffer.put(original);
        return newBuffer;
    }

    /**
     * Retrieves all bytes for the specified file.
     *
     * @param fileName  File name to get bytes from
     * @return          Byte array of uncompressed file data
     */
    public byte[] getFileBytes(String fileName) {
        try {
            HashTableEntry entry = findEntry(fileName);
            ByteBuffer totalBytes = ByteBuffer.allocate(0);
            for (FileDataEntry fileDataEntry : fileData) {
                if (fileDataEntry.getHashTableEntry() == entry) {
                    byte[] bytesToAdd = fileDataEntry.getFileBytes(fileName);
                    if(bytesToAdd.length + totalBytes.position() > totalBytes.capacity()) {
                        context.getLogger().debug("Reallocating to: " + bytesToAdd.length + totalBytes.position());
                        totalBytes = reallocate(totalBytes, bytesToAdd.length + totalBytes.position());
                    }
                    context.getLogger().debug("Adding " + bytesToAdd.length + " bytes");
                    totalBytes.put(bytesToAdd);
                }
            }
            return totalBytes.array();
        } catch (Exception ex) {
            context.getErrorHandler().handleError("Could not add bytes to " +
                    "final file due to: " + ex.getMessage());
            ex.printStackTrace();
            return new byte[0];
        }
    }

    /**
     * Extracts the specified file to the target file
     *
     * @param fileName  MPQ file name to extract
     * @param target    Target file to write to
     */
    public void extractFile(String fileName, File target) {
        byte[] data = getFileBytes(fileName);
        try {
            context.getFileWriter().write(data, target);
        } catch (IOException ex) {
            context.getErrorHandler().handleCriticalError(ex.getMessage());
        }
        context.getLogger().debug("Wrote to file: " + target.getAbsolutePath());
    }

    /**
     * Extracts the specified filename to the same name on disk.
     *
     * @param fileName  Filename in archive and on disk.
     */
    public void extractFile(String fileName) {
        extractFile(fileName, new File("out/" + fileName));
    }

    /**
     * Returns theoretical number of files in the archive.
     * This is not necessarily the amount of files that can be known
     * or extracted, simply the number of block table entries.
     *
     * @return  Total file count (theoretical)
     */
    public int getFileCount() {
        return blockTable.getEntries().size();
    }

    /**
     * Returns the number of files that we know the name of.
     *
     * @return  Number of known files.
     */
    public int getKnownFileCount() {
        return listfile.getEntries().size();
    }

    /**
     * Returns the amount of files that we don't know the name of.
     *
     * @return  Number of unknown files
     */
    public int getUnknownFileCount() {
        return blockTable.getEntries().size() - listfile.getEntries().size();
    }

    /**
     * Adds a file to the list of known archive files.
     *
     * @param fileName  Filename to add.
     * @return True if the file actually exists; false if not.
     */
    public boolean addFileName(String fileName) {
        if(fileExists(fileName)) {
            listfile.add(fileName);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Extracts all known files.
     */
    public void extractAllKnown() {
        int unknownCount = getUnknownFileCount();
        if(unknownCount > 0) {
            context.getLogger().warn("Unknown files will not be extracted (count=" + unknownCount + ")");
        }
        for(String entry : listfile.getEntries()) {
            extractFile(entry);
        }
    }

    /**
     * Gets the Set of known file names in the MPQ
     *
     * @return  Set of known file names
     */
    public Set<String> getFileNames() {
        return listfile.getEntries();
    }

    public StormUtility getStormUtility() {
        return stormUtility;
    }

    public void setStormUtility(StormUtility stormUtility) {
        this.stormUtility = stormUtility;
    }

    public StormSecurity getStormSecurity() {
        return stormSecurity;
    }

    public void setStormSecurity(StormSecurity stormSecurity) {
        this.stormSecurity = stormSecurity;
    }

    public ArchiveHeader getArchiveHeader() {
        return archiveHeader;
    }

    public void setArchiveHeader(ArchiveHeader archiveHeader) {
        this.archiveHeader = archiveHeader;
    }

    public EncryptedBlockTable getEncryptedBlockTable() {
        return encryptedBlockTable;
    }

    public void setEncryptedBlockTable(EncryptedBlockTable encryptedBlockTable) {
        this.encryptedBlockTable = encryptedBlockTable;
    }

    public EncryptedHashTable getEncryptedHashTable() {
        return encryptedHashTable;
    }

    public void setEncryptedHashTable(EncryptedHashTable encryptedHashTable) {
        this.encryptedHashTable = encryptedHashTable;
    }

    public BlockTable getBlockTable() {
        return blockTable;
    }

    public void setBlockTable(BlockTable blockTable) {
        this.blockTable = blockTable;
    }

    public List<FileDataEntry> getFileData() {
        return fileData;
    }

    public void setFileData(List<FileDataEntry> fileData) {
        this.fileData = fileData;
    }

    public HashTable getHashTable() {
        return hashTable;
    }

    public void setHashTable(HashTable hashTable) {
        this.hashTable = hashTable;
    }

    public ListFile getListfile() {
        return listfile;
    }

    public void setListfile(ListFile listfile) {
        this.listfile = listfile;
    }

    public ExtendedAttributes getExtendedAttributes() {
        return extendedAttributes;
    }

    public void setExtendedAttributes(ExtendedAttributes extendedAttributes) {
        this.extendedAttributes = extendedAttributes;
    }

    public ExtendedBlockTable getExtendedBlockTable() {
        return extendedBlockTable;
    }

    public void setExtendedBlockTable(ExtendedBlockTable extendedBlockTable) {
        this.extendedBlockTable = extendedBlockTable;
    }

    public StrongSignature getStrongSignature() {
        return strongSignature;
    }

    public void setStrongSignature(StrongSignature strongSignature) {
        this.strongSignature = strongSignature;
    }

    public WeakSignature getWeakSignature() {
        return weakSignature;
    }

    public void setWeakSignature(WeakSignature weakSignature) {
        this.weakSignature = weakSignature;
    }

    public MpqContext getContext() {
        return context;
    }

    public void setContext(MpqContext context) {
        this.context = context;
    }
}
