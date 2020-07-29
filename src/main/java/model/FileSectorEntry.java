package model;

import compression.CompressionHandler;
import storm.StormSecurity;
import helper.ByteHelper;
import reader.BinaryReader;
import settings.MpqContext;

import java.io.IOException;
import java.nio.ByteBuffer;

public class FileSectorEntry {

    private int start; // Start byte, offset by the start of file data entry
    private int end; // End byte, offset by the start of file data entry
    private int offset; // Offset  to file data entry
    private int compressedSize; // Compressed size of sector
    private int realSize; // File size of sector (decompressed)
    private int key; // Key, if known; else -1
    private boolean compressed; // True if compressed, false if not.
    private boolean encrypted; // True if encrypted, false if not
    private byte[] rawData; // Stores raw bytes of sector, can be compressed
    private byte[] fileData; // Stores decompressed/decrypted data. Essentially a cache for multiple extractions.

    private BinaryReader reader;
    private CompressionHandler compressionHandler;

    private boolean isRead; // Set to true when we read raw datA
    private boolean isProcessed; // Set to true when we finish decompressing/decrypting/etc.

    private MpqContext context;
    private StormSecurity stormSecurity;

    /**
     * Creates a new Sector entry containing data from a file sector
     *
     * @param start Start byte, offset by the start of file data entry
     * @param end   End byte, offset by the start of file data entry
     * @param offset    Offset to start of file data entry
     * @param compressedSize Compressed size of sector
     * @param realSize  File size of sector (decompressed)
     * @param compressed    True if compressed, false if not.
     * @param encrypted True if encrypted, false if not
     * @param reader    File reader linked to mpq file
     * @param context   Mpq context
     */
    public FileSectorEntry(int start, int end, int offset, int compressedSize,
                           int realSize, boolean compressed, boolean encrypted, int key,
                           BinaryReader reader, MpqContext context, StormSecurity stormSecurity) {
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
        this.compressionHandler = new CompressionHandler(context);
        this.stormSecurity = stormSecurity;
    }

    /**
     * Performs reading of bytes
     */
    public void readRawData(int sectorCount) {
        if(isRead) {
            // We already read rawData so there's no need to do it again
            return;
        }
        try {
            // We only read rawData when requested to save memory!
            reader.setPosition(start+offset);
            rawData = reader.readBytes((end+offset) - (start+offset));
            if(encrypted) {
                context.getLogger().debug("Decrypting file data with key=" + key+sectorCount);
                rawData = stormSecurity.decryptBytes(rawData, key + sectorCount);
            }
            if(rawData.length != compressedSize) {
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
        if(isProcessed) {
            fileBytes.put(fileData);
        } else {
            if(compressed) {
                byte compressionFlag = rawData[0];
                fileData = ByteHelper.trimBytes(rawData, 1);
                fileData = compressionHandler.decompress(fileData, compressionFlag, realSize);
            } else {
                fileData = rawData;
            }
            fileBytes.put(fileData);
            isProcessed = true;
        }
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

    public CompressionHandler getCompressionHandler() {
        return compressionHandler;
    }

    public void setCompressionHandler(CompressionHandler compressionHandler) {
        this.compressionHandler = compressionHandler;
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

    public StormSecurity getStormSecurity() {
        return stormSecurity;
    }

    public void setStormSecurity(StormSecurity stormSecurity) {
        this.stormSecurity = stormSecurity;
    }
}
