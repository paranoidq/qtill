package me.qtill.commons.serialization;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class SerializerUtilTest {


    @Test
    public void testJDK() throws IOException {
        Serializer serializer = SerializerUtil.getSerializer(SerializerType.JDK);
        byte[] bytes = serializer.serialize("aa");
        String origin = serializer.deserialize(bytes, String.class);
        assert origin.equals("aa");
    }

    @Test
    public void testHessian() throws IOException {
        Serializer serializer = SerializerUtil.getSerializer(SerializerType.HESSIAN);
        byte[] bytes = serializer.serialize("aa");
        String origin = serializer.deserialize(bytes, String.class);
        assert origin.equals("aa");
    }

    @Test
    public void testKyro() throws IOException {
        Serializer serializer = SerializerUtil.getSerializer(SerializerType.KRYO);
        byte[] bytes = serializer.serialize("aa");
        String origin = serializer.deserialize(bytes, String.class);
        assert origin.equals("aa");
    }

    @Test
    public void testProtostuff() throws IOException {
        Serializer serializer = SerializerUtil.getSerializer(SerializerType.PROTOSTUFF);
        byte[] bytes = serializer.serialize("aa");
        String origin = serializer.deserialize(bytes, String.class);
        assert origin.equals("aa");
    }
}