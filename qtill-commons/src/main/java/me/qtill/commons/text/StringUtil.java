package me.qtill.commons.text;

import org.apache.commons.lang.StringUtils;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class StringUtil {

    /**
     * 判断字符串是否为空
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        if (str != null) {
            str = str.trim();
        }
        return StringUtils.isEmpty(str);
    }

    /**
     * 判断字符串是否非空
     *
     * @param str
     * @return
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 分割固定格式的字符串
     *
     * @param str
     * @param separator
     * @return
     */
    public static String[] split(String str, String separator) {
        return StringUtils.splitByWholeSeparator(str, separator);
    }


    /**
     * 根据char数组拼接字符串
     *
     * @param chars char数组
     * @param sp 分隔符
     * @return
     */
    public static String join(char[] chars, char sp) {
        StringBuilder sb = new StringBuilder();
        for (char ch : chars) {
            sb.append(ch).append(sp);
        }
        return sb.substring(0, sb.length() - 1);
    }
}
