package compression;

import settings.MpqContext;

public class CompressionHandler {

    private static final byte IMAADPCM_STEREO = 0x40;
    private static final byte IMAADPCM_MONO = -0x80;
    private static final byte HUFFMAN = 0x01;
    private static final byte DEFLATED = 0x02;
    private static final byte IMPLODE = 0x08;
    private static final byte BZIP2 = 0x10;

    private DeflationCompression deflationCompression;
    private MpqContext context;

    public CompressionHandler(MpqContext context) {
        this.deflationCompression = new DeflationCompression();
        this.context = context;
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
            context.getErrorHandler().
                    handleCriticalError("Not yet written (Implode)");
        }
        if(deflatedCompressed) {
            context.getLogger().debug
                    ("Applying inflation to origin data (src = " +
                            data.length + " dest = " + desiredSize + ")");
            byte[] tmp = new byte[desiredSize];
            tmp = deflationCompression.undo(data, tmp);
            data = tmp;
            context.getLogger().debug("Inflation OK (size = " + data.length + ")");
        }
        if(huffmanCompressed) {
            context.getErrorHandler().
                    handleCriticalError("Not yet written (Huffman)");
        }
        if(stereoCompressed) {
            context.getErrorHandler().
                    handleCriticalError("Not yet written (Stereo)");
        }
        if(monoCompressed) {
            context.getErrorHandler().
                    handleCriticalError("Not yet written (Mono)");
        }

        return data;
    }
}
