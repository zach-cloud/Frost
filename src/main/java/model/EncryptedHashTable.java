package model;

import interfaces.IReadable;
import reader.BinaryReader;

import java.io.IOException;

public class EncryptedHashTable implements IReadable {

    private static final int BYTES_PER_BLOCK_TABLE_ENTRY = 16;

    private byte[] encryptedData;

    private int entryCount;

    public EncryptedHashTable (int entryCount) {
        this.entryCount = entryCount;
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
            System.out.println("Read " + size + " bytes as encrypted hash table");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public byte[] getEncryptedData() {
        return encryptedData;
    }
}
