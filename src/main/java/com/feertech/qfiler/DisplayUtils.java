package com.feertech.qfiler;

public class DisplayUtils {

    private static final float KB = 1024f;

    private static final String[] shortSizes = {"b","Kb","Mb","Gb","Tb"};
    private static final String[] longSizes = {"bytes", "Kilobytes", "Megabytes", "Gigabytes", "Terrabytes"};

    private DisplayUtils() {
        // Private constructor for utility class
    }

    public static String padTo(String text, int length) {
        StringBuilder sb = new StringBuilder(text);
        while(sb.length() < length) {
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String paddedHex(int value, int digits) {
        String num = Integer.toHexString(value);
        if( num.length() > digits ) {
            return num.substring(num.length() - digits);
        }
        while(num.length() < digits) {
            num = "0"+num;
        }
        return num;
    }

    public static String dataLength(long length, boolean shortForm) {
        float size = 1;
        for(int i=0; i<shortSizes.length-1; i++) {
            if( length < size * KB ) {
                if( length % size == 0){
                    return String.format("%.0f ", length / size)+ (shortForm? shortSizes[i] :  longSizes[i]);
                }
                return String.format("%.1f ", length / size)+ (shortForm? shortSizes[i] :  longSizes[i]);
            }
            size *= KB;
        }

        return String.format("%.1f", length / size) +
                (shortForm ? shortSizes[shortSizes.length-1] : " "+longSizes[longSizes.length-1] );
    }
}
