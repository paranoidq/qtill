package me.qtill.commons.serialization;

import me.qtill.commons.serialization.hessian.HessianSerializer;
import me.qtill.commons.serialization.jdk.JdkSerializer;
import me.qtill.commons.serialization.kyro.KryoSerializer;
import me.qtill.commons.serialization.protostuff.ProtostuffSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class SerializeUtil {

    private static Map<SerializeType, Serializer> serializerMap = new HashMap<>();
    static {
        serializerMap.put(SerializeType.JDK, new JdkSerializer());
        serializerMap.put(SerializeType.KRYO, new KryoSerializer());
        serializerMap.put(SerializeType.HESSIAN, new HessianSerializer());
        serializerMap.put(SerializeType.PROTOSTUFF, new ProtostuffSerializer());
    }

    /**
     * 根据指定type，获取对应的Serializer实例
     * @param type
     * @return
     */
    public static final Serializer getInstance(SerializeType type) {
        return serializerMap.get(type);
    }

}
