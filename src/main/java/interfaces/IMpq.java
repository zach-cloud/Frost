package interfaces;

import java.io.File;
import java.util.Set;

public interface IMpq {

    /**
     * Determines if the MPQ archive contains this file.
     *
     * @param fileName File name to check
     * @return True if exists in archive; false if not.
     */
     boolean fileExists(String fileName);

    /**
     * Extracts the file to the base directory, using the same filename.
     *
     * @param fileName File name to extract from archive
     */
     void extractFile(String fileName);

    /**
     * Adds files from the external listfile (disk) into the internal listfile
     *
     * @param externalListfilePath Text file containing external listfile
     */
     void addExternalListfile(File externalListfilePath);

    /**
     * Retrieves a Set of the file names in the archive.
     * This set cannot be modified.
     * <p>
     * If you really want to modify it, you should retrieve the MpqObject
     * and then get the file names from that. Do this at your own risk.
     *
     * @return Unmodifiable set of file names
     */
     Set<String> getFileNames();

    /**
     * Extracts all files that we know the name of
     */
     void extractAllKnown();

    /**
     * Extracts all files that we know the name of, and includes
     * an external listfile.
     *
     * @param externalListfilePath Path to external listfile.
     */
     void extractAllKnown(File externalListfilePath);

    /**
     * Returns theoretical number of files in the archive.
     * This is not necessarily the amount of files that can be known
     * or extracted, simply the number of block table entries.
     *
     * Does not count empty files.
     *
     * @return Total file count (theoretical)
     */
     int getFileCount();

    /**
     * Returns the number of files that we know the name of.
     *
     * Does not count empty files.
     *
     * @return Number of known files.
     */
     int getKnownFileCount();

    /**
     * Returns the amount of files that we don't know the name of.
     *
     * Does not count empty files.
     *
     * @return  Number of unknown files
     */
    int getUnknownFileCount();

    /**
     * Saves this MPQ.
     *
     * @param destination File to save to.
     */
    void save(File destination);

}
