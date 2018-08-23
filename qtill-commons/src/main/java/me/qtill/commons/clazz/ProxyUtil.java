package me.qtill.commons.clazz;

import com.google.common.base.Preconditions;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import me.qtill.commons.clazz.test.BarClass;
import me.qtill.commons.clazz.test.FooClass;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.TypeCache;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;
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


    public static void main(String[] args) throws Exception {
        FooClass foo = new FooClass();
        BarClass bar = new BarClass();
//        ByteBuddyAgent.install();
//        new ByteBuddy()
//            .redefine(BarClass.class)
//            .method(ElementMatchers.named("m"))
//            .intercept(FixedValue.value("xxx"))
////            .name(FooClass.class.getName())
//            .make()
//            .load(FooClass.class.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent())
//        ;

//        System.out.println(foo.m());
//        System.out.println(bar.m());


//        DynamicType.Builder<BarClass> builder = new ByteBuddy()
//            .subclass(BarClass.class);
//        Class<?> sub = builder.make().load(ClassLoader.getSystemClassLoader()).getLoaded();
//        System.out.println(sub.getName());


//        DynamicType.Builder<BarClass> builder = new ByteBuddy().with(new NamingStrategy.SuffixingRandom("_suffix", "prefi_"){
//            @Override
//            protected String name(TypeDescription superClass) {
//                return "aa" + super.name(superClass);
//            }
//        })
//            .subclass(BarClass.class);
//        Class<?> sub = builder.make().load(ClassLoader.getSystemClassLoader()).getLoaded();
//        System.out.println(sub.getName());



//        DynamicType.Builder<BarClass> builder = new ByteBuddy().redefine(BarClass.class).name("aaa")
//            .method(ElementMatchers.named("m")).intercept(FixedValue.value("xxx"));
//        Class<?> sub = builder.make().load(ClassLoader.getSystemClassLoader()).getLoaded();
//
//        System.out.println(sub.getName());
//        System.out.println(sub.getMethod("m").invoke(sub
//            .newInstance(), null));



        // 通过javaavent + hotswap动态修改已加载的类的行为
//        ByteBuddyAgent.install();
//        DynamicType.Builder<BarClass> builder = new ByteBuddy().redefine(BarClass.class)
//            .method(ElementMatchers.named("m")).intercept(FixedValue.value("xxx"));
//        Class<?> sub = builder.make().load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent()).getLoaded();
//
//        System.out.println(sub.getName());
//        System.out.println(bar.m());



        // 动态修改
        ByteBuddyAgent.install();
        DynamicType.Builder<BarClass> builder = new ByteBuddy().redefine(BarClass.class)
            .field(ElementMatchers.named("test")).value("test2");

        Class<?> sub = builder.make().load(ClassLoader.getSystemClassLoader(), ClassReloadingStrategy.fromInstalledAgent()).getLoaded();
        System.out.println(sub.getName());
        System.out.println(BarClass.test);
    }

}
