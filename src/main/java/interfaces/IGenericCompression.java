package interfaces;

public interface IGenericCompression {

    /**
     * Applies this compression type.
     *
     * @param src   Source (uncompressed) data
     * @return      Compressed data
     */
    byte[] apply(byte[] src);

    /**
     * Reverses this compression type.
     *
     * @param src   Source (compressed) data
     * @param dest  Array to write compressed data into
     *              Depending on what compression type this is,
     *              the array may need to be instantiated to the
     *              size of the uncompressed data!
     * @return      Uncompressed data
     */
    byte[] undo(byte[] src, byte[] dest);

}
