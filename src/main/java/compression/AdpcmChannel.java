package compression;

public class AdpcmChannel {

    private static final int INITIAL_STEP =0x2C;

    private short state;
    private short step;

    public AdpcmChannel() {
        this.step = INITIAL_STEP;
    }

    public short getState() {
        return state;
    }

    public void setState(short state) {
        this.state = state;
    }

    public short getStep() {
        return step;
    }

    public void setStep(short step) {
        this.step = step;
    }
}
