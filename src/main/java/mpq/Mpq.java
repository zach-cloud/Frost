package mpq;

import model.MpqObject;
import reader.BinaryReader;
import settings.MpqContext;
import settings.MpqLogger;
import settings.MpqSettings;

import java.io.File;

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
     * MPQ Context, storing the logger and settings.
     */
    private MpqContext context;

    /**
     * Creates a Mpq with the specified file
     * Default logger/settings
     *
     * @param origin    MPQ file
     */
    public Mpq(File origin) {
        this(origin, new MpqLogger(), new MpqSettings());
    }

    /**
     * Creates a MPQ with the specified file name.
     * Default logger/settings
     *
     * @param origin    File name of MPQ file
     */
    public Mpq(String origin) {
        this(new File(origin));
    }

    /**
     * Creates a Mpq with the specified file
     * Default settings
     *
     * @param origin    MPQ file
     * @param logger    MPQ Logger
     */
    public Mpq(File origin, MpqLogger logger) {
        this(origin, logger, new MpqSettings());
    }

    /**
     * Creates a MPQ with the specified file name.
     * Default settings
     *
     * @param origin    File name of MPQ file
     * @param logger    MPQ Logger
     */
    public Mpq(String origin, MpqLogger logger) {
        this(new File(origin), logger);
    }

    /**
     * Creates a Mpq with the specified file
     * Default logger
     *
     * @param origin    MPQ file
     * @param settings  MPQ Settings
     */
    public Mpq(File origin, MpqSettings settings) {
        this(origin, new MpqLogger(settings), settings);
    }

    /**
     * Creates a MPQ with the specified file name.
     * Default logger
     *
     * @param origin    File name of MPQ file
     * @param settings  MPQ Settings
     */
    public Mpq(String origin, MpqSettings settings) {
        this(new File(origin), settings);
    }

    /**
     * Creates a Mpq with the specified file
     *
     * @param origin    MPQ file
     * @param logger    MPQ Logger
     * @param settings  MPQ Settings
     */
    public Mpq(File origin, MpqLogger logger, MpqSettings settings) {
        this.origin = origin;
        this.context = new MpqContext(logger, settings);
        verifySourceFileExists();
        readFile();
    }

    /**
     * Creates a MPQ with the specified file name.
     *
     * @param origin    File name of MPQ file
     * @param logger    MPQ Logger
     * @param settings  MPQ Settings
     */
    public Mpq(String origin, MpqLogger logger, MpqSettings settings) {
        this(new File(origin), logger, settings);
    }

    /**
     * Verifies that the file exists.
     * If it does not exist, throws exception.
     */
    private void verifySourceFileExists() {
        if(!origin.exists()) {
            throw new IllegalArgumentException(
                    "File does not exist: " + origin.getAbsolutePath());
        }
    }

    /**
     * Reads MPQ file data into an MPQ model object
     */
    private void readFile() {
        BinaryReader reader = new BinaryReader(origin, context);
        this.mpqObject = new MpqObject(context);
        mpqObject.read(reader);
    }

    /**
     * Determines if the MPQ archive contains this file.
     *
     * @param fileName  File name to check
     * @return          True if exists in archive; false if not.
     */
    public boolean fileExists(String fileName) {
        return mpqObject.fileExists(fileName);
    }

    /**
     * Extracts the file to the base directory, using the same filename.
     *
     * @param fileName  File name to extract from archive
     */
    public void extractFile(String fileName) {
        mpqObject.extractFile(fileName);
    }
}
