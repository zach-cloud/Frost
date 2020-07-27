package model;

import interfaces.IReadable;
import reader.BinaryReader;
import settings.MpqContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListFile {

    private MpqContext context;
    private List<String> entries;

    public ListFile(MpqContext context) {
        this.context = context;
        entries = new ArrayList<>();
    }

    public void parseListfile(String fileContents, String delimiter) {
        Collections.addAll(entries, fileContents.split(delimiter));
    }

}
