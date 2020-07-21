package reader;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BinaryReader {

    private ByteOrder byteOrder;
    private ByteBuffer stream;
    private List<Integer> undoStack;
    private int lastReadBytes = 0;

    /**
     * Makes a new binary reader with file contents
     *
     * @param origin    File to set for reader
     * @param byteOrder Byte order (little/big endian)
     */
    public BinaryReader(File origin, ByteOrder byteOrder) {
        try {
            byte[] fileData = IOUtils.toByteArray(new FileInputStream(origin));
            stream = ByteBuffer.allocate(fileData.length);
            stream.put(fileData);
            stream.order(byteOrder);
            ((Buffer)stream).flip();
            this.undoStack = new LinkedList<>();
            this.byteOrder = byteOrder;
        } catch (IOException ex) {
            throw new IllegalArgumentException("Cannot read file: " + origin.getAbsolutePath());
        }
    }

    /**
     * Makes a new binary reader with file contents
     * Assumes little endian
     *
     * @param origin    File to set for reader
     */
    public BinaryReader(File origin) {
        this(origin, ByteOrder.LITTLE_ENDIAN);
    }

    public void goTo(String flag) throws IOException {
        int size = flag.length();
        byte currentByte = 0;
        byte currentLetterByte = 0;
        int currentLetter = 0;
        while(currentLetter != size) {
            currentByte = readByte();
            currentLetterByte = (byte)flag.charAt(currentLetter);
            if(currentByte == currentLetterByte) {
                currentLetter++;
            } else {
                if(currentLetter > 0) {
                    for(int i = 0; i < currentLetter; i++) {
                        undo();
                    }
                }
                currentLetter = 0;
            }
        }
        for(int i = 0; i < currentLetter; i++) {
            undo();
        }
    }

    public void goTo(int flag) {
        stream.position(flag);
    }

    /**
     * Undoes the last operation (shifts position back)
     */
    public void undo() {
        if(undoStack.size() == 0) {
            throw new IllegalArgumentException("Attempted to undo a non-existent operation");
        }
        int adjustment = undoStack.remove(undoStack.size()-1);
        stream.position(stream.position() - adjustment);
    }

    /**
     * Adds the last operation to stack
     * Resets byte count of last operation
     */
    private void adjustStack() {
        if(lastReadBytes != 0) {
            undoStack.add(lastReadBytes);
            if(undoStack.size() > 100) {
                undoStack.remove(0);
            }
        }
        lastReadBytes = 0;
    }

    private byte[] readBytesInternal(int count) throws IOException {
        byte[] collected = new byte[count];
        for(int i = 0 ; i < count; i++) {
            collected[i] = readByteInternal();
        }
        return collected;
    }

    public byte[] readBytes(int count) throws IOException {
        adjustStack();
        return readBytesInternal(count);
    }

    /**
     * Reads a number of characters as a String
     *
     * @param length    Character count to read
     * @return          String data
     */
    public String readString(int length) throws IOException {
        System.out.println("Reading string (" + length + ")");
        adjustStack();
        byte[] collected = readBytesInternal(length);
        return new String(collected);
    }

    /**
     * Reads a String until it finds a null terminator.
     *
     * @return          String data
     */
    public String readString() throws IOException {
        System.out.println("Reading string (until 0x00)");
        adjustStack();
        List<Byte> bytes = new ArrayList<>();
        byte current = 0;
        do {
            current = readByteInternal();
            if(current != 0x00) {
                bytes.add(current);
            }
        } while(current != 0x00);
        byte[] bytesArray = new byte[bytes.size()];
        for(int i = 0; i < bytes.size(); i++) {
            bytesArray[i] = bytes.get(i);
        }
        return new String(bytesArray);
    }

    /**
     * Reads a single byte.
     * For internal use, increments the read bytes count
     *
     * @return          1 byte
     */
    private byte readByteInternal() throws IOException {
        lastReadBytes++;
        byte b = stream.get();
        System.out.println("Read byte: " + String.format("%02X ", b));
        return b;
    }

    /**
     * Reads a single byte.
     *
     * @return          1 byte
     */
    public byte readByte() throws IOException {
        System.out.println("Reading byte (1)");
        adjustStack();
        return readByteInternal();
    }

    /**
     * Reads a 8 byte Unsigned Int.
     * Little endian.
     *
     * @return          Uin64
     */
    public long readLong()  throws IOException {
        System.out.println("Reading long (8)");
        adjustStack();
        byte[] collected = readBytesInternal(8);
        return java.nio.ByteBuffer.wrap(collected).order(byteOrder).getLong();
    }

    /**
     * Reads a 2 byte Unsigned Int.
     * Little endian.
     *
     * @return          Uint16
     */
    public int readShort()  throws IOException {
        System.out.println("Reading short (2)");
        adjustStack();
        byte[] collected = readBytesInternal(2);
        return java.nio.ByteBuffer.wrap(collected).order(byteOrder).getShort();
    }

    /**
     * Reads a 4 byte Unsigned Int.
     * Little endian.
     *
     * @return          Uint with byte size X
     */
    public int readInt() throws IOException {
        System.out.println("Reading int (4)");
        adjustStack();
        byte[] collected = readBytesInternal(4);
        return java.nio.ByteBuffer.wrap(collected).order(byteOrder).getInt();
    }

    /**
     * Reads a 4-byte Real.
     * Little endian.
     *
     * @return          Real value
     */
    public double readReal() throws IOException {
        System.out.println("Reading real (4)");
        adjustStack();
        byte[] collected = new byte[4];
        for(int i = 0 ; i < 4; i++) {
            collected[i] = readByteInternal();
        }
        return ByteBuffer.wrap(collected).order(byteOrder).getFloat();
    }

    public int getPosition() {
        return stream.position();
    }

    public void setPosition(int start) {
        stream.position(start);
    }
}
