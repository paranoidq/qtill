package me.qtill.commons.hash;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class NativeHashcodeTest {


    public static void main(String[] args) {
        NativeHashcodeTest o1 = new NativeHashcodeTest();

        NativeHashcodeTest o2 = new NativeHashcodeTest();

        // -XX:hashCode=0、1、2、3、4
        System.out.println(o1.hashCode());
        System.out.println(o2.hashCode());

    }
}
