package model;

import interfaces.IReadable;
import interfaces.IByteSerializable;
import reader.BinaryReader;
import settings.MpqContext;

public final class ExtendedAttributes implements IReadable, IByteSerializable {

    private MpqContext context;

    public ExtendedAttributes(MpqContext context) {
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

    /**
     * Converts this object into a byte array which represents
     * the same state as the object.
     *
     * @return  Byte array of object.
     */
    @Override
    public byte[] toBytes() {
        return new byte[0];
    }
}
