package model;

import settings.MpqContext;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Representation of an internal MPQ listfile.
 */
public class ListFile {

    private MpqContext context;
    private Set<String> entries;

    /**
     * Creates a Listfile with no entries
     *
     * @param context   MPQ context
     */
    public ListFile(MpqContext context) {
        this.context = context;
        entries = new HashSet<>();
    }

    /**
     * Creates a Listfile with entries from a newline-delimited String
     *
     * @param source      Newline delimited String
     * @param mpqObject   MPQ object to search for files in
     * @param context     MPQ context
     */
    public ListFile(String source, MpqObject mpqObject, MpqContext context) {
        this.context = context;
        entries = new HashSet<>();

        try {
            Reader inputString = new StringReader(source);
            BufferedReader reader = new BufferedReader(inputString);
            String line;
            while ((line = reader.readLine()) != null) {
                if (mpqObject.fileExists(line)) {
                    entries.add(line);
                } else {
                    context.getLogger().warn("File was located in (listfile)" +
                            " but not in archive: " + line);
                }
            }
        } catch (IOException ex) {
            context.getErrorHandler().handleCriticalError("Could not read internal listfile: "
                    + ex.getMessage());
        }
    }

    public void add(String entry) {
        entries.add(entry);
    }

    public Set<String> getEntries() {
        return entries;
    }

    public void setEntries(Set<String> entries) {
        this.entries = entries;
    }
}
