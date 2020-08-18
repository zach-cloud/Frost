package model;

import interfaces.IReadable;
import interfaces.IByteSerializable;
import com.github.zachcloud.reader.BinaryReader;
import settings.MpqContext;

public final class StrongSignature implements IReadable, IByteSerializable {

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
        throw new UnsupportedOperationException("Under development");
    }

    /**
     * Converts this object into a byte array which represents
     * the same state as the object.
     *
     * @return Byte array of object.
     */
    @Override
    public byte[] toBytes() {
        throw new UnsupportedOperationException("Under development");
    }
}
