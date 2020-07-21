package model;

public class HashTableEntry {

    private int filePathHashA;
    private int filePathHashB;
    private short language;
    private short platform;
    private int fileBlockIndex;

    /*
    00h: int32 FilePathHashA : The hash of the file path, using method A.
04h: int32 FilePathHashB : The hash of the file path, using method B.
08h: int16 Language : The language of the file. This is a Windows LANGID data type, and uses the same values. 0 indicates the default language (American English), or that the file is language-neutral.
0Ah: int8 Platform : The platform the file is used for. 0 indicates the default platform. No other values have been observed.
0Ch: int32 FileBlockIndex : If the hash table entry is valid, this is the index into the block table of the file. Otherwise, one of the following two values:
	FFFFFFFFh: Hash table entry is empty, and has always been empty. Terminates searches for a given file.
	FFFFFFFEh: Hash table entry is empty, but was valid at some point (in other words, the file was deleted). Does not terminate searches for a given file.

     */

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

    public HashTableEntry(int filePathHashA, int filePathHashB, short language, short platform, int fileBlockIndex) {
        this.filePathHashA = filePathHashA;
        this.filePathHashB = filePathHashB;
        this.language = language;
        this.platform = platform;
        this.fileBlockIndex = fileBlockIndex;
    }
}
