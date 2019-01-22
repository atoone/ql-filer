package com.feertech.qfiler;

public class DataUtils {

    private DataUtils() {
        // Private constructor for utils class
    }

    public static String byteHex(byte value) {
        return Integer.toString( value & 0x0ff, 16);
    }

    public static short shortValue(byte[] source, int offset) {
        return (short) ((source[offset+1] & 0x0ff) | ((source[offset] & 0x0ff) << 8));
    }

    public static short shortLE(byte[] source, int offset) {
        return (short) ((source[offset] & 0x0ff) | ((source[offset+1] & 0x0ff) << 8));
    }

    public static String shortHex(int value) {
        return Integer.toString(value & 0x0ffff, 16);
    }

    public static int intValue(byte[] source, int offset) {
        int value = 0;
        for(int i=0; i<4; i++) {
            value = (value << 8) | (source[offset+i] & 0x0ff);
        }
        return value;
    }

    public static void copyBytes(byte[] source, int srcOffset, byte[] dest, int dstOffset, int length) {
        for(int i=0; i<length; i++) {
            dest[dstOffset+i] = source[srcOffset+i];
        }
    }

    public static void setShort(short value, byte[] dest, int offset) {
        dest[offset] = (byte) ((value >> 8) & 0xff);
        dest[offset+1] = (byte) (value & 0xff);
    }

    public static void setInt(int value, byte[] dest, int offset) {
        for(int i=3; i>=0; i--) {
            dest[offset++] = (byte)(value >> (i*8));
        }
    }

    public static void copyBytes(byte[] source, byte[] dest, int offset) {
        for( int i=0; i<source.length; i++ ) {
            dest[offset+i] = source[i];
        }
    }

    public static boolean checkBytes(byte[] array, int offset, int length, byte value) {
        for( int i=offset; i<offset+length; i++) {
            if(array[i] != value) return false;
        }
        return true;
    }

    public static String doHex(byte[] data, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        int end = offset+length;
        boolean first = true;
        while(offset < end) {
            if( !first ) {
                sb.append("\n");
            }
            first = false;
            for(int i=0; i<16; i++) {
                if( i+offset >= data.length) {
                    sb.append("  ");
                }
                else {
                    if ((data[i+offset] & 0x0ff) < 16) sb.append("0");
                    sb.append(Integer.toHexString(data[i + offset] & 0x0FF));
                }
                if( (i & 0x01) == 0x01 ) sb.append(" ");
            }

            for(int i=0; i<16; i++) {
                if( i+offset >= data.length) {
                    sb.append(" ");
                }
                else if( (data[i+offset] & 0x0ff) < 32 ) {
                    sb.append(".");
                }
                else if( (data[i+offset] & 0x0ff) >= 127 ) {
                    sb.append("?");
                }
                else {
                    sb.append((char)data[i+offset]);
                }
            }

            offset += 16;
        }
        return sb.toString();
    }

    public static ChecksumBuilder checksum(byte value) {
        return new ChecksumBuilder().add(value);
    }

    public static ChecksumBuilder checksum(byte[] array, int offset, int length) {
        return new ChecksumBuilder().add(array, offset, length);
    }

    public static class ChecksumBuilder {
        int value;

        ChecksumBuilder() {
            value = 0x0f0f;
        }

        public ChecksumBuilder add(byte value) {
            this.value += value & 0x0FF;
            return this;
        }

        public ChecksumBuilder add(byte[] array, int offset, int length) {
            for(int i=offset; i<offset+length; i++) {
                value += array[i] & 0x0FF;
            }
            return this;
        }

        public ChecksumBuilder add(byte[] array) {
            return add(array, 0, array.length);
        }

        public ChecksumBuilder add(short value) {
            this.value += (value & 0x0ff);
            this.value += ((value >> 8) & 0x0ff);
            return this;
        }

        public short checksum() {
            return (short)(((value >> 8) & 0x0ff) | ((value & 0x0ff) << 8));
        }
    }
}
