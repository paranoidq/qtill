package me.qtill.commons.serialization;

import java.io.IOException;

/**
 * 序列化接口
 *
 * @author paranoidq
 * @since 1.0.0
 */
public interface Serializer {

    /**
     * 序列化
     * @param object
     * @return
     * @throws IOException
     */
    byte[] serialize(Object object) throws IOException;


    /**
     * 反序列化
     * @param bytes
     * @param clazz
     * @param <T>
     * @return
     * @throws IOException
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException;

}
