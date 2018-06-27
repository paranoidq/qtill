package me.qtill.commons.clazz;

import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.util.Set;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public final class ClassUtil {


    public Set<Class<?>> getClasses(String basePackage) {

        Reflections reflections = new Reflections(
            new ConfigurationBuilder()
            .setScanners(new SubTypesScanner(true), new ResourcesScanner())
        );
        return null;
    }



}
