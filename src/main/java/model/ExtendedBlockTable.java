package model;

import interfaces.IReadable;
import reader.BinaryReader;
import settings.MpqContext;

public class ExtendedBlockTable implements IReadable {

    private MpqContext context;

    public ExtendedBlockTable(MpqContext context) {
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
