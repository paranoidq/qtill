package me.qtill.commons.collection;

import com.google.common.collect.ObjectArrays;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import me.qtill.commons.base.annotation.Nullable;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public final class ArrayUtil {

    /**
     * 传入类型与大小创建数组.
     *
     * Array.newInstance()的性能并不差，内部实现是一个native方法
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] newArray(Class<T> type, int length) {
        return (T[]) Array.newInstance(type, length);
    }

    /**
     * 从collection转为Array, 以 list.toArray(new String[0]); 最快 不需要创建list.size()的数组.
     *
     * 本函数等价于list.toArray(new String[0]); 用户也可以直接用后者.
     *
     * https://shipilev.net/blog/2016/arrays-wisdom-ancients/
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(Collection<T> col, Class<T> type) {
        return col.toArray((T[]) Array.newInstance(type, 0));
    }

    /**
     * Swaps the two specified elements in the specified array.
     */
    private static void swap(Object[] arr, int i, int j) {
        Object tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    /**
     * 将传入的数组乱序
     */
    public static <T> T[] shuffle(T[] array) {
        if (array != null && array.length > 1) {
            ThreadLocalRandom rand = ThreadLocalRandom.current();
            return shuffle(array, rand);
        } else {
            return array;
        }
    }

    /**
     * 将传入的数组乱序
     */
    public static <T> T[] shuffle(T[] array, Random random) {
        if (array != null && array.length > 1 && random != null) {
            for (int i = array.length; i > 1; i--) {
                swap(array, i - 1, random.nextInt(i));
            }
        }
        return array;
    }

    /**
     * 添加元素到数组头.
     */
    public static <T> T[] concat(@Nullable T element, T[] array) {
        return ObjectArrays.concat(element, array);
    }

    /**
     * 添加元素到数组末尾.
     */
    public static <T> T[] concat(T[] array, @Nullable T element) {
        return ObjectArrays.concat(array, element);
    }

    ////////////////// guava Array 转换为底层为原子类型的List ///////////

    /**
     * 原版将数组转换为List.
     *
     * 注意转换后的List不能写入, 否则抛出UnsupportedOperationException
     *
     * @see java.util.Arrays#asList(Object...)
     */
    public static <T> List<T> asList(T... a) {
        return Arrays.asList(a);
    }

    /**
     * Arrays.asList()的加强版, 返回一个底层为原始类型int的List
     *
     * 与保存Integer相比节约空间，同时只在读取数据时AutoBoxing.
     *
     * @see java.util.Arrays#asList(Object...)
     * @see com.google.common.primitives.Ints#asList(int...)
     */
    public static List<Integer> intAsList(int... backingArray) {
        return Ints.asList(backingArray);
    }

    /**
     * Arrays.asList()的加强版, 返回一个底层为原始类型long的List
     *
     * 与保存Long相比节约空间，同时只在读取数据时AutoBoxing.
     *
     * @see java.util.Arrays#asList(Object...)
     * @see com.google.common.primitives.Longs#asList(long...)
     */
    public static List<Long> longAsList(long... backingArray) {
        return Longs.asList(backingArray);
    }

    /**
     * Arrays.asList()的加强版, 返回一个底层为原始类型double的List
     *
     * 与保存Double相比节约空间，同时也避免了AutoBoxing.
     *
     * @see java.util.Arrays#asList(Object...)
     * @see com.google.common.primitives.Doubles#asList(double...)
     */
    public static List<Double> doubleAsList(double... backingArray) {
        return Doubles.asList(backingArray);
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
