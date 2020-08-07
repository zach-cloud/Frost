package deployer;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static settings.GlobalSettings.VERSION;

/**
 * Deploys Frost as a standalone application.
 */
public final class FrostDeployer {

    private final String USER_PATH = System.getProperty("user.dir") + "\\";

    private final String JAR_PATH = USER_PATH + "target\\Frost-" + VERSION + ".jar";
    private final String LICENSE_PATH = USER_PATH + "LICENSE";
    private final String LISTFILE_PATH = USER_PATH + "listfile.txt";
    private final String RUN_CONTENTS = "java -Xmx1g -jar Frost-" + VERSION + ".jar\nread  -n 1 -p \"Press any key to exit\"";

    private final String RELEASES_DESTINATION = USER_PATH + "Releases\\Frost" + VERSION + "\\";
    private final String JAR_DESTINATION = RELEASES_DESTINATION + "Frost-" + VERSION + ".jar";
    private final String LICENSE_DESTINATION = RELEASES_DESTINATION + "LICENSE";
    private final String LISTFILE_DESTINATION = RELEASES_DESTINATION + "listfile.txt";
    private final String RUN_DESTINATION = RELEASES_DESTINATION + "run.bat";

    /**
     * Default constructor. Nothing to initialize.
     */
    public FrostDeployer() {

    }

    /**
     * Copies all JAST-related files into a ReleasePackage directory
     * Assumed that "mvn package" was already executed
     */
    public void run() throws IOException {
        copyFile(JAR_PATH, JAR_DESTINATION);
        copyFile(LICENSE_PATH, LICENSE_DESTINATION);
        copyFile(LISTFILE_PATH, LISTFILE_DESTINATION);
        writeFileContents(RUN_CONTENTS, RUN_DESTINATION);
    }

    /**
     * Writes the string to a file
     *
     * @param contents      Data to write
     * @param destination   Destination file
     */
    private void writeFileContents(String contents, String destination) throws IOException {
        FileUtils.write(new File(destination), contents, Charset.defaultCharset());
        System.out.println("Wrote file: " + destination);
    }

    /**
     * Copies a file
     *
     * @param path          Source file
     * @param destination   Destination file
     */
    private void copyFile(String path, String destination) throws IOException {
        System.out.println("Copying file: " + path + " to: " + destination);
        File file = new File(path);
        if(!file.exists()) {
            throw new IOException("File does not exist: " + path);
        }
        File dest = new File(destination);
        if(dest.exists()) {
            dest.delete();
        }
        dest.mkdirs();
        dest.delete();

        byte[] fileBytes = FileUtils.readFileToByteArray(file);
        FileUtils.writeByteArrayToFile(dest, fileBytes);

    }

    /**
     * Main method to execute and deploy a JAST package.
     *
     * @param args  Ignored
     */
    public static void main(String[] args) throws IOException {
        new FrostDeployer().run();
    }
}
