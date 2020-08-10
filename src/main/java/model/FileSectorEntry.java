package model;

import compression.CompressionHandler;
import interfaces.IByteSerializable;
import frost.FrostSecurity;
import helper.ByteHelper;
import reader.BinaryReader;
import settings.MpqContext;

import java.io.IOException;
import java.nio.ByteBuffer;

public final class FileSectorEntry implements IByteSerializable {

    private int start; // Start byte, offset by the start of file data entry
    private int end; // End byte, offset by the start of file data entry
    private int offset; // Offset  to file data entry
    private int compressedSize; // Compressed size of sector
    private int realSize; // File size of sector (decompressed)
    private int key; // Key, if known; else -1
    private boolean compressed; // True if compressed, false if not.
    private boolean encrypted; // True if encrypted, false if not
    private int sectorCount = -1;
    private byte[] rawData; // Stores raw bytes of sector, can be compressed
    private byte[] fileData; // Stores decompressed/decrypted data. Essentially a cache for multiple extractions.

    private BinaryReader reader;

    private boolean isRead; // Set to true when we read raw datA
    private boolean isProcessed; // Set to true when we finish decompressing/decrypting/etc.

    private MpqContext context;
    private FrostSecurity frostSecurity;

    /**
     * Creates a new Sector entry containing data from a file sector
     *
     * @param start          Start byte, offset by the start of file data entry
     * @param end            End byte, offset by the start of file data entry
     * @param offset         Offset to start of file data entry
     * @param compressedSize Compressed size of sector
     * @param realSize       File size of sector (decompressed)
     * @param compressed     True if compressed, false if not.
     * @param encrypted      True if encrypted, false if not
     * @param reader         File reader linked to mpq file
     * @param context        FrostMpq context
     */
    public FileSectorEntry(int start, int end, int offset, int compressedSize,
                           int realSize, boolean compressed, boolean encrypted, int key,
                           BinaryReader reader, MpqContext context, FrostSecurity frostSecurity) {
        this.start = start;
        this.end = end;
        this.offset = offset;
        this.reader = reader;
        this.context = context;
        this.compressedSize = compressedSize;
        this.realSize = realSize;
        this.compressed = compressed;
        this.encrypted = encrypted;
        this.key = key;
        this.frostSecurity = frostSecurity;
    }

    /**
     * Performs reading of bytes
     */
    public void readRawData(int sectorCount) {
        if (isRead) {
            // We already read rawData so there's no need to do it again
            return;
        }
        try {
            // We only read rawData when requested to save memory!
            reader.setPosition(start + offset);
            rawData = reader.readBytes((end + offset) - (start + offset));
            if (encrypted) {
                context.getLogger().debug("Decrypting file data with key=" + key + sectorCount);
                rawData = frostSecurity.decryptBytes(rawData, key + sectorCount);
                this.sectorCount = sectorCount;
            }
            if (rawData.length != compressedSize) {
                context.getErrorHandler().handleError("Compressed size check failed ("
                        + rawData.length + " vs " + compressedSize + ")");
            }
            isRead = true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Adds file data to byte buffer
     *
     * @param fileBytes Byte buffer to add to
     */
    public void addBytes(ByteBuffer fileBytes) {
        if (isProcessed) {
            fileBytes.put(fileData);
        } else {
            if (compressed) {
                byte compressionFlag = rawData[0];
                fileData = ByteHelper.trimBytes(rawData, 1);
                fileData = context.getCompressionHandler().decompress(fileData, compressionFlag, realSize);
            } else {
                fileData = rawData;
            }
            fileBytes.put(fileData);
            isProcessed = true;
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
        if (!isRead) {
            context.getErrorHandler().handleCriticalError
                    ("Attempted to add bytes before reading them");
        }
        if (encrypted) {
            return frostSecurity.encryptBytes(rawData, key + sectorCount);
        } else {
            return rawData;
        }
    }

    public void setSingleSectorData(byte[] data) {
        this.rawData = data;
        this.fileData = data;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getCompressedSize() {
        return compressedSize;
    }

    public void setCompressedSize(int compressedSize) {
        this.compressedSize = compressedSize;
    }

    public int getRealSize() {
        return realSize;
    }

    public void setRealSize(int realSize) {
        this.realSize = realSize;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public byte[] getRawData() {
        return rawData;
    }

    public void setRawData(byte[] rawData) {
        this.rawData = rawData;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public BinaryReader getReader() {
        return reader;
    }

    public void setReader(BinaryReader reader) {
        this.reader = reader;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isProcessed() {
        return isProcessed;
    }

    public void setProcessed(boolean processed) {
        isProcessed = processed;
    }

    public MpqContext getContext() {
        return context;
    }

    public void setContext(MpqContext context) {
        this.context = context;
    }

    public FrostSecurity getFrostSecurity() {
        return frostSecurity;
    }

    public void setFrostSecurity(FrostSecurity frostSecurity) {
        this.frostSecurity = frostSecurity;
    }

}
