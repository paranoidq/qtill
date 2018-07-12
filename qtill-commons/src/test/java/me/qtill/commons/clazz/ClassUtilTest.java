package me.qtill.commons.clazz;

import me.qtill.commons.clazz.testScan.*;
import me.qtill.commons.clazz.testScan.annotate.*;
import me.qtill.commons.clazz.testScan.sub.SubHello;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class ClassUtilTest {

    @Test
    public void getClasses() throws ClassNotFoundException {
        String basePackage = "me.qtill.commons.clazz.testScan";
        Set<Class<?>> clsSet = ClassUtil.getClasses(basePackage);
        assertTrue(clsSet.size() > 0);
        assertTrue(clsSet.contains(me.qtill.commons.clazz.testScan.IHello.class));
        assertFalse(clsSet.contains(ClassUtil.class));

        // test扫描子包
        assertTrue(clsSet.contains(SubHello.class));

        // 包含接口
        assertTrue(clsSet.contains(IHello.class));

        // test包括abstract class
        assertTrue(clsSet.contains(AbstractHello.class));
        assertTrue(clsSet.contains(HelloImpl2.class));

        assertTrue(clsSet.contains(TestAnnotation.class));
        assertTrue(clsSet.contains(TestEnum.class));

    }

    @Test
    public void testImplentationClasses() throws ClassNotFoundException {
        String basePackage = "me.qtill.commons.clazz.testScan";
        Set<Class<? extends IHello>> clsSet = ClassUtil.getImplementionClasses(basePackage, IHello.class);

        // 包含所有的直接实现类、间接实现类
        assertTrue(clsSet.contains(HelloImpl.class));
        assertTrue(clsSet.contains(HelloImpl2.class));
        assertTrue(clsSet.contains(SubHello.class));

        // 包含实现了接口的抽象类
        assertFalse(clsSet.contains(AbstractHello.class));

        // 包含实现了接口的enum
        assertTrue(clsSet.contains(TestEnum.class));

        // 不包含接口自身
        assertFalse(clsSet.contains(IHello.class));
        // 不包含没有实现接口的类
        assertFalse(clsSet.contains(NoHello.class));
    }

    @Test
    public void testAnnotation() {
        String basePackage = "me.qtill.commons.clazz.testScan";
        Set<Class<?>> clsSet = ClassUtil.getClassesWithAnnotation(basePackage, TestAnnotation.class);

        assertTrue(clsSet.contains(IAnnotate.class));
        assertTrue(clsSet.contains(AbstractAnnotate.class));
        assertTrue(clsSet.contains(AnnotateImpl.class));
        assertTrue(clsSet.contains(AnnotateImpl2.class));

        // 注解不是inherit的，接口注解了，但是实现类没有注解
        assertFalse(clsSet.contains(AnnotateImpl3.class));

        // 可继承的注解
        clsSet = ClassUtil.getClassesWithAnnotation(basePackage, TestAnnotation2.class);
        assertTrue(clsSet.contains(InheritAnnotate.class));
        // 注解被继承了 TODO
        assertTrue(clsSet.contains(InheritAnnotateImpl.class));


    }

    @Test
    public void resolve() throws ClassNotFoundException {
        String clsName = "me.qtill.commons.clazz.testScan.IHello";
        Class<?> cls = ClassUtil.resolve(clsName);
        assertSame(IHello.class, cls);
    }
}