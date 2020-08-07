package reader;

import org.apache.commons.io.IOUtils;
import settings.MpqContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Helper class for reading binary files (such as MPQs)
 */
public final class BinaryReader {

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
            initialize(fileData, byteOrder);
        } catch (IOException ex) {
            throw new RuntimeException("Cannot read file: " + origin.getAbsolutePath());
        }
    }

    public BinaryReader(byte[] source, ByteOrder byteOrder) {
        initialize(source, byteOrder);
    }

    private void initialize(byte[] source, ByteOrder byteOrder) {
        stream = ByteBuffer.allocate(source.length);
        stream.put(source);
        stream.order(byteOrder);
        // Ignore this warning. It's not redundant. Code in JAR form fails without cast!!
        ((Buffer) stream).flip();
        this.undoStack = new LinkedList<>();
        this.byteOrder = byteOrder;
    }

    public BinaryReader(byte[] source) {
        initialize(source, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Makes a new binary reader with file contents
     * Assumes little endian
     *
     * @param origin File to set for reader
     */
    public BinaryReader(File origin) {
        this(origin, ByteOrder.LITTLE_ENDIAN);
    }

    public void goTo(String flag) throws IOException {
        int size = flag.length(); // Length of the word to search for
        byte currentByte;   // Current byte read from buffer
        byte currentLetterByte; // Current byte in the
        int currentLetter = 0;  // Current match length that we have found so far
        int position = stream.position();   // How many we have read
        int correctPosition = 0; // What position we will return
        int maxLength = stream.array().length;
        // Scan entire buffer from current position
        while (stream.position() < maxLength) {
            position++;
            currentByte = readByte();
            currentLetterByte = (byte) flag.charAt(currentLetter);
            if (currentByte == currentLetterByte) {
                currentLetter++;
            } else {
                if (currentLetter > 0) {
                    for (int i = 0; i < currentLetter; i++) {
                        // Correctly handle undo stack.
                        undo();
                    }
                }
                currentLetter = 0;
            }
            if(currentLetter == size) {
                // We found a match. Reset and track this match.
                correctPosition = stream.position() - size;
                currentLetter = 0;
            }
        }
        for (int i = 0; i < currentLetter; i++) {
            // Correctly handle undo stack.
            undo();
        }
        // Go to correct location
        goTo(correctPosition);
    }

    public void goTo(int flag) {
        stream.position(flag);
    }

    /**
     * Undoes the last operation (shifts position back)
     */
    public void undo() {
        if (undoStack.size() == 0) {
            throw new RuntimeException("Attempted to undo a non-existent operation");
        }
        int adjustment = undoStack.remove(undoStack.size() - 1);
        stream.position(stream.position() - adjustment);
    }

    /**
     * Adds the last operation to stack
     * Resets byte count of last operation
     */
    private void adjustStack() {
        if (lastReadBytes != 0) {
            undoStack.add(lastReadBytes);
            if (undoStack.size() > 100) {
                undoStack.remove(0);
            }
        }
        lastReadBytes = 0;
    }

    private byte[] readBytesInternal(int count) throws IOException {
        byte[] collected = new byte[count];
        for (int i = 0; i < count; i++) {
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
     * @param length Character count to read
     * @return String data
     */
    public String readString(int length) throws IOException {
        //System.out.println("Reading string (" + length + ")");
        adjustStack();
        byte[] collected = readBytesInternal(length);
        return new String(collected);
    }

    /**
     * Reads a String until it finds a null terminator.
     *
     * @return String data
     */
    public String readString() throws IOException {
        //System.out.println("Reading string (until 0x00)");
        adjustStack();
        List<Byte> bytes = new ArrayList<>();
        byte current = 0;
        do {
            current = readByteInternal();
            if (current != 0x00) {
                bytes.add(current);
            }
        } while (current != 0x00);
        byte[] bytesArray = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            bytesArray[i] = bytes.get(i);
        }
        return new String(bytesArray);
    }

    /**
     * Reads a single byte.
     * For internal use, increments the read bytes count
     *
     * @return 1 byte
     */
    private byte readByteInternal() throws IOException {
        lastReadBytes++;
        byte b = stream.get();
        //System.out.println("Read byte: " + String.format("%02X ", b));
        return b;
    }

    /**
     * Reads a single byte.
     *
     * @return 1 byte
     */
    public byte readByte() throws IOException {
        //System.out.println("Reading byte (1)");
        adjustStack();
        return readByteInternal();
    }

    /**
     * Reads a 8 byte Unsigned Int.
     * Little endian.
     *
     * @return Uin64
     */
    public long readLong() throws IOException {
        //System.out.println("Reading long (8)");
        adjustStack();
        byte[] collected = readBytesInternal(8);
        return java.nio.ByteBuffer.wrap(collected).order(byteOrder).getLong();
    }

    /**
     * Reads a 2 byte Unsigned Int.
     * Little endian.
     *
     * @return Uint16
     */
    public int readShort() throws IOException {
        //System.out.println("Reading short (2)");
        adjustStack();
        byte[] collected = readBytesInternal(2);
        return java.nio.ByteBuffer.wrap(collected).order(byteOrder).getShort();
    }

    /**
     * Reads a 4 byte Unsigned Int.
     * Little endian.
     *
     * @return Uint with byte size X
     */
    public int readInt() throws IOException {
        //System.out.println("Reading int (4)");
        adjustStack();
        byte[] collected = readBytesInternal(4);
        return java.nio.ByteBuffer.wrap(collected).order(byteOrder).getInt();
    }

    /**
     * Reads a 4-byte Real.
     * Little endian.
     *
     * @return Real value
     */
    public double readReal() throws IOException {
        //System.out.println("Reading real (4)");
        adjustStack();
        byte[] collected = new byte[4];
        for (int i = 0; i < 4; i++) {
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

    public int getSize() {
        return stream.array().length;
    }
}
