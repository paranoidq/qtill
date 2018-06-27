package me.qtill.commons.clazz;

import com.google.common.base.Preconditions;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * 动态代理工具类
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class ProxyUtil {

    private ProxyUtil() {
        throw new UnsupportedOperationException();
    }

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
            new Class<?>[]{interfaceType},
            invocationHandler
        );
        return interfaceType.cast(object);
    }


    /**
     * 创建CGLib动态代理
     *
     * @param clazzType
     * @param methodInterceptor
     * @param <T>
     * @return
     */
    public static <T> T createCglibDynamicProxy(Class<T> clazzType, MethodInterceptor methodInterceptor) {
        Preconditions.checkNotNull(clazzType);
        Enhancer enhancer = new Enhancer();
        enhancer.setCallback(methodInterceptor);
        enhancer.setInterfaces(new Class<?>[]{clazzType});
        return clazzType.cast(enhancer.create());
    }

    /**
     * 创建Javassist动态代理
     * 比较慢，不建议使用javassist动态代理，而应该使用javassist字节码代理
     *
     * @param clazzType
     * @param methodHandler
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T createJavassistDynamicProxy(Class<T> clazzType, MethodHandler methodHandler) throws Exception {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setInterfaces(new Class<?>[]{clazzType});
        Class<T> proxyClass = proxyFactory.createClass();
        T proxy = clazzType.cast(proxyClass.newInstance());
        ((ProxyObject) proxy).setHandler(methodHandler);
        return proxy;
    }

    /**
     * 创建Javassist字节码代理
     * 该方法需要根据业务需求定制，没法做到通用
     *
     * @param clazzType
     * @param ctMethod
     * @param <T>
     * @return
     * @throws Exception
     */
    @Deprecated
    public static <T> T createJavassistBytecodeProxy(Class<T> clazzType, CtMethod ctMethod) throws Exception {
        ClassPool mPool = new ClassPool(true);
        CtClass mCtc = mPool.makeClass(clazzType.getName() + "#JavaassistProxy");
        mCtc.addInterface(mPool.get(clazzType.getName()));
        mCtc.addConstructor(CtNewConstructor.defaultConstructor(mCtc));
        mCtc.addMethod(ctMethod);
        Class<?> pc = mCtc.toClass();
        return clazzType.cast(pc.newInstance());
    }

}
