package me.qtill.commons.clazz;

import com.google.common.base.Preconditions;
import com.google.common.reflect.Reflection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class ProxyUtil {

    /**
     * 创建JDK动态代理
     *
     * @param interfaceType
     * @param invocationHandler
     * @param <T>
     * @return
     */
    public static <T> T newJdkProxyInstance(Class<T> interfaceType, InvocationHandler invocationHandler) {
        Preconditions.checkNotNull(interfaceType);
        Preconditions.checkArgument(interfaceType.isInterface(), "%s is not an interface", interfaceType);
        Object object = Proxy.newProxyInstance(
            interfaceType.getClassLoader(),
            new Class<?>[]{ interfaceType },
            invocationHandler
        );
        return interfaceType.cast(object);
    }





}
