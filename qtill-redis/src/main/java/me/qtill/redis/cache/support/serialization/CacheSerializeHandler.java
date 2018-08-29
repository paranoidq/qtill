package me.qtill.redis.cache.support.serialization;

import java.io.Serializable;
import java.util.Optional;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public interface CacheSerializeHandler {

    /**
     * 序列化
     * @param object
     * @param <T>
     * @return
     */
    <T extends Serializable> byte[] serialize(T object);

    /**
     * 反序列化
     * @param bytes
     * @param <T>
     * @return
     */
    <T extends Serializable> Optional<T> deserialize(byte[] bytes);

}
