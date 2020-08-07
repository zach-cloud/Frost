package io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileWriter {

    /**
     * Writes bytes to file.
     *
     * @param data Data to print to file
     * @param file File to write to
     * @throws IOException If file cannot be written to.
     */
    public void write(byte[] data, File file) throws IOException {
        if (file.exists()) {
            file.delete();
        }
        file.mkdirs();
        file.delete();

        FileOutputStream writer = new FileOutputStream(file);
        writer.write(data);
        writer.flush();
        writer.close();
    }
}
