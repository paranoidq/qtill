package me.qtill.commons.clazz.test;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class ClassLoaderTest {

    public static void main(String[] args) throws ClassNotFoundException {
        Class.forName("me.qtill.commons.clazz.test.BarClass");
    }

}
