/*
 */
package dk.cintix.sms.io;

/**
 *
 * @author migo
 */
public class Binary {

    /**
     * longToByte
     *
     * @param value
     *
     * @return
     */
    public static byte[] longToByte(long value) {
        byte[] unsignedInt = new byte[4];
        unsignedInt[0] = (byte) (value & 0xFF);
        unsignedInt[1] = (byte) ((value >> 8) & 0xFF);
        unsignedInt[2] = (byte) ((value >> 16) & 0xFF);
        unsignedInt[3] = (byte) ((value >> 24) & 0xFF);
        return unsignedInt;

    }

    /**
     * bytesToLong
     *
     * @param unsignedInt
     *
     * @return
     */
    public static long bytesToLong(byte[] unsignedInt) {
        return ((unsignedInt[3] & 0xFF) << 24) | ((unsignedInt[2] & 0xFF) << 16) | ((unsignedInt[1] & 0xFF) << 8) | unsignedInt[0] & 0xFF;

    }
}
