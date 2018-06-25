package me.qtill.commons.text;

import org.apache.commons.lang.CharUtils;

/**
 * 字符工具类
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class CharUtil {

    /**
     * 是否是ascii字符，包括：字母、数字、特殊字符
     * @param ch
     * @return
     */
    public boolean isAscii(char ch) {
        return CharUtils.isAscii(ch);
    }

    /**
     * 是否是ascii字母或数字
     * @param ch
     * @return true if between 48 and 57 or 65 and 90 or 97 and 122 inclusive
     */
    public boolean isAsciiAlphanumeric(char ch) {
        return CharUtils.isAsciiAlphanumeric(ch);
    }

    /**
     * 是否是ascii字母
     * @param ch
     * @return
     */
    public boolean isAsciiAlpha(char ch) {
        return CharUtils.isAsciiAlpha(ch);
    }

    /**
     * 是否是ascii数字
     * @param ch
     * @return
     */
    public boolean isAsciiNumeric(char ch) {
        return CharUtils.isAsciiNumeric(ch);
    }

    /**
     * 是否是ascii小写字母
     * @param ch
     * @return
     */
    public boolean isAsciiAlphaLower(char ch) {
        return CharUtils.isAsciiAlphaLower(ch);
    }

    /**
     * 是否是ascii大写字母
     * @param ch
     * @return
     */
    public boolean isAsciiAlphaUpper(char ch) {
        return CharUtils.isAsciiAlphaUpper(ch);
    }

    /**
     * 转为小写
     * @param ch
     * @return
     */
    public char toLowerCase(char ch) {
        return Character.toLowerCase(ch);
    }

    /**
     * 转为大写
     * @param ch
     * @return
     */
    public char toUpperCase(char ch) {
        return Character.toUpperCase(ch);
    }

}
