package model;

import settings.MpqContext;

public class HashTableEntry {

    private int filePathHashA;
    private int filePathHashB;
    private short language;
    private short platform;
    private int fileBlockIndex;
    private MpqContext context;

    public HashTableEntry(int filePathHashA, int filePathHashB, short language, short platform, int fileBlockIndex, MpqContext context) {
        this.filePathHashA = filePathHashA;
        this.filePathHashB = filePathHashB;
        this.language = language;
        this.platform = platform;
        this.fileBlockIndex = fileBlockIndex;
        this.context = context;
    }

    public int getFilePathHashA() {
        return filePathHashA;
    }

    public int getFilePathHashB() {
        return filePathHashB;
    }

    public short getLanguage() {
        return language;
    }

    public short getPlatform() {
        return platform;
    }

    public int getFileBlockIndex() {
        return fileBlockIndex;
    }

    public void setFilePathHashA(int filePathHashA) {
        this.filePathHashA = filePathHashA;
    }

    public void setFilePathHashB(int filePathHashB) {
        this.filePathHashB = filePathHashB;
    }

    public void setLanguage(short language) {
        this.language = language;
    }

    public void setPlatform(short platform) {
        this.platform = platform;
    }

    public void setFileBlockIndex(int fileBlockIndex) {
        this.fileBlockIndex = fileBlockIndex;
    }

    public MpqContext getContext() {
        return context;
    }

    public void setContext(MpqContext context) {
        this.context = context;
    }
}
