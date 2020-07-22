package model;

import interfaces.IReadable;
import reader.BinaryReader;

import java.io.IOException;

@Deprecated
public class FileSector implements IReadable {

    private int size;
    private int normalSize;
    private int position;
    private BlockTableEntry entry;
    private BinaryReader reader;
    private byte flags;
    private byte[] data;

    /*
    	40h: IMA ADPCM mono
	80h: IMA ADPCM stereo
	01h: Huffman encoded
	02h: Deflated (see ZLib)
	08h: Imploded (see PKWare Data Compression Library)
	10h: BZip2 compressed (see BZip2)
     */

    private static final byte IMAADPCM_STEREO = 0x40;
    private static final byte IMAADPCM_MONO = -0x80;
    private static final byte HUFFMAN = 0x01;
    private static final byte DEFLATED = 0x02;
    private static final byte IMPLODE = 0x08;
    private static final byte BZIP2 = 0x10;


    public FileSector(int size, int normalSize, int position, BlockTableEntry entry) {
        this.entry = entry;
        this.size = size;
        this.normalSize = normalSize;
        this.position = position;
    }

    /**
     * Reads from the binary reader into this model object
     *
     * @param reader Binary reader
     */
    @Override
    public void read(BinaryReader reader) {
        try {
            if(entry.isCompressed()) {
                flags = reader.readByte();

                boolean stereoCompressed = (flags & IMAADPCM_STEREO) != 0;
                boolean monoCompressed = (flags & IMAADPCM_MONO) != 0;
                boolean huffmanCompressed = (flags & HUFFMAN) != 0;
                boolean deflatedCompressed = (flags & DEFLATED) != 0;
                boolean implodedCompressed = (flags & IMPLODE) != 0;
                boolean bzip2Compressed = (flags & BZIP2) != 0;

                byte[] compressedData = reader.readBytes(size - 1);

                System.out.println(new String(compressedData));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void readLater(BinaryReader reader) {
        this.reader = reader;
    }

    public void readSelf() {
        this.read(reader);
    }
}
