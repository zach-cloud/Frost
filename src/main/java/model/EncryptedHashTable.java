package model;

import interfaces.IReadable;
import interfaces.IByteSerializable;
import reader.BinaryReader;
import settings.MpqContext;
import storm.StormSecurity;

import java.io.IOException;

import static storm.StormConstants.BLOCK_TABLE_ENCRYPTION_KEY;
import static storm.StormConstants.HASH_TABLE_ENCRYPTION_KEY;

public class EncryptedHashTable implements IReadable, IByteSerializable {

    private static final int BYTES_PER_BLOCK_TABLE_ENTRY = 16;

    private byte[] encryptedData;

    private int entryCount;

    private MpqContext context;

    public EncryptedHashTable (int entryCount, MpqContext context) {
        this.entryCount = entryCount;
        this.context = context;
    }

    public void encrypt(byte[] array, StormSecurity security) {
        this.encryptedData = security.encryptBytes(array, HASH_TABLE_ENCRYPTION_KEY);
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

    /**
     * Converts this object into a byte array which represents
     * the same state as the object.
     *
     * @return  Byte array of object.
     */
    @Override
    public byte[] toBytes() {
        return encryptedData;
    }

    public byte[] getEncryptedData() {
        return encryptedData;
    }

    public static int getBytesPerBlockTableEntry() {
        return BYTES_PER_BLOCK_TABLE_ENTRY;
    }

    public void setEncryptedData(byte[] encryptedData) {
        this.encryptedData = encryptedData;
    }

    public int getEntryCount() {
        return entryCount;
    }

    public void setEntryCount(int entryCount) {
        this.entryCount = entryCount;
    }

    public MpqContext getContext() {
        return context;
    }

    public void setContext(MpqContext context) {
        this.context = context;
    }
}
