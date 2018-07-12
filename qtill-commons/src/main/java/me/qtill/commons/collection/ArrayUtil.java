package me.qtill.commons.collection;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public final class ArrayUtil {

    private ArrayUtil() {
        throw new UnsupportedOperationException();
    }


/***************************** fill util ***********************************/

    /**
     * 填充byte数组
     *
     * @param source
     * @param fromIndex
     * @param toIndex
     * @param val
     */
    public static void fill(byte[] source, int fromIndex, int toIndex, byte val) {
        Arrays.fill(source, fromIndex, toIndex, val);
    }

    /**
     * 填充int数组
     *
     * @param source
     * @param fromIndex
     * @param toIndex
     * @param val
     */
    public static void fill(int[] source, int fromIndex, int toIndex, int val) {
        Arrays.fill(source, fromIndex, toIndex, val);
    }

    /**
     * 填充long数组
     *
     * @param source
     * @param fromIndex
     * @param toIndex
     * @param val
     */
    public static void fill(long[] source, int fromIndex, int toIndex, long val) {
        Arrays.fill(source, fromIndex, toIndex, val);
    }

    /**
     * 填充float数组
     *
     * @param source
     * @param fromIndex
     * @param toIndex
     * @param val
     */
    public static void fill(float[] source, int fromIndex, int toIndex, float val) {
        Arrays.fill(source, fromIndex, toIndex, val);
    }

    /**
     * 填充boolean数组
     *
     * @param source
     * @param fromIndex
     * @param toIndex
     * @param val
     */
    public static void fill(boolean[] source, int fromIndex, int toIndex, boolean val) {
        Arrays.fill(source, fromIndex, toIndex, val);
    }

    /**
     * 填充boolean数组
     *
     * @param source
     * @param fromIndex
     * @param toIndex
     * @param val
     */
    public static void fill(double[] source, int fromIndex, int toIndex, double val) {
        Arrays.fill(source, fromIndex, toIndex, val);
    }

    /**
     * 填充char数组
     *
     * @param source
     * @param fromIndex
     * @param toIndex
     * @param val
     */
    public static void fill(char[] source, int fromIndex, int toIndex, char val) {
        Arrays.fill(source, fromIndex, toIndex, val);
    }

    /**
     * 填充String数组
     *
     * @param source
     * @param fromIndex
     * @param toIndex
     * @param val
     */
    public static void fill(String[] source, int fromIndex, int toIndex, String val) {
        Arrays.fill(source, fromIndex, toIndex, val);
    }

    /**
     * 填充对象数组
     *
     * @param source
     * @param fromIndex
     * @param toIndex
     * @param val
     */
    public static void fill(Object[] source, int fromIndex, int toIndex, Object val) {
        Arrays.fill(source, fromIndex, toIndex, val);
    }


/***************************** copy util ***********************************/

    /**
     * 新建数组
     *
     * @param componentType
     * @param len
     * @param <T>
     * @return
     */
    public static <T> T[] newInstance(Class<?> componentType, int len) {
        if (componentType == Object.class) {
            return (T[]) new Object[len];
        }
        return (T[]) Array.newInstance(componentType, len);
    }

    /**
     * 将src数组的全部元素拷贝到dest数组
     *
     * @param source
     * @param srcPos
     * @param dest
     * @param desPos
     * @param len
     */
    public static void copy(Object[] source, int srcPos, Object[] dest, int desPos, int len) {
        System.arraycopy(source, srcPos, dest, desPos, len);
    }

    /**
     * 从起始位置拷贝数组
     *
     * @param source
     * @param dest
     * @param len
     */
    public static void copyFromHead(Object[] source, Object[] dest, int len) {
        System.arraycopy(source, 0, dest, 0, len);
    }

    /**
     * 从结束位置拷贝数组
     *
     * @param source
     * @param dest
     * @param len
     */
    public static void copyFromTail(Object[] source, Object[] dest, int len) {
        System.arraycopy(source, source.length - len, dest, dest.length - len, len);
    }


/***************************** empty util ***********************************/

    /**
     * 数组是否为空
     *
     * @param array
     * @return
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length <= 0;
    }

    /**
     * 是否为非空
     *
     * @param array
     * @return
     */
    public static boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }


/***************************** join util ***********************************/

    /**
     * 将src数组合并到dest数组中
     *
     * @param src
     * @param dest
     * @param joinPos join起始的位置，src数组从该位置开始join到dest数组中
     */
    public static void join(Object[] src, Object[] dest, int joinPos) {
        System.arraycopy(src, 0, dest, joinPos, src.length);
    }

    /**
     * 合并两个byte数组
     *
     * @param a1
     * @param a2
     * @return
     */
    public static byte[] join(byte[] a1, byte[] a2) {
        byte[] join = new byte[a1.length + a2.length];
        System.arraycopy(a1, 0, join, 0, a1.length);
        System.arraycopy(a2, 0, join, a1.length, a2.length);
        return join;
    }

    /**
     * 合并两个int数组
     *
     * @param a1
     * @param a2
     * @return
     */
    public static int[] join(int[] a1, int[] a2) {
        int[] join = new int[a1.length + a2.length];
        System.arraycopy(a1, 0, join, 0, a1.length);
        System.arraycopy(a2, 0, join, a1.length, a2.length);
        return join;
    }

    /**
     * 合并两个long数组
     *
     * @param a1
     * @param a2
     * @return
     */
    public static long[] join(long[] a1, long[] a2) {
        long[] join = new long[a1.length + a2.length];
        System.arraycopy(a1, 0, join, 0, a1.length);
        System.arraycopy(a2, 0, join, a1.length, a2.length);
        return join;
    }

    /**
     * 合并两个float数组
     *
     * @param a1
     * @param a2
     * @return
     */
    public static float[] join(float[] a1, float[] a2) {
        float[] join = new float[a1.length + a2.length];
        System.arraycopy(a1, 0, join, 0, a1.length);
        System.arraycopy(a2, 0, join, a1.length, a2.length);
        return join;
    }

    /**
     * 合并两个boolean数组
     *
     * @param a1
     * @param a2
     * @return
     */
    public static boolean[] join(boolean[] a1, boolean[] a2) {
        boolean[] join = new boolean[a1.length + a2.length];
        System.arraycopy(a1, 0, join, 0, a1.length);
        System.arraycopy(a2, 0, join, a1.length, a2.length);
        return join;
    }

    /**
     * 合并两个double数组
     *
     * @param a1
     * @param a2
     * @return
     */
    public static double[] join(double[] a1, double[] a2) {
        double[] join = new double[a1.length + a2.length];
        System.arraycopy(a1, 0, join, 0, a1.length);
        System.arraycopy(a2, 0, join, a1.length, a2.length);
        return join;
    }

    /**
     * 合并两个Object数组
     *
     * @param a1
     * @param a2
     * @return
     */
    public static <T> T[] join(T[] a1, T[] a2) {
        T[] join = (T[]) newInstance(a1.getClass().getComponentType(), a1.length + a2.length);
        System.arraycopy(a1, 0, join, 0, a1.length);
        System.arraycopy(a2, 0, join, a1.length, a2.length);
        return join;
    }

/***************************** toString util ***********************************/

    /**
     * toString
     *
     * @param arary
     * @param <T>
     * @return
     */
    public static <T> String toString(T[] arary) {
        return Arrays.toString(arary);
    }

    /**
     * toString
     *
     * @param arary
     * @return
     */
    public static String toString(int[] arary) {
        return Arrays.toString(arary);
    }

    /**
     * toString
     *
     * @param arary
     * @return
     */
    public static String toString(long[] arary) {
        return Arrays.toString(arary);
    }

    /**
     * toString
     *
     * @param arary
     * @return
     */
    public static String toString(float[] arary) {
        return Arrays.toString(arary);
    }

    /**
     * toString
     *
     * @param arary
     * @return
     */
    public static String toString(char[] arary) {
        return Arrays.toString(arary);
    }

    /**
     * toString
     *
     * @param arary
     * @return
     */
    public static String toString(boolean[] arary) {
        return Arrays.toString(arary);
    }

    /**
     * toString
     *
     * @param arary
     * @return
     */
    public static String toString(double[] arary) {
        return Arrays.toString(arary);
    }


}
