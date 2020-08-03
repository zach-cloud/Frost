package interfaces;

/**
 * Represents a class that can be written out to a file,
 * as a chunk of bytes.
 *
 * This class does not actually write itself to a file, but rather
 * returns itself as a byte array which can then be written.
 */
public interface IByteSerializable {

    /**
     * Converts this object into a byte array which represents
     * the same state as the object.
     *
     * @return  Byte array of object.
     */
    byte[] toBytes();
}
