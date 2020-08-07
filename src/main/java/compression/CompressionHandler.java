package compression;

import interfaces.IGenericCompression;
import settings.MpqContext;

/**
 * Handles compression types based off compression flags.
 */
public final class CompressionHandler {

    /* Compression flag constants */
    private static final byte IMAADPCM_STEREO = 0x40;
    private static final byte IMAADPCM_MONO = -0x80;
    private static final byte HUFFMAN = 0x01;
    private static final byte DEFLATED = 0x02;
    private static final byte IMPLODE = 0x08;
    private static final byte BZIP2 = 0x10;

    private IGenericCompression deflationCompression;
    private IGenericCompression stereoCompression;
    private IGenericCompression monoCompression;
    private IGenericCompression implodeCompression;
    private MpqContext context;

    public CompressionHandler(MpqContext context) {
        this.deflationCompression = new DeflationCompression();
        this.stereoCompression = new AdpcmCompression(2);
        this.monoCompression = new AdpcmCompression(1);
        this.implodeCompression = new ImplodeCompression();
        this.context = context;
    }

    private byte[] applyGenericDecompress(byte[] data, IGenericCompression whichCompression,
                                          String compressionName, int desiredSize) {
        context.getLogger().debug
                ("Applying " + compressionName + " to origin data (src = " +
                        data.length + " dest = " + desiredSize + ")");
        byte[] tmp = new byte[desiredSize];
        tmp = whichCompression.undo(data, tmp);
        context.getLogger().debug(compressionName + " OK (size = " + tmp.length + ")");
        return tmp;
    }

    public byte[] decompress(byte[] data, int compressionFlag, int desiredSize) {
        boolean bzip2Compressed = (compressionFlag & BZIP2) != 0;
        boolean implodedCompressed = (compressionFlag & IMPLODE) != 0;
        boolean deflatedCompressed = (compressionFlag & DEFLATED) != 0;
        boolean huffmanCompressed = (compressionFlag & HUFFMAN) != 0;
        boolean stereoCompressed = (compressionFlag & IMAADPCM_STEREO) != 0;
        boolean monoCompressed = (compressionFlag & IMAADPCM_MONO) != 0;

        if(bzip2Compressed) {
            context.getErrorHandler().
                    handleCriticalError("Not yet written (Bzip)");
        }
        if(implodedCompressed) {
            data = applyGenericDecompress(data, implodeCompression,
                    "Implode", desiredSize);
        }
        if(deflatedCompressed) {
            data = applyGenericDecompress(data, deflationCompression,
                    "Inflate", desiredSize);
        }
        if(huffmanCompressed) {
            context.getErrorHandler().
                    handleCriticalError("Not yet written (Huffman)");
        }
        if(stereoCompressed) {
            data = applyGenericDecompress(data, stereoCompression,
                    "Stereo", desiredSize);
        }
        if(monoCompressed) {
            data = applyGenericDecompress(data, monoCompression,
                    "Mono", desiredSize);
        }

        return data;
    }

    public byte[] compress(byte[] data, int compressionFlag) {
        context.getErrorHandler().handleCriticalError("Not implemented (compress)");
        return null;
    }
}
