package model;

import interfaces.IReadable;
import reader.BinaryReader;
import settings.MpqContext;
import storm.StormConstants;

import java.io.IOException;

public class EncryptedBlockTable implements IReadable {

    private byte[] encryptedData;

    private int entryCount;
    private MpqContext context;

    public EncryptedBlockTable (int entryCount, MpqContext context) {
        this.entryCount = entryCount;
        this.context = context;
    }

    public byte[] getEncryptedData() {
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
