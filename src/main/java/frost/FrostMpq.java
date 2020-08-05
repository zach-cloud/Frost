package frost;

import interfaces.IFrostMpq;
import interfaces.IByteSerializable;
import model.MpqObject;
import reader.BinaryReader;
import settings.MpqContext;
import settings.MpqLogger;
import settings.MpqSettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * Represents an MPQ archive that can be read, extracted, and modified.
 */
public class FrostMpq implements IFrostMpq, IByteSerializable {

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
     * Creates a FrostMpq with the specified file
     * Default logger/settings
     *
     * @param origin MPQ file
     */
    public FrostMpq(File origin) {
        this(origin, new MpqLogger(), new MpqSettings());
    }

    /**
     * Creates a MPQ with the specified file name.
     * Default logger/settings
     *
     * @param origin File name of MPQ file
     */
    public FrostMpq(String origin) {
        this(new File(origin));
    }

    /**
     * Creates a FrostMpq with the specified file
     * Default settings
     *
     * @param origin MPQ file
     * @param logger MPQ Logger
     */
    public FrostMpq(File origin, MpqLogger logger) {
        this(origin, logger, new MpqSettings());
    }

    /**
     * Creates a MPQ with the specified file name.
     * Default settings
     *
     * @param origin File name of MPQ file
     * @param logger MPQ Logger
     */
    public FrostMpq(String origin, MpqLogger logger) {
        this(new File(origin), logger);
    }

    /**
     * Creates a FrostMpq with the specified file
     * Default logger
     *
     * @param origin   MPQ file
     * @param settings MPQ Settings
     */
    public FrostMpq(File origin, MpqSettings settings) {
        this(origin, new MpqLogger(settings), settings);
    }

    /**
     * Creates a MPQ with the specified file name.
     * Default logger
     *
     * @param origin   File name of MPQ file
     * @param settings MPQ Settings
     */
    public FrostMpq(String origin, MpqSettings settings) {
        this(new File(origin), settings);
    }

    /**
     * Creates a MPQ with the specified file and context.
     *
     * @param origin    Origin file
     * @param context   MPQ context
     */
    public FrostMpq(File origin, MpqContext context) {
        this.origin = origin;
        this.context = context;
        verifySourceFileExists();
        readFile();
    }

    /**
     * Creates a FrostMpq with the specified file
     *
     * @param origin   MPQ file
     * @param logger   MPQ Logger
     * @param settings MPQ Settings
     */
    public FrostMpq(File origin, MpqLogger logger, MpqSettings settings) {
        this(origin, new MpqContext(logger, settings));
    }

    /**
     * Creates a MPQ with the specified file name.
     *
     * @param origin   File name of MPQ file
     * @param logger   MPQ Logger
     * @param settings MPQ Settings
     */
    public FrostMpq(String origin, MpqLogger logger, MpqSettings settings) {
        this(new File(origin), logger, settings);
    }

    /**
     * Verifies that the file exists.
     * If it does not exist, throws exception.
     */
    private void verifySourceFileExists() {
        if (!origin.exists()) {
            throw new IllegalArgumentException(
                    "File does not exist: " + origin.getAbsolutePath());
        }
    }

    /**
     * Converts this object into a byte array which represents
     * the same state as the object.
     *
     * @return  Byte array of object.
     */
    @Override
    public byte[] toBytes() {
        return mpqObject.toBytes();
    }

    /**
     * Saves this MPQ.
     *
     * @param destination File to save to.
     */
    @Override
    public void save(File destination) {
        try {
            context.getFileWriter().write(toBytes(), destination);
        } catch (Exception ex) {
            ex.printStackTrace();
            context.getErrorHandler().handleCriticalError("Could not save file: " + ex.getMessage());
        }
    }

    /**
     * Reads MPQ file data into an MPQ model object
     */
    private void readFile() {
        BinaryReader reader = new BinaryReader(origin);
        this.mpqObject = new MpqObject(context);
        mpqObject.read(reader);
    }

    /**
     * Determines if the MPQ archive contains this file.
     *
     * @param fileName File name to check
     * @return True if exists in archive; false if not.
     */
    @Override
    public boolean fileExists(String fileName) {
        return mpqObject.fileExists(fileName);
    }

    /**
     * Extracts the file to the base directory, using the same filename.
     *
     * @param fileName File name to extract from archive
     */
    @Override
    public void extractFile(String fileName) {
        mpqObject.extractFile(fileName);
    }

    /**
     * Adds files from the external listfile (disk) into the internal listfile
     *
     * @param externalListfilePath  Text file containing external listfile
     */
    @Override
    public void addExternalListfile(File externalListfilePath) {
        if(mpqObject.getUnknownFileCount() == 0) {
            context.getLogger().info("Skipping external listfile since we know all files.");
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(externalListfilePath));
            String line;
            while ((line = br.readLine()) != null) {
                // Only adds if it actually exists
                mpqObject.addFileName(line);
            }
            context.getLogger().debug("Found " + mpqObject.getKnownFileCount() + " files!");
            context.getLogger().debug("Archive contains: " + mpqObject.getFileCount() + " files");
            context.getLogger().info("Unknown file count: " + mpqObject.getUnknownFileCount());
        } catch (IOException ex) {
            context.getErrorHandler().handleCriticalError(ex.getMessage());
        }
    }

    /**
     * Retrieves a Set of the file names in the archive.
     * This set cannot be modified.
     *
     * If you really want to modify it, you should retrieve the MpqObject
     * and then get the file names from that. Do this at your own risk.
     *
     * @return  Unmodifiable set of file names
     */
    @Override
    public Set<String> getFileNames() {
        return Collections.unmodifiableSet(mpqObject.getFileNames());
    }
    /**
     * Extracts all files that we know the name of
     */
    @Override
    public void extractAllKnown() {
        mpqObject.extractAllKnown();
    }

    /**
     * Extracts all files that we know the name of, and includes
     * an external listfile.
     *
     * @param externalListfilePath  Path to external listfile.
     */
    @Override
    public void extractAllKnown(File externalListfilePath) {
        addExternalListfile(externalListfilePath);
        extractAllKnown();
    }

    /**
     * Returns theoretical number of files in the archive.
     * This is not necessarily the amount of files that can be known
     * or extracted, simply the number of block table entries.
     *
     * @return  Total file count (theoretical)
     */
    @Override
    public int getFileCount() {
        return mpqObject.getFileCount();
    }

    /**
     * Returns the number of files that we know the name of.
     *
     * @return  Number of known files.
     */
    @Override
    public int getKnownFileCount() {
        return mpqObject.getKnownFileCount();
    }

    /**
     * Returns the amount of files that we don't know the name of.
     *
     * @return  Number of unknown files
     */
    public int getUnknownFileCount() {
        return mpqObject.getUnknownFileCount();
    }

    /**
     * Imports this file into the archive.
     * Replaces if the file already exists.
     *
     * @param name  File name to import
     * @param data  File bytes
     */
    public void importFile(String name, byte[] data) {
        mpqObject.importFile(name, data);
    }

    /**
     * Deletes a file from the archive.
     *
     * @param name  Filename to delete
     * @return true if deleted; false if not.
     *         returns false if the file didn't exist
     */
    public boolean delete(String name) {
        return mpqObject.delete(name);
    }

    public File getOrigin() {
        return origin;
    }

    public void setOrigin(File origin) {
        this.origin = origin;
    }

    public MpqObject getMpqObject() {
        return mpqObject;
    }

    public void setMpqObject(MpqObject mpqObject) {
        this.mpqObject = mpqObject;
    }

    public MpqContext getContext() {
        return context;
    }

    public void setContext(MpqContext context) {
        this.context = context;
    }
}
