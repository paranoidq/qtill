package me.qtill.commons.codec;

import org.apache.commons.codec.DecoderException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class CodecUtilTest {

    @Test
    public void byteArray2HexWithSp() throws DecoderException {
        String str = "aaa";
        String hex = CodecUtil.byteArray2Hex(str.getBytes(), ',');
        assertEquals("61,61,61", hex);
    }

    @Test
    public void testHex2ByteArrayWithSp() throws DecoderException {
        String hex = "61,61,61";
        String srt = "aaa";
        byte[] origin = CodecUtil.hex2ByteArray(hex, ',');
        assertArrayEquals(srt.getBytes(), origin);
    }
}