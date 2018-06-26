package me.qtill.commons.clazz;


import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.Reflection;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.reflect.ConstructorUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.apache.commons.lang.reflect.MethodUtils;
import org.apache.tools.ant.util.ClasspathUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Class元信息工具类
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class ClassUtil {


    /**
     * 从指定package中加载类
     *
     * @param basePackage
     * @return
     * @throws IOException
     */
    public static List<Class<?>> getClasses(String basePackage) throws IOException {
        List<Class<?>> ret = Lists.newArrayList();
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        ImmutableSet<ClassPath.ClassInfo> classInfoSet = ClassPath.from(loader).getTopLevelClasses(basePackage);
        for (final ClassPath.ClassInfo info : classInfoSet) {
            ret.add(info.load());
        }
        return ret;
    }

    /**
     * 从指定package中加载给定接口的子类
     * <href>http://stackoverflow.com/questions/520328/can-you-find-all-classes-in-a-package-using-reflection</href>
     *
     * @param basePackage
     * @param superType
     * @return
     * @throws IOException
     */
    public static List<Class<?>> getClasses(String basePackage, Class<?> superType) throws IOException {
        List<Class<?>> ret = Lists.newArrayList();
        List<Class<?>> allClasses = getClasses(basePackage);
        for (Class<?> clazz : allClasses) {
            if (superType.isAssignableFrom(clazz)) {
                ret.add(clazz);
            }
        }
        return ret;
    }


    /**
     * 从指定package中加载被指定注解标记的类
     *
     * @param backPackage
     * @param annotationClass
     * @return
     * @throws IOException
     */
    public static List<Class<?>> getClassesByAnnotation(String backPackage, Class<? extends Annotation> annotationClass) throws IOException {
        List<Class<?>> ret = Lists.newArrayList();
        List<Class<?>> allClasses = getClasses(backPackage);
        for (Class<?> clazz : allClasses) {
            if (clazz.isAnnotationPresent(annotationClass)) {
                ret.add(clazz);
            }
        }
        return ret;
    }


    /**
     * 从jar包中加载指定类的子类
     *
     * @param path
     * @param superType
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static List<Class<?>> getClassesFromJar(String path, Class<?> superType) throws IOException, ClassNotFoundException{
        List<Class<?>> classes = Lists.newArrayList();
        JarFile jarFile = new JarFile(path);
        Enumeration<JarEntry> e = jarFile.entries();

        URL[] urls = {new URL("jar:file:" + path + "!/")};
        URLClassLoader cl = URLClassLoader.newInstance(urls);
        while (e.hasMoreElements()) {
            JarEntry jarEntry = e.nextElement();
            if (jarEntry.isDirectory() || !jarEntry.getName().endsWith(".class")) {
                continue;
            }
            // -6, because of .class postfix
            String className = jarEntry.getName().substring(0, jarEntry.getName().length() - 6);
            className = className.replace('/', '.');
            Class c = cl.loadClass(className);
            if (superType.isAssignableFrom(c)) {
                classes.add(c);
            }
        }
        return classes;
    }


    /**
     * 获取Class元信息
     *
     * @param className
     * @param <T>
     * @return
     * @throws ClassNotFoundException
     */
    public static <T> Class<T> getClass(String className) throws ClassNotFoundException {
        return (Class<T>) ClassUtils.getClass(className);
    }

    /**
     * 获取Class元信息, 指定classLoader
     *
     * @param className
     * @param classLoader
     * @param <T>
     * @return
     * @throws ClassNotFoundException
     */
    public static <T> Class<T> getClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
        return (Class<T>) ClassUtils.getClass(classLoader, className);
    }

    /**
     * 获取类的所有接口
     * @param clazz
     * @return
     */
    public static List<Class<?>> getAllInterfaces(Class<?> clazz) {
        return ClassUtils.getAllInterfaces(clazz);
    }

    /**
     * 获取里的所有超类
     * @param clazz
     * @return
     */
    public static List<Class<?>> getAllSuperclasses(Class<?> clazz) {
        return ClassUtils.getAllSuperclasses(clazz);
    }

    /**
     * 获取class所在的包名
     *
     * @param clazz
     * @return
     */
    public static String getPackageName(Class<?> clazz) {
        return ClassUtils.getPackageName(clazz);
    }

    /**
     * 获取class所在的包名
     * @param clazz
     * @return
     */
    public static String getPackageName(String clazz) {
        return ClassUtils.getPackageCanonicalName(clazz);
    }

    /**
     * 获取class所在的包名
     * @param clazz
     * @return
     */
    public static String getPackageCononicalName(Class<?> clazz) {
        return ClassUtils.getPackageCanonicalName(clazz);
    }

    /**
     * 获取class所在的包名
     * @param clazz
     * @return
     */
    public static String getPackageCononicalName(String clazz) {
        return ClassUtils.getPackageCanonicalName(clazz);
    }


    /**
     * 获取Method对象
     *
     * @param clazz
     * @param methodName
     * @param parameterTypes
     * @return
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes) {
        return MethodUtils.getMatchingAccessibleMethod(clazz, methodName, parameterTypes);
    }


    /**
     * 获取Field对象
     *
     * @param clazz
     * @param filedName
     * @return
     */
    public static Field getField(Class<?> clazz, String filedName) {
        return FieldUtils.getField(clazz, filedName);
    }

    /**
     * 获取Field对象, 忽略访问控制
     *
     * @param clazz
     * @param filedName
     * @return
     */
    public static Field getFieldForce(Class<?> clazz, String filedName) {
        return FieldUtils.getField(clazz, filedName, true);
    }

    /**
     * 获取构造函数
     *
     * @param clazz
     * @param paramaeterTypes
     * @return
     */
    public static Constructor getConstructor(Class<?> clazz, Class<?>[] paramaeterTypes) {
        return ConstructorUtils.getMatchingAccessibleConstructor(clazz, paramaeterTypes);
    }



}
