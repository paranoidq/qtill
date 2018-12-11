package me.qtill.commons.clazz.test;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class BarClass {

    public static String test;
    static {
        System.out.println("initialize BarClass");
    }

    public String m() {
        return "bar";
    }
}
