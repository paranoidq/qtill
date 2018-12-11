package me.qtill.commons.serialization;

import org.junit.Test;

import java.io.IOException;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class SerializeUtilTest {


    @Test
    public void testJDK() throws IOException {
        Serializer serializer = SerializeUtil.getInstance(SerializeType.JDK);
        byte[] bytes = serializer.serialize("aa");
        String origin = serializer.deserialize(bytes, String.class);
        assert origin.equals("aa");
    }

    @Test
    public void testHessian() throws IOException {
        Serializer serializer = SerializeUtil.getInstance(SerializeType.HESSIAN);
        byte[] bytes = serializer.serialize("aa");
        String origin = serializer.deserialize(bytes, String.class);
        assert origin.equals("aa");
    }

    @Test
    public void testKyro() throws IOException {
        Serializer serializer = SerializeUtil.getInstance(SerializeType.KRYO);
        byte[] bytes = serializer.serialize("aa");
        String origin = serializer.deserialize(bytes, String.class);
        assert origin.equals("aa");
    }

    @Test
    public void testProtostuff() throws IOException {
        Serializer serializer = SerializeUtil.getInstance(SerializeType.PROTOSTUFF);
        byte[] bytes = serializer.serialize("aa");
        String origin = serializer.deserialize(bytes, String.class);
        assert origin.equals("aa");
    }
}