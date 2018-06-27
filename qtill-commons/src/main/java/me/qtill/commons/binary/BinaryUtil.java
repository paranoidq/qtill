package me.qtill.commons.binary;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Longs;
import me.qtill.commons.text.StringUtil;
import org.apache.commons.codec.binary.Hex;

/**
 * 二进制工具类
 *
 * @author paranoidq
 * @since 1.0.0
 */
public final class BinaryUtil {

    private BinaryUtil() {
        throw new UnsupportedOperationException();
    }


    /**
     * 获取运算数指定位置的值
     * e.g., 0000 1011 获取其第0位的值为1, 第2位为1, 第2位的值为0
     *
     * @param source
     * @param pos    0 <= pos < 8, 0表示最低位，7表示最高位
     * @return
     */
    public static byte getBitValue(byte source, int pos) {
        Preconditions.checkArgument(pos >= 0 && pos < 8, "pos it out of index(0-7): " + pos);
        return (byte) ((source >> pos) & 0x1);
    }

    /**
     * 将运算数的指定位置置1
     *
     * @param source
     * @param pos    0 <= pos < 8, 0表示最低位，7表示最高位
     * @return
     */
    public static byte setBit(byte source, int pos) {
        Preconditions.checkArgument(pos >= 0 && pos < 8, "pos it out of index(0-7): " + pos);
        byte mask = (byte) (1 << pos);
        return (byte) (source | mask);
    }

    /**
     * 将运算数的指定位置置0
     * 先reverse，然后将指定位置置1，然后在reverse
     *
     * @param source
     * @param pos
     * @return
     */
    public static byte unsetBit(byte source, int pos) {
        Preconditions.checkArgument(pos >= 0 && pos < 8, "pos it out of index(0-7): " + pos);
        return reverseAll(setBit(reverseAll(source), pos));
    }


    /**
     * 检查指定位是否为1
     *
     * @param source
     * @param pos
     * @return
     */
    public static boolean isBitSet(byte source, int pos) {
        Preconditions.checkArgument(pos >= 0 && pos < 8, "pos it out of index(0-7): " + pos);
        byte s = (byte) (source >>> pos);
        return (source & 0x01) == 1;
    }

    /**
     * 将运算数某一位取反
     *
     * @param source
     * @param pos
     * @return
     */
    public static byte reverseBitValue(byte source, int pos) {
        Preconditions.checkArgument(pos >= 0 && pos < 8, "pos it out of index(0-7): " + pos);
        byte mask = (byte) (1 << pos);
        return (byte) (source ^ mask);
    }

    /**
     * 将所有位取反
     *
     * @param source
     * @return
     */
    public static byte reverseAll(byte source) {
        return (byte) (0xff ^ source);
    }

    /**
     * 计算有多少1
     *
     * @param b
     * @return
     */
    public static int count(byte b) {
        return Integer.bitCount(b);
    }

    /**
     * byte数组转为四字节整数
     * byte[0]表示高位, byte[4]表示低位
     *
     * @param b
     * @return
     */
    public static int byteArrayToInt(byte[] bytes) {
        Preconditions.checkArgument(bytes != null && bytes.length == 4, "byteArray is invalid, must be size of 4");
        return bytes[3] & 0xFF |
            (bytes[2] & 0xFF) << 8 |
            (bytes[1] & 0xFF) << 16 |
            (bytes[0] & 0xFF) << 24;
    }

    /**
     * 整形四字节数转换为byte数组
     * byte[0]表示高位, byte[4]表示低位
     *
     * @param intValue
     * @return
     */
    public static byte[] intToByteArray(int intValue) {
        return new byte[]{
            (byte) ((intValue >> 24) & 0xFF),
            (byte) ((intValue >> 16) & 0xFF),
            (byte) ((intValue >> 8) & 0xFF),
            (byte) (intValue & 0xFF)
        };
    }

    /**
     * 长整型8字节数转为byte数组
     *
     * @param longValue
     * @return
     */
    public static byte[] longToByteArray(long longValue) {
        return Longs.toByteArray(longValue);
    }

    /**
     * byte数组转为长整型8字节数
     *
     * @param bytes
     * @return
     */
    public static long byteArrayToLong(byte[] bytes) {
        return Longs.fromByteArray(bytes);
    }

    /**
     * 以16进制形式打印byte数组，默认为小写
     *
     * @param bytes
     * @return
     */
    public static String toHexString(byte[] bytes) {
        return toHexString(bytes, true);
    }

    /**
     * 以16进制形式打印byte数组
     *
     * @param bytes
     * @param toLowerCase 指定大小写，true为小写(默认)，false为大写
     * @return
     */
    public static String toHexString(byte[] bytes, boolean toLowerCase) {
        return Hex.encodeHexString(bytes, toLowerCase);
    }

    /**
     * 以16进制形式打印byte数组，并添加分隔符
     *
     * @param bytes
     * @param sp    分隔符
     * @return
     */
    public static String toHexString(byte[] bytes, char sp) {
        char[] chars = Hex.encodeHex(bytes);
        return StringUtil.join(chars, sp);
    }

    /**
     * 打印byte数组的每个bit
     * 格式：00000001
     *
     * @param b
     * @return
     */
    public static String toBitString(byte b) {
        StringBuilder sb = new StringBuilder(8) ;
        for (int i = 7; i >= 0; i--) {
            sb.append((byte) ((b >> i) & 0x1));
        }
        return sb.toString();
    }

}
