package me.qtill.commons.object;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public final class ObjectUtil {

    // 重用Objenesis实例
    private static Objenesis objenesis = new ObjenesisStd(true);

    /**
     * 获取初始化器，通过{@link ObjectInstantiator#newInstance()}构造实例对象
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> ObjectInstantiator<T> newObjectInstantiator(Class<T> clazz) {
        return objenesis.getInstantiatorOf(clazz);
    }

    /**
     * 直接新建一个类的实例，但没有重用，因此效率低于{@link ObjectUtil#newObjectInstantiator(Class)}
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T newInstance(Class<T> clazz) {
        return (T) objenesis.newInstance(clazz);
    }

}