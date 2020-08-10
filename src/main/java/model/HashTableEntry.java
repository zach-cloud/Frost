package model;

import helper.MaliciousMPQHelper;
import interfaces.IByteSerializable;
import settings.MpqContext;
import frost.FrostConstants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class HashTableEntry implements IByteSerializable {

    /**
     * Stores hashes of file for comparison
     */
    private int filePathHashA;
    private int filePathHashB;

    /**
     * We really don't care about these.
     */
    private short language;
    private short platform;

    /**
     * Index into the block table to search for
     */
    private int fileBlockIndex;

    /**
     * Array index callback for debugging
     */
    private int callbackId;
    private MpqContext context;

    public HashTableEntry(int filePathHashA, int filePathHashB,
                          short language, short platform,
                          int fileBlockIndex, MpqContext context) {
        this.filePathHashA = filePathHashA;
        this.filePathHashB = filePathHashB;
        this.language = language;
        this.platform = platform;
        this.fileBlockIndex = MaliciousMPQHelper.fixNegativeValue(fileBlockIndex);
        this.context = context;
    }

    /**
     * Converts this object into a byte array which represents
     * the same state as the object.
     *
     * @return Byte array of object.
     */
    @Override
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(FrostConstants.BYTES_PER_HASH_TABLE_ENTRY);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt(filePathHashA);
        buffer.putInt(filePathHashB);
        buffer.putShort(language);
        buffer.putShort(platform);
        buffer.putInt(fileBlockIndex);

        return buffer.array();
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

    public int getCallbackId() {
        return callbackId;
    }

    public void setCallbackId(int callbackId) {
        this.callbackId = callbackId;
    }
}
