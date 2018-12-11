package me.qtill.commons.clazz;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.google.common.util.concurrent.RateLimiter;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.ClassAnnotationMatchProcessor;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.ClassMatchProcessor;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.ImplementingClassMatchProcessor;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.SubclassMatchProcessor;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;
import io.github.lukehutch.fastclasspathscanner.utils.ClasspathUtils;
import org.apache.commons.lang.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Set;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public final class ClassUtil {

    /**
     * 获取package下的所有的class
     *
     * @param basePackage
     * @return
     * @throws ClassNotFoundException
     */
    public static Set<Class<?>> getClasses(String basePackage) throws ClassNotFoundException {
        Set<Class<?>> classes = Sets.newHashSet();
        FastClasspathScanner fastClasspathScanner = new FastClasspathScanner(basePackage);
        ScanResult scanResult = fastClasspathScanner.matchAllClasses(new ClassMatchProcessor() {
            @Override
            public void processMatch(Class<?> classRef) {
                classes.add(classRef);
            }
        }).scan();
        return classes;
    }

    /**
     * 获取package下指定接口的实现类
     * 返回结果
     *      不包含：接口、抽象类
     *      包含：直接实现类、间接实现类
     *
     * @param basePackage
     * @param cls 必须是接口，不能是抽象类或标准类
     * @param <T>
     * @return
     */
    public static <T> Set<Class<? extends T>> getImplementionClasses(String basePackage, Class<T> cls) {
        Preconditions.checkArgument(isInterface(cls), "param is not interface: " + cls);
        Set<Class<? extends T>> classes = Sets.newHashSet();
        FastClasspathScanner fastClasspathScanner = new FastClasspathScanner(basePackage);
        fastClasspathScanner.matchClassesImplementing(cls, new ImplementingClassMatchProcessor<T>() {
            @Override
            public void processMatch(Class<? extends T> subclass) {
                if (!isAbstract(subclass) && !isInterface(subclass)) {
                    classes.add(subclass);
                }
            }
        }).scan();
        return classes;
    }


    /**
     * 获取package下标记了特定注解的类
     *
     * @param basePackage
     * @param annotationCls
     * @return
     */
    public static Set<Class<?>> getClassesWithAnnotation(String basePackage, Class<? extends Annotation> annotationCls) {
        Set<Class<?>> classes = Sets.newHashSet();
        FastClasspathScanner fastClasspathScanner = new FastClasspathScanner(basePackage);
        fastClasspathScanner.matchClassesWithAnnotation(annotationCls, new ClassAnnotationMatchProcessor() {
            @Override
            public void processMatch(Class<?> classWithAnnotation) {
                classes.add(classWithAnnotation);
            }
        }).scan();
        return classes;
    }


    /**
     * 是否是接口
     *
     * @param cls
     * @return
     */
    public static boolean isInterface(Class<?> cls) {
        return Modifier.isInterface(cls.getModifiers());
    }

    /**
     * 是否是抽象类
     *
     * @param cls
     * @return
     */
    public static boolean isAbstract(Class<?> cls) {
        return Modifier.isAbstract(cls.getModifiers());
    }

    /**
     * 是否是内部类
     *
     * @param cls
     * @return
     */
    public static boolean isInnerClass(Class<?> cls) {
        if (cls == null) {
            return false;
        }
        return cls.getName().indexOf('$') >= 0;
    }


    /**
     * 通过class全限定名解析出class对象
     *
     * @param className
     * @return
     * @throws ClassNotFoundException
     */
    public static Class<?> resolve(String className) throws ClassNotFoundException {
        return ClassUtils.getClass(className);
    }


}
