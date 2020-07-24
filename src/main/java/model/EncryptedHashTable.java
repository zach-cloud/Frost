package model;

import interfaces.IReadable;
import reader.BinaryReader;
import settings.MpqContext;

import java.io.IOException;

public class EncryptedHashTable implements IReadable {

    private static final int BYTES_PER_BLOCK_TABLE_ENTRY = 16;

    private byte[] encryptedData;

    private int entryCount;

    private MpqContext context;

    public EncryptedHashTable (int entryCount, MpqContext context) {
        this.entryCount = entryCount;
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
            int size = entryCount * BYTES_PER_BLOCK_TABLE_ENTRY;
            encryptedData = reader.readBytes(size);
            context.getLogger().debug("Read " + size + " bytes as encrypted hash table");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public byte[] getEncryptedData() {
        return encryptedData;
    }
}
