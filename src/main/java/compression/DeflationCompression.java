package compression;

import com.jcraft.jzlib.Deflater;
import com.jcraft.jzlib.Inflater;

public class DeflationCompression {

    private Inflater inflater;
    private Deflater deflater;

    public DeflationCompression() {
        this.inflater = new Inflater();
        this.deflater = new Deflater();
    }

    public byte[] inflate(byte[] src, byte[] dest) {
        inflater.init();
        inflater.setInput(src);
        inflater.setOutput(dest);
        inflater.inflate(0);
        inflater.end();
        return dest;
    }
}
