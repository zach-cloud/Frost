package model;

import compression.CompressionHandler;
import encryption.StormSecurity;
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
    private byte[] fileData; // Stores decompressed/decrypted data

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
                // TODO: Fixme
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
            if(encrypted) {
                context.getErrorHandler()
                        .handleCriticalError("Not yet implemented (decrypt)");
                // TODO: Write decryption code
            }
            if(compressed) {
                byte compressionFlag = rawData[0];
                fileData = ByteHelper.trimBytes(rawData, 1);
                fileData = compressionHandler.decompress(fileData, compressionFlag, realSize);
            }
            fileBytes.put(fileData);
            isProcessed = true;
        }
    }
}
