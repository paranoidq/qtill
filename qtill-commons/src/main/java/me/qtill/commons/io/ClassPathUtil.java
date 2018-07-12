package me.qtill.commons.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class ClassPathUtil {

    private ClassPathUtil() {
        throw new UnsupportedOperationException("cannot instantiate");
    }

    /**
     * 获取context classloader
     *
     * @return
     */
    public static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * 获取system classloader
     *
     * @return
     */
    public static ClassLoader getSystemClassLoader() {
        return ClassLoader.getSystemClassLoader();
    }

    /**
     * 加载类
     *
     * @param clazz
     * @param classLoader
     * @return
     * @throws ClassNotFoundException
     */
    public static Class<?> loadClass(String clazz, ClassLoader classLoader) throws ClassNotFoundException {
        return classLoader.loadClass(clazz);
    }

    /**
     * 通过context classloader加载类
     *
     * @param clazz
     * @return
     * @throws ClassNotFoundException
     */
    public static Class<?> loadClass(String clazz) throws ClassNotFoundException {
        return getContextClassLoader().loadClass(clazz);
    }


    /**
     * 根据资源相对classpath的path获取资源
     * 返回{@link InputStream}实例
     *
     * @param classpathRelativePath
     * @return
     * @throws IOException
     */
    public static InputStream getResourceAsStream(String classpathRelativePath) throws IOException {
        if (!classpathRelativePath.contains("../")) {
            return getContextClassLoader().getResourceAsStream(classpathRelativePath);
        }
        return getExtendedResourceAsStream(classpathRelativePath);
    }

    /**
     * 根据资源相对classpath的path获取资源
     * 返回{@link URL}实例
     *
     * @param classpathRelativePath
     * @return
     * @throws MalformedURLException
     */
    public static URL getResource(String classpathRelativePath) throws MalformedURLException {
        if (!classpathRelativePath.contains("../")) {
            return getContextClassLoader().getResource(classpathRelativePath);
        }
        return getExtendedResource(classpathRelativePath);
    }

    /**
     * 根据资源相对classpath的path获取资源
     * path包含../上级路径标识
     * 返回{@link InputStream}实例
     *
     * @param classpathRelativePath
     * @return
     * @throws IOException
     */
    private static InputStream getExtendedResourceAsStream(String classpathRelativePath) throws IOException {
        URL url = getAbsoluteUrl(classpathRelativePath);
        return url.openStream();
    }

    /**
     * 根据资源相对classpath的path获取资源
     * path包含../上级路径标识
     * 返回{@link URL}实例
     *
     * @param classpathRelativePath
     * @return
     * @throws MalformedURLException
     */
    private static URL getExtendedResource(String classpathRelativePath) throws MalformedURLException {
        return getAbsoluteUrl(classpathRelativePath);

    }

    /**
     * 根据relativePath获取绝对路径的URL
     *
     * @param classpathRelativePath
     * @return
     * @throws MalformedURLException
     */
    public static URL getAbsoluteUrl(String classpathRelativePath) throws MalformedURLException {
        if (!classpathRelativePath.contains("../")) {
            if (classpathRelativePath.substring(0, 1).equals("/")) {
                classpathRelativePath = classpathRelativePath.substring(1);
            }
            return getContextClassLoader().getResource(classpathRelativePath);
        }
        return new URL(convertRelativePath2AbsolutePath(classpathRelativePath));
    }

    /**
     * 根据
     *
     * @param relativePath
     * @return
     */
    public static String convertRelativePath2AbsolutePath(String relativePath) {
        String classpathAbsolutePath = getClasspathAbsolutePath();
        // 剔除第一个/
        if (relativePath.substring(0, 1).equals("/")) {
            relativePath = relativePath.substring(1);
        }

        // 取出路径中包含../的部分
        String wildcardSubPath = relativePath.substring(0, relativePath.lastIndexOf("../") + 3);
        // 剔除路径中包含../的部分，剩余路径都是常规路径
        relativePath = relativePath.substring(relativePath.lastIndexOf("../") + 3);

        // 统计../出现的次数
        int wildcardCount = 0;
        while (wildcardSubPath.contains("../")) {
            wildcardCount++;
            wildcardSubPath = wildcardSubPath.substring(3);
        }

        // 将../合并到路径中，得到规范化路径
        for (int i = 0; i < wildcardCount; i++) {
            classpathAbsolutePath = classpathAbsolutePath.substring(0, classpathAbsolutePath.lastIndexOf("/", classpathAbsolutePath.length() - 2) + 1);
        }

        return classpathAbsolutePath + relativePath;
    }

    /**
     * 获取classpath绝对路径
     *
     * @return
     */
    public static String getClasspathAbsolutePath() {
        return getContextClassLoader().getResource("").toString();
    }

    /**
     * 根据url获取绝对路径
     *
     * @param url
     * @return
     */
    public static String getAbsoluteFilePath(URL url) {
        return convertUrl2File(url).getAbsolutePath();
    }

    /**
     * 根据url获取file
     *
     * @param url
     * @return
     */
    public static File convertUrl2File(URL url) {
        String path = url.toString().replace("file:", "");
        File file = null;
        if (url != null) {
            file = new File(path);
        }
        return file;
    }

    /**
     * 根据路径获取URL
     *
     * @param path
     * @return
     * @throws MalformedURLException
     */
    public static URL convertPath2URL(String path) throws MalformedURLException {
        return convertFile2URL(new File(path));
    }

    /**
     * 根据文件获取URL
     *
     * @param file
     * @return
     * @throws MalformedURLException
     */
    public static URL convertFile2URL(File file) throws MalformedURLException {
        URL url = null;
        if (file != null) {
            url = file.toURI().toURL();
        }
        return url;
    }


}
