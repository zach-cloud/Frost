package model;

import frost.FrostSecurity;
import helper.ByteHelper;
import interfaces.IReadable;
import interfaces.IByteSerializable;
import reader.BinaryReader;
import settings.MpqContext;

import java.io.IOException;

import static frost.FrostConstants.*;

public class EncryptedHashTable implements IReadable, IByteSerializable {

    private byte[] encryptedData;

    private int entryCount;

    private MpqContext context;

    public EncryptedHashTable (int entryCount, MpqContext context) {
        this.entryCount = entryCount;
        this.context = context;
    }

    public void encrypt(byte[] array, FrostSecurity security) {
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
            int size = entryCount * BYTES_PER_HASH_TABLE_ENTRY;
            encryptedData = reader.readBytes(size);
            context.getLogger().debug("Read " + size + " bytes as encrypted hash table");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String toString() {
        return ByteHelper.bytesToString(encryptedData);
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
