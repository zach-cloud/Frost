package interfaces;

import reader.BinaryReader;

public interface IReadable {

    /**
     * Reads from the binary reader into this model object
     *
     * @param reader    Binary reader
     */
    void read(BinaryReader reader);
}
