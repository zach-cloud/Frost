package helper;

public class IntHelper {

    public static int intToUnsignedInt(int original, int mod) {
        long unsignedInt = original & 0x00000000ffffffffL;
        unsignedInt = unsignedInt % mod;
        return (int)unsignedInt;
    }

}
