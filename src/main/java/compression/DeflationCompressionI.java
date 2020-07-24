package compression;

import com.jcraft.jzlib.Deflater;
import com.jcraft.jzlib.Inflater;
import interfaces.IGenericCompression;

/**
 * Represents a Deflation or Inflation compression type.
 * Can perform both operations.
 */
public class DeflationCompressionI implements IGenericCompression {

    private Inflater inflater;
    private Deflater deflater;

    /**
     * Initializes the compression type
     */
    public DeflationCompressionI() {
        this.inflater = new Inflater();
        this.deflater = new Deflater();
    }

    /**
     * Inflates src into dest.
     *
     * @param src   Source bytes (compressed)
     * @param dest  Byte array of the size of the uncompressed data
     * @return      Filled destination array. You can also simply use dest after running method.
     */
    public byte[] undo(byte[] src, byte[] dest) {
        inflater.init();
        inflater.setInput(src);
        inflater.setOutput(dest);
        inflater.inflate(0);
        inflater.end();
        return dest;
    }

    /**
     * Applies this compression type.
     *
     * @param src Source (uncompressed) data
     * @return Compressed data
     */
    @Override
    public byte[] apply(byte[] src) {
        return new byte[0];
    }
}
