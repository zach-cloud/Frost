package model;

import frost.FrostSecurity;
import interfaces.IByteSerializable;
import frost.FrostConstants;
import frost.FrostUtility;
import interfaces.IReadable;
import reader.BinaryReader;
import settings.MpqContext;

import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static frost.FrostConstants.*;

public final class MpqObject implements IReadable, IByteSerializable {

    private FrostUtility frostUtility;
    private FrostSecurity frostSecurity;

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

    // Whatever came before the header
    private byte[] preHeader;

    // Archive offsets
    private int blockTableStart;
    private int hashTableStart;
    private long extendedBlockTableOffset;
    private int hashTableOffsetHigh;
    private int blockTableOffsetHigh;
    private int headerStart;

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
        try {
            // Initialize helper components
            this.frostSecurity = new FrostSecurity();
            this.frostUtility = new FrostUtility(frostSecurity, context);

            // Read header - starts at the beginning of MPQ Archive part
            archiveHeader = new ArchiveHeader(context);
            archiveHeader.read(reader);

            // Save positions
            headerStart = archiveHeader.getOffsetStart();
            int headerEnd = reader.getPosition();
            calculateOffsets();

            // Log some diagnostic data to debug.
            context.getLogger().debug("headerStart=" + headerStart + ",headerEnd="
                    + headerEnd + ",blockTableStart=" + blockTableStart + ",hashTableStart="
                    + hashTableStart + ",extendedBlockTableOffset=" + extendedBlockTableOffset
                    + ",hashTableOffsetHigh=" + hashTableOffsetHigh + ",blockTableOffsetHigh="
                    + blockTableOffsetHigh);
            context.getLogger().debug("Header: " + archiveHeader.toString());

            // Read stuff before header
            reader.setPosition(0);
            preHeader = reader.readBytes(headerStart);
            readBlockTable(reader, blockTableStart);
            readHashTable(reader, hashTableStart);
            readFileData(reader);
            extractInternalListfile();
            context.getLogger().info("Successfully read " + fileData.size() + " files.");
            // TODO: Read the rest of this garbage. (extended stuff)
        } catch (Exception ex) {
            ex.printStackTrace();
            context.getErrorHandler().handleCriticalError(
                    "Failed to build MPQ file: " + ex.getMessage());
        }
    }

    private void calculateOffsets() {
        // Calculate some offsets
        blockTableStart = archiveHeader.getBlockTableOffset() + headerStart;
        hashTableStart = archiveHeader.getHashTableOffset() + headerStart;
        extendedBlockTableOffset = archiveHeader.getExtendedBlockTableOffset() + headerStart;
        hashTableOffsetHigh = archiveHeader.getHashTableOffsetHigh() + headerStart;
        blockTableOffsetHigh = archiveHeader.getBlockTableOffsetHigh() + headerStart;
    }

    private void readBlockTable(BinaryReader reader, int blockTableStart) {
        reader.setPosition(blockTableStart);
        this.encryptedBlockTable = new EncryptedBlockTable(archiveHeader.getBlockTableEntries(), context);
        encryptedBlockTable.read(reader);
        this.blockTable = new BlockTable(frostSecurity, encryptedBlockTable, context);
    }

    private void readHashTable(BinaryReader reader, int hashTableStart) {
        reader.setPosition(hashTableStart);
        this.encryptedHashTable = new EncryptedHashTable(archiveHeader.getHashTableEntries(), context);
        encryptedHashTable.read(reader);
        this.hashTable = new HashTable(frostSecurity, encryptedHashTable, context);
    }

    private void readFileData(BinaryReader reader) {
        this.fileData = new ArrayList<>();
        int lastValidEntry = blockTable.getEntries().size();

        for (HashTableEntry hashTableEntry : hashTable.getEntries()) {
            // We only care about entries with contents

            int blockTableIndex = hashTableEntry.getFileBlockIndex() % lastValidEntry;
            if (hashTableEntry.getFileBlockIndex() != FrostConstants.MPQ_HASH_ENTRY_DELETED &&
                    hashTableEntry.getFileBlockIndex() != FrostConstants.MPQ_HASH_ENTRY_EMPTY) {
                // Get associated block table entry
                if (blockTableIndex < blockTable.getEntries().size()
                        && blockTableIndex >= 0) {
                    BlockTableEntry blockTableEntry = blockTable.get(blockTableIndex);
                    if (blockTableEntry.isEncrypted()) {
                        context.getLogger().debug("Encrypted entry will be read later.");
                    }
                    FileDataEntry fileDataEntry = new FileDataEntry(headerStart, frostSecurity,
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
        if (fileExists("(listfile)")) {
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
     * @param fileName File name to check
     * @return True if exists; false if not.
     */
    public boolean fileExists(String fileName) {
        HashTableEntry entry = frostUtility.findEntry
                (hashTable, fileName, FrostConstants.ANY_LANGUAGE, FrostConstants.ANY_PLATFORM);
        return entry != null;
    }

    /**
     * Locates the hash table entry for the file name
     * Throws critical error if it does not exist.
     *
     * @param fileName File name of file
     * @return Hash table entry.
     */
    private HashTableEntry findEntry(String fileName) {
        HashTableEntry entry = frostUtility.findEntry
                (hashTable, fileName, FrostConstants.ANY_LANGUAGE, FrostConstants.ANY_PLATFORM);
        if (entry == null) {
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
     * @param fileName File name to get bytes from
     * @return Byte array of uncompressed file data
     */
    public byte[] getFileBytes(String fileName) {

        try {
            HashTableEntry entry = findEntry(fileName);

            // Discover duplicates
            int count = 0;
            for (FileDataEntry fileDataEntry : fileData) {
                if (fileDataEntry.getHashTableEntry() == entry) {
                    count++;
                }
            }

            if(count == 0) {
                context.getLogger().warn("No file found");
                return new byte[0];
            } else if(count > 1) {
                context.getLogger().warn("Multiple files found");
            }

            ByteBuffer totalBytes = ByteBuffer.allocate(0);
            for (FileDataEntry fileDataEntry : fileData) {
                if (fileDataEntry.getHashTableEntry() == entry) {
                    byte[] bytesToAdd = fileDataEntry.getFileBytes(fileName);
                    if (bytesToAdd.length + ((Buffer) totalBytes).position() > totalBytes.capacity()) {
                        context.getLogger().debug("Reallocating to: " + bytesToAdd.length + ((Buffer) totalBytes).position());
                        totalBytes = reallocate(totalBytes, bytesToAdd.length + ((Buffer) totalBytes).position());
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
     * Converts this object into a byte array which represents
     * the same state as the object.
     *
     * @return Byte array of object.
     */
    @Override
    public byte[] toBytes() {
        rebuild();
        byte[] nextToAdd;
        ByteBuffer archiveBytes = ByteBuffer.allocate(preHeader.length +
                archiveHeader.getArchiveSize());
        archiveBytes.order(ByteOrder.LITTLE_ENDIAN);
        archiveBytes.put(preHeader);
        nextToAdd = archiveHeader.toBytes();
        archiveBytes.put(nextToAdd);
        ((Buffer) archiveBytes).position(blockTableStart);
        nextToAdd = blockTable.toBytes();
        archiveBytes.put(nextToAdd);
        ((Buffer) archiveBytes).position(hashTableStart);
        nextToAdd = hashTable.toBytes();
        archiveBytes.put(nextToAdd);
        for (int i = 0; i < fileData.size(); i++) {
            FileDataEntry fileDataEntry = fileData.get(i);
            ((Buffer) archiveBytes).position(fileDataEntry.getInitialPosition());
            nextToAdd = fileDataEntry.toBytes();
            archiveBytes.put(nextToAdd);
        }
        return archiveBytes.array();
    }

    /**
     * Extracts the specified file to the target file
     *
     * @param fileName MPQ file name to extract
     * @param target   Target file to write to
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
     * @param fileName Filename in archive and on disk.
     */
    public void extractFile(String fileName) {
        extractFile(fileName, new File("out/" + fileName));
    }

    /**
     * Returns theoretical number of files in the archive.
     * This is not necessarily the amount of files that can be known
     * or extracted, simply the number of block table entries.
     *
     * @return Total file count (theoretical)
     */
    public int getFileCount() {
        return blockTable.getEntries().size();
    }

    /**
     * Returns the number of files that we know the name of.
     *
     * @return Number of known files.
     */
    public int getKnownFileCount() {
        return listfile.getEntries().size();
    }

    /**
     * Returns the amount of files that we don't know the name of.
     *
     * @return Number of unknown files
     */
    public int getUnknownFileCount() {
        return blockTable.getEntries().size() - listfile.getEntries().size();
    }

    /**
     * Adds a file to the list of known archive files.
     *
     * @param fileName Filename to add.
     * @return True if the file actually exists; false if not.
     */
    public boolean addFileName(String fileName) {
        if (fileExists(fileName)) {
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
        if (unknownCount > 0) {
            context.getLogger().warn("Unknown files will not be extracted (count=" + unknownCount + ")");
        }
        for (String entry : listfile.getEntries()) {
            extractFile(entry);
        }
    }

    /**
     * Imports this file into the archive.
     * Replaces if the file already exists.
     *
     * @param name File name to import
     * @param data File bytes
     */
    public void importFile(String name, byte[] data) {
        // First, delete file if it exists.
        context.getLogger().debug("Attempting to delete file: " + name);
        delete(name);
        // See if we have empty space to add this file in.
        HashTableEntry blankHashtableEntry = findAvailableHashtableEntry(name);
        if (blankHashtableEntry == null) {
            context.getErrorHandler().handleCriticalError("Not written yet (reallocate hashtable)");
            return;
        }

        // Create a new block table entry for this file.
        // Block offset will be fixed later when we rebuild it.
        BlockTableEntry blockTableEntry = new BlockTableEntry(-1, data.length,
                data.length, 0x80000000 + 0x01000000, context);
        // Set up our new hash table entry
        // We don't need to add it, since it already existed. It was just blank before.
        int blockTableIndex = blockTable.addEntry(blockTableEntry);
        int hashA = frostSecurity.hashAsInt(name, MPQ_HASH_NAME_A);
        int hashB = frostSecurity.hashAsInt(name, MPQ_HASH_NAME_B);
        blankHashtableEntry.setContext(context);
        blankHashtableEntry.setFileBlockIndex(blockTableIndex);
        blankHashtableEntry.setFilePathHashA(hashA);
        blankHashtableEntry.setFilePathHashB(hashB);
        blankHashtableEntry.setLanguage((short) 0);
        blankHashtableEntry.setPlatform((short) 0);
        // Now we'll set up our new File Data Entry

        FileDataEntry dataEntry = new FileDataEntry(headerStart, frostSecurity, -1,
                archiveHeader, blockTableEntry, blankHashtableEntry, context);
        dataEntry.setSingleSectorData(data);
        fileData.add(dataEntry);
        // Increase the block table size since we added a new entry
        // First, push back the hash table if we need to.
        int bytesRequired = FrostConstants.BYTES_PER_BLOCK_TABLE_ENTRY;
        archiveHeader.setBlockTableEntries(archiveHeader.getBlockTableEntries() + 1);

        // Finally, increase the archive size.
        this.archiveHeader.setArchiveSize(1 + archiveHeader.getArchiveSize() + data.length + bytesRequired);
        context.getLogger().debug("Added a single sector entry of " + data.length + " bytes");
        rebuild();
        context.getLogger().debug("Rebuilt MPQ after importing file.");
    }

    public HashTableEntry findAvailableHashtableEntry(String fileName) {
        long initialEntry = (frostSecurity.hashAsInt(fileName, MPQ_HASH_TABLE_OFFSET) & (hashTable.size() - 1));
        int i = (int)initialEntry;
        int count = 0;
        int maxSize = (int)hashTable.size();
        context.getLogger().debug("Starting search at " + i);

        while(count < maxSize) {
            i = i % maxSize;

            HashTableEntry entry = hashTable.get(i);
            if (entry.getFileBlockIndex() == FrostConstants.MPQ_HASH_ENTRY_DELETED ||
                    entry.getFileBlockIndex() == FrostConstants.MPQ_HASH_ENTRY_EMPTY) {
                context.getLogger().debug("Found an available hashtable entry at " + i);
                return entry;
            }
            context.getLogger().debug("Entry " + i + " is unavailable.");
            i++;
            count++;
        }
        return null;
    }

    private void rebuild() {
        // Calculate new values for the header.
        int newHeaderStart = preHeader.length;
        int newBlockTableSize = blockTable.getEntries().size();

        int currentPosition = newHeaderStart + 32;
        // Discover reserved space.
        List<ReservedBlockSpace> reservedBlockSpaces = new ArrayList<>();
        for (FileDataEntry entry : fileData) {
            if (entry.getBlockTableEntry().isEncrypted() && entry.getBlockTableEntry().isKeyAdjusted()) {
                reservedBlockSpaces.add(new ReservedBlockSpace(entry.getBlockTableEntry().getBlockOffset()
                        , entry.getBlockTableEntry().getBlockSize()));
            }
        }

        // Allocate space for each file data entry
        for (FileDataEntry entry : fileData) {

            int newFileOffset = currentPosition;
            int size = entry.getByteSize();
            currentPosition += size;
            // See if this block is reserved
            for(ReservedBlockSpace reservedBlockSpace : reservedBlockSpaces) {
                context.getLogger().debug("Comparing for intersection: " + (currentPosition-newHeaderStart) + " and " + reservedBlockSpace.toString());
                if(reservedBlockSpace.intersects(currentPosition-newHeaderStart+size) || reservedBlockSpace.intersects(currentPosition-newHeaderStart)) {
                    context.getLogger().debug("Skipping " + size + " bytes due to reserved mpq space");
                    currentPosition += size + entry.getBlockTableEntry().getBlockSize();
                    newFileOffset +=size + entry.getBlockTableEntry().getBlockSize();
                }
            }

            if (entry.getBlockTableEntry().isEncrypted() && entry.getBlockTableEntry().isKeyAdjusted()) {
                // We need to leave these alone since the encryption
                // uses the block offset in the key!
                context.getLogger().debug("Skipping reserved space for encrypted entry");
            } else {
                entry.readAll();
                entry.setOffsetPosition(newFileOffset);
                // Block offset factors in the header start automatically.
                entry.getBlockTableEntry().setBlockOffset(newFileOffset - newHeaderStart);
                context.getLogger().debug("Reallocated " + size + " bytes for a block at " + newFileOffset);

            }
        }

        archiveHeader.setHashTableOffset(currentPosition);
        currentPosition += archiveHeader.getHashTableEntries() * FrostConstants.BYTES_PER_HASH_TABLE_ENTRY;
        archiveHeader.setBlockTableOffset(currentPosition);
        archiveHeader.setBlockTableEntries(newBlockTableSize);
        currentPosition += newBlockTableSize * FrostConstants.BYTES_PER_BLOCK_TABLE_ENTRY;
        headerStart = newHeaderStart;
        calculateOffsets();
        archiveHeader.setArchiveSize(currentPosition);
        context.getLogger().debug("Rebuilt MPQ as " + currentPosition + " bytes");
    }

    /**
     * Deletes a file from the archive.
     *
     * @param name Filename to delete
     * @return true if deleted; false if not.
     * returns false if the file didn't exist
     */
    public boolean delete(String name) {
        if (!fileExists(name)) {
            context.getLogger().info("File did not exist in mpq: " + name);
            return false;
        }
        context.getLogger().info("Deleting file from archive: " + name);

        context.getLogger().debug("Locating hash table entry");
        HashTableEntry entry = frostUtility.findEntry
                (hashTable, name, FrostConstants.ANY_LANGUAGE, FrostConstants.ANY_PLATFORM);
        int whichBlockTableEntry = entry.getFileBlockIndex() % blockTable.getEntries().size();
        context.getLogger().debug("Found hash entry as #" + entry.getCallbackId());
        context.getLogger().debug("Associated block table entry is #" + whichBlockTableEntry + "(from " + entry.getFileBlockIndex() + ")");
        int indexToRemove = -1;
        for(int i = 0; i < fileData.size(); i++) {
            FileDataEntry fileDataEntry = fileData.get(i);
            if(fileDataEntry.getHashTableEntry() == entry) {
                context.getLogger().debug("Deleting entry with block index #" + entry.getFileBlockIndex());
                indexToRemove = i;
            }
        }
        if(indexToRemove >= 0) {
            context.getLogger().debug("Removing file data #" + indexToRemove);
            fileData.remove(indexToRemove);
        }
        context.getLogger().debug("Nulling out hash table entry");
        entry.setPlatform((short)-1);
        entry.setLanguage((short)-1);
        entry.setCallbackId(-1);
        entry.setContext(null);
        entry.setFileBlockIndex(-1);
        entry.setFilePathHashA(-1);
        entry.setFilePathHashB(-1);
        context.getLogger().debug("Cleaning out block table entry");
        for(HashTableEntry hashTableEntry : hashTable.getEntries()) {
            int correctedIndex = hashTableEntry.getFileBlockIndex() % blockTable.getEntries().size();
            if(correctedIndex >= indexToRemove) {
                correctedIndex--;
            }
            hashTableEntry.setFileBlockIndex(correctedIndex);
        }
        blockTable.getEntries().remove(indexToRemove);
        context.getLogger().debug("File removes successfully");
        return true;
    }

    /**
     * Gets the Set of known file names in the MPQ
     *
     * @return Set of known file names
     */
    public Set<String> getFileNames() {
        return listfile.getEntries();
    }

    public FrostUtility getFrostUtility() {
        return frostUtility;
    }

    public void setFrostUtility(FrostUtility frostUtility) {
        this.frostUtility = frostUtility;
    }

    public FrostSecurity getFrostSecurity() {
        return frostSecurity;
    }

    public void setFrostSecurity(FrostSecurity frostSecurity) {
        this.frostSecurity = frostSecurity;
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
