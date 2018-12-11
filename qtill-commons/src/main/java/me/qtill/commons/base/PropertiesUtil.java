package me.qtill.commons.base;

/**
 * @author paranoidq
 * @since 1.0.0
 */

import me.qtill.commons.codec.Charsets;
import me.qtill.commons.io.URLResourceUtil;
import me.qtill.commons.number.NumberUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;

/**
 * 关于Properties的工具类
 *
 * 1. 统一风格读取Properties到各种数据类型
 *
 * 2. 从文件或字符串装载Properties
 */
public class PropertiesUtil {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);


    /////////////////// 读取Properties ////////////////////

    public static boolean getBoolean(Properties p, String name, boolean defaultValue) {
        return BooleanUtil.toBoolean(p.getProperty(name), defaultValue);
    }

    public static int getInt(Properties p, String name, int defaultValue) {
        return NumberUtil.toInt(p.getProperty(name), defaultValue);
    }

    public static long getLong(Properties p, String name, long defaultValue) {
        return NumberUtil.toLong(p.getProperty(name), defaultValue);
    }

    public static double getDouble(Properties p, String name, double defaultValue) {
        return NumberUtil.toDouble(p.getProperty(name), defaultValue);
    }

    public static String getString(Properties p, String name, String defaultValue) {
        return p.getProperty(name, defaultValue);
    }

    /////////// 加载Properties////////

    /**
     * 从文件路径加载properties. 默认使用utf-8编码解析文件
     *
     * 路径支持从外部文件或resources文件加载, "file://"或无前缀代表外部文件, "classpath://"代表resources
     *
     * @throws IllegalArgumentException if cannot load correctly
     */
    public static Properties loadFromFile(String generalPath) {
        Properties p = new Properties();
        try (Reader reader = new InputStreamReader(URLResourceUtil.asStream(generalPath), Charsets.UTF_8)) {
            p.load(reader);
        } catch (IOException e) {
            throw new IllegalArgumentException("load propery from " + generalPath + " failed", e);
        }
        return p;
    }

    /**
     * 从字符串内容加载Properties
     *
     * @throws IllegalArgumentException if cannot load correctly
     */
    public static Properties loadFromString(String content) {
        Properties p = new Properties();
        try (Reader reader = new StringReader(content)) {
            p.load(reader);
        } catch (IOException e) {
            throw new IllegalArgumentException("load propery from [" + content + "] failed", e);
        }
        return p;
    }
}

