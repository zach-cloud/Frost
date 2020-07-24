package model;

import interfaces.IReadable;
import reader.BinaryReader;
import settings.MpqContext;

public class StrongSignature implements IReadable {

    private MpqContext context;

    public StrongSignature(MpqContext context) {
        this.context = context;
    }

    /**
     * Reads from the binary reader into this model object
     *
     * @param reader Binary reader
     */
    @Override
    public void read(BinaryReader reader) {

    }
}
