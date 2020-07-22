package model;

import reader.BinaryReader;

import java.io.IOException;

public class FileSectorEntry {

    private int start;
    private int end;
    private BinaryReader reader;

    public FileSectorEntry(int start, int end, int offset, BinaryReader reader) {
        this.start = start + offset;
        this.end = end + offset;
        this.reader = reader;
    }

    public void readSelf() {
        try {
            reader.setPosition(start);
            byte[] data = reader.readBytes(end - start);
            System.out.println();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
