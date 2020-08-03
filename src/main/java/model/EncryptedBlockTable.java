package model;

import interfaces.IReadable;
import interfaces.IByteSerializable;
import reader.BinaryReader;
import settings.MpqContext;
import storm.StormConstants;
import storm.StormSecurity;

import java.io.IOException;

import static storm.StormConstants.BLOCK_TABLE_ENCRYPTION_KEY;

public class EncryptedBlockTable implements IReadable, IByteSerializable {

    private byte[] encryptedData;

    private int entryCount;
    private MpqContext context;

    public EncryptedBlockTable (int entryCount, MpqContext context) {
        this.entryCount = entryCount;
        this.context = context;
    }

    public void encrypt(byte[] array, StormSecurity security) {
        this.encryptedData = security.encryptBytes(array, BLOCK_TABLE_ENCRYPTION_KEY);
    }

    public byte[] getEncryptedData() {
        return encryptedData;
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

    /**
     * Reads from the binary reader into this model object
     *
     * @param reader Binary reader
     */
    @Override
    public void read(BinaryReader reader) {
        try {
            int size = entryCount * StormConstants.BYTES_PER_BLOCK_TABLE_ENTRY;
            encryptedData = reader.readBytes(size);
            context.getLogger().debug("Read " + size + " bytes as encrypted block table");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
