package me.qtill.commons.serialization;

import org.junit.Test;

import java.io.IOException;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class SerializerUtilTest {


    @Test
    public void testJDK() throws IOException {
        Serializer serializer = SerializerUtil.getInstance(SerializerType.JDK);
        byte[] bytes = serializer.serialize("aa");
        String origin = serializer.deserialize(bytes, String.class);
        assert origin.equals("aa");
    }

    @Test
    public void testHessian() throws IOException {
        Serializer serializer = SerializerUtil.getInstance(SerializerType.HESSIAN);
        byte[] bytes = serializer.serialize("aa");
        String origin = serializer.deserialize(bytes, String.class);
        assert origin.equals("aa");
    }

    @Test
    public void testKyro() throws IOException {
        Serializer serializer = SerializerUtil.getInstance(SerializerType.KRYO);
        byte[] bytes = serializer.serialize("aa");
        String origin = serializer.deserialize(bytes, String.class);
        assert origin.equals("aa");
    }

    @Test
    public void testProtostuff() throws IOException {
        Serializer serializer = SerializerUtil.getInstance(SerializerType.PROTOSTUFF);
        byte[] bytes = serializer.serialize("aa");
        String origin = serializer.deserialize(bytes, String.class);
        assert origin.equals("aa");
    }
}