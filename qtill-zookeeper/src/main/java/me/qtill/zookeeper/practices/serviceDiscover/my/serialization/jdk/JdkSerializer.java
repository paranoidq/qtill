package me.qtill.zookeeper.practices.serviceDiscover.my.serialization.jdk;


import me.qtill.zookeeper.practices.serviceDiscover.my.serialization.Serializer;

import java.io.*;

/**
 * JDK原生序列化
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class JdkSerializer implements Serializer {

    @Override
    public byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
        outputStream.writeObject(object);
        outputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream inputStream = new ObjectInputStream(byteArrayInputStream);
        T object = null;
        try {
            object = (T) inputStream.readObject();
            return object;
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        } finally {
            inputStream.close();
        }
    }
}
