package me.qtill.commons.codec;

import com.google.common.base.Joiner;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;

/**
 * 编解码工具类
 * - base64编解码
 * - urlencode编解码
 * - byte数组-16进制编解码
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class CodecUtil {

    private CodecUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * BASE64编码
     *
     * @param data
     * @return
     */
    public static byte[] encodeBase64(byte[] data) {
        return Base64.encodeBase64(data);
    }

    /**
     * BASE64解码
     *
     * @param data
     * @return
     */
    public static byte[] decodeBase64(byte[] data) {
        return Base64.decodeBase64(data);
    }

    /**
     * BASE64编码, URLSafe
     *
     * @param data
     * @return
     */
    public static byte[] encodeBase64UrlSafe(byte[] data) {
        return Base64.encodeBase64URLSafe(data);
    }

    /**
     * Base64编码, Chuncked
     *
     * @param data
     * @return
     */
    public static byte[] encodeBase64Chuncked(byte[] data) {
        return Base64.encodeBase64Chunked(data);
    }

    /**
     * byte数组转16进制字符串
     *
     * @param data
     * @return
     */
    public static String byteArray2Hex(byte[] data) {
        return String.valueOf(Hex.encodeHex(data));
    }

    /**
     * byte数组转16进制字符串, 包含分隔符
     *
     * @param data
     * @param sp
     * @return
     */
    public static String byteArray2Hex(byte[] data, char sp) throws DecoderException {
        char[] chs = Hex.encodeHex(data);
        StringBuilder sb = new StringBuilder(3 * chs.length - 1);
        for (int i = 0; i < chs.length; i += 2) {
            sb.append(chs[i]).append(chs[i + 1]).append(sp);
        }
        return sb.toString().substring(0, sb.length() - 1);
    }

    /**
     * 16进制字符串转byte数组
     *
     * @param data
     * @return
     * @throws DecoderException
     */
    public static byte[] hex2ByteArray(String data) throws DecoderException {
        return Hex.decodeHex(data.toCharArray());
    }


    /**
     * 16进制字符串转byte数组, 包含分隔符
     *
     * @param data
     * @param sp
     * @return
     * @throws DecoderException
     */
    public static byte[] hex2ByteArray(String data, char sp) throws DecoderException {
        StringBuilder sb = new StringBuilder();
        String[] hexs = data.split(Character.toString(sp), -1);
        String hexStr = Joiner.on("").join(hexs);
        return Hex.decodeHex(hexStr.toCharArray());
    }

    /**
     * URLEncode
     *
     * @param data
     * @param encoding
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String urlEncode(String data, Charset encoding) throws UnsupportedEncodingException {
        return URLEncoder.encode(data, encoding.name());
    }

    /**
     * URLDecode
     *
     * @param data
     * @param encoding
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String urlDecode(String data, Charset encoding) throws UnsupportedEncodingException {
        return URLDecoder.decode(data, encoding.name());
    }


}
