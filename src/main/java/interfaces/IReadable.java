package interfaces;

import com.github.zachcloud.reader.BinaryReader;

public interface IReadable {

    /**
     * Reads from the binary reader into this model object
     *
     * @param reader    Binary reader
     */
    void read(BinaryReader reader);
}
