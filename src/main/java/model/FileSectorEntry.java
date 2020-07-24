package model;

import reader.BinaryReader;
import settings.MpqContext;

import java.io.IOException;

public class FileSectorEntry {

    private int start;
    private int end;
    private byte[] data;
    private BinaryReader reader;

    private MpqContext context;

    public FileSectorEntry(int start, int end, int offset, BinaryReader reader, MpqContext context) {
        this.start = start + offset;
        this.end = end + offset;
        this.reader = reader;
        this.context = context;
    }

    public void readSelf() {
        try {
            reader.setPosition(start);
            data = reader.readBytes(end - start);
            System.out.println();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
