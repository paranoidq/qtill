package me.qtill.redis.cache.support.serialization;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.Optional;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class DefaultCacheSerializeHandler implements CacheSerializeHandler {

    @Override
    public <T extends Serializable> byte[] serialize(T object) {
        return SerializationUtils.serialize(object);
    }

    @Override
    public <T extends Serializable> Optional<T> deserialize(byte[] bytes) {
        return Optional.ofNullable(bytes == null ? null : SerializationUtils.deserialize(bytes));
    }
}
