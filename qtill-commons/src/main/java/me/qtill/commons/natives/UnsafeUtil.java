package me.qtill.commons.natives;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Unsafe工具类
 *
 * @author paranoidq
 * @since 1.0.0
 */
public final class UnsafeUtil {

    private static          Object lock = new Object();
    private static volatile Unsafe unsafeInstance;

    /**
     * 获取Unsafe实例
     *
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    public static Unsafe getUnsafeInstance() throws NoSuchFieldException, IllegalAccessException {
        if (unsafeInstance == null) {
            synchronized (lock) {
                if (unsafeInstance == null) {
                    Field theUnsafeInstance = Unsafe.class.getDeclaredField("theUnsafe");
                    theUnsafeInstance.setAccessible(true);
                    unsafeInstance = (Unsafe) theUnsafeInstance.get(Unsafe.class);
                }
            }
        }
        return unsafeInstance;
    }
}
