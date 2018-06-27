package me.qtill.commons.text;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class StringUtilTest {

    @Test
    public void join() {
        char[] chars = {'a', 'b', 'c'};
        String string = StringUtil.join(chars, '-');
        assertEquals("a-b-c", string);
    }
}