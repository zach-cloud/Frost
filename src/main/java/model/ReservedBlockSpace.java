package model;

public final class ReservedBlockSpace {

    private int offset;
    private int size;

    public boolean intersects(int position) {
        return (position >= offset && position <= offset + size);
    }

    public ReservedBlockSpace(int offset, int size) {
        this.offset = offset;
        this.size = size;
    }

    public String toString() {
        return "(" + offset + "," + (offset + size) + ")";
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
