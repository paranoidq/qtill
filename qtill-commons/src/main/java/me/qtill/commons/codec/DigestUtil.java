package me.qtill.commons.codec;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * 摘要工具类
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class DigestUtil {

    private DigestUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * MD5摘要
     *
     * @param data
     * @return byte数组
     */
    public static byte[] md5(byte[] data) {
        return DigestUtils.md5(data);
    }

    /**
     * MD5摘要
     *
     * @param data
     * @return byte数组的16进制串
     */
    public static String md5Hex(byte[] data) {
        return DigestUtils.md5Hex(data);
    }

    /**
     * SHA1摘要
     *
     * @param data
     * @return byte数组
     */
    public static byte[] sha1(byte[] data) {
        return DigestUtils.sha1(data);
    }

    /**
     * SHA1摘要
     *
     * @param data
     * @return byte数组的16进制串
     */
    public static String sha1Hex(byte[] data) {
        return DigestUtils.sha1Hex(data);
    }

    /**
     * SHA256摘要
     *
     * @param data
     * @return byte数组
     */
    public static byte[] sha256(byte[] data) {
        return DigestUtils.sha256(data);
    }

    /**
     * SHA256摘要
     *
     * @param data
     * @return byte数组的16进制串
     */
    public static String sha256Hex(byte[] data) {
        return DigestUtils.sha256Hex(data);
    }


    /**
     * SHA512摘要
     *
     * @param data
     * @return byte数组
     */
    public static byte[] sha512(byte[] data) {
        return DigestUtils.sha512(data);
    }

    /**
     * SHA256摘要
     *
     * @param data
     * @return byte数组的16进制串
     */
    public static String sha512Hex(byte[] data) {
        return DigestUtils.sha512Hex(data);
    }
}
