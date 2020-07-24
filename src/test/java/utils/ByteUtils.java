package utils;

public class ByteUtils {

    public static String bytesToString(byte[] ar) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < ar.length; i++) {
            builder.append(ar[i]).append(",");
        }
        builder.setLength(builder.length()-1);
        return builder.toString();
    }

    public static byte[] stringToBytes(String s) {
        String[] split = s.split(",");
        byte[] ar = new byte[split.length];
        for(int i = 0; i < split.length; i++) {
            ar[i] = (byte)Integer.parseInt(split[i]);
        }
        return ar;
    }


}
