package me.qtill.commons.codec;

import org.junit.Test;

import java.nio.charset.Charset;

import static org.junit.Assert.*;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class CharsetsTest {


    @Test
    public void testOf() {
        Charset charset = Charsets.of("ISO_8859_1");
        assertEquals(Charsets.ISO_8859_1, charset);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testNullInput() {
        Charset charset = Charsets.of(null);
    }


    @Test
    public void test() {
        Charset charset = Charsets.GBK;
        assertEquals(Charset.forName("GBK"), charset);
    }
}