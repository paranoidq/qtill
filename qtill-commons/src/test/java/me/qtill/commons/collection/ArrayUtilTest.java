package me.qtill.commons.collection;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class ArrayUtilTest {

    @Test
    public void testJoin() {
        String[] a1 = {"a"};
        String[] a2 = {"b"};
        String[] s = ArrayUtil.join(a1, a2);
        assertTrue(s.length == 2);

    }

    @Test
    public void testToString() {
        int[] a = {1, 2, 3};
        System.out.println(ArrayUtil.toString(a));

        char[] s = {'a', 'b'};
        System.out.println(ArrayUtil.toString(s));
    }
}