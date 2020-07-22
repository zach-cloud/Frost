package mpq;

import model.MpqObject;
import org.apache.commons.io.IOUtils;
import reader.BinaryReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Represents an MPQ archive that can be read, extracted, and modified.
 */
public class Mpq {

    /**
     * File that is an MPQ archive.
     */
    private File origin;

    /**
     * Interpreted model object
     */
    private MpqObject mpqObject;

    /**
     * Creates a Mpq with the specified file
     *
     * @param origin    MPQ file
     */
    public Mpq(File origin) {
        this.origin = origin;
        verifyFile();
        readFile();
    }

    /**
     * Creates a MPQ with the specified file name.
     *
     * @param origin    File name of MPQ file
     */
    public Mpq(String origin) {
        this(new File(origin));
    }

    /**
     * Verifies that the file exists.
     * If it does not exist, throws exception.
     */
    private void verifyFile() {
        if(!origin.exists()) {
            throw new IllegalArgumentException(
                    "File does not exist: " + origin.getAbsolutePath());
        }
    }

    private void readFile() {
        BinaryReader reader = new BinaryReader(origin);
        this.mpqObject = new MpqObject();
        mpqObject.read(reader);
    }

    public boolean fileExists(String fileName) {
        return mpqObject.fileExists(fileName);
    }

    public void extractFile(String fileName) {
        mpqObject.extractFile(fileName);
    }
}
