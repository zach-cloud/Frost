package compression;

import compression.exploder.Exploder;
import interfaces.IGenericCompression;

/**
 * Wrapper for Exploder class.
 */
public final class ImplodeCompression implements IGenericCompression {

    /**
     * Applies this compression type.
     *
     * @param src Source (uncompressed) data
     * @return Compressed data
     */
    @Override
    public byte[] apply(byte[] src) {
        // don't care about this
        return new byte[0];
    }

    /**
     * Reverses this compression type.
     *
     * @param src  Source (compressed) data
     * @param dest Array to write compressed data into
     *             Depending on what compression type this is,
     *             the array may need to be instantiated to the
     *             size of the uncompressed data!
     * @return Uncompressed data
     */
    @Override
    public byte[] undo(byte[] src, byte[] dest) {
        Exploder.pkexplode(src, dest);
        return dest;
    }
}
