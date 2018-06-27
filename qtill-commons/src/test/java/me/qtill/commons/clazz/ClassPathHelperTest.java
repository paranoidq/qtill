package me.qtill.commons.clazz;


import org.apache.commons.lang.ClassUtils;
import org.junit.Test;
import org.reflections.util.ClasspathHelper;

import java.net.URL;
import java.util.Collection;

import static org.junit.Assert.assertEquals;


/**
 * @author paranoidq
 * @since 1.0.0
 */
public class ClassPathHelperTest {

    @Test
    public void testClassPathHelper() {

        Collection<URL> resources = ClasspathHelper.forResource("resource.properties");
        System.out.println(resources);

        resources = ClasspathHelper.forPackage("me.qtill.commons.clazz");
        System.out.println(resources);
    }
}
