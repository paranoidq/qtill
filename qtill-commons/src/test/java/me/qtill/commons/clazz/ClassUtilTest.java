package me.qtill.commons.clazz;

import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class ClassUtilTest {

    @Test
    public void testGetConstructor() throws NoSuchMethodException {
        Constructor<IHello> constructor = ClassUtil.getConstructor(HelloImpl.class, null);
        Constructor<IHello> constructor2 = ClassUtil.getConstructor(HelloImpl.class, new Class[]{String.class});

        assertEquals(HelloImpl.class.getConstructor(null), constructor);
        assertEquals(HelloImpl.class.getConstructor(new Class[]{String.class}), constructor2);
        assertEquals(2, HelloImpl.class.getConstructors().length);
    }

    @Test
    public void testGetPackageName() {
        String pkgNme = ClassUtil.getPackageCononicalName(HelloImpl.class);
        assertEquals("me.qtill.commons.clazz", pkgNme);
    }


    @Test
    public void testGetInterfaces() {
        List<Class<?>> interfaces = ClassUtil.getAllInterfaces(HelloImpl.class);
        assertEquals(1, interfaces.size());
        assertEquals(IHello.class, interfaces.get(0));
    }


    @Test
    public void testgetClasses() throws IOException {
        List<Class<?>> classes = ClassUtil.getClasses("me.qtill.commons.clazz", IHello.class);
        assertEquals(2, classes.size());
    }
}