package me.qtill.zookeeper.practices.serviceDiscover.my.serialization;

import me.qtill.zookeeper.practices.serviceDiscover.my.serialization.hessian.HessianSerializer;
import me.qtill.zookeeper.practices.serviceDiscover.my.serialization.jdk.JdkSerializer;
import me.qtill.zookeeper.practices.serviceDiscover.my.serialization.kyro.KryoSerializer;
import me.qtill.zookeeper.practices.serviceDiscover.my.serialization.protostuff.ProtostuffSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class SerializerUtil {

    private static Map<SerializerType, Serializer> serializerMap = new HashMap<>();

    static {
        serializerMap.put(SerializerType.JDK, new JdkSerializer());
        serializerMap.put(SerializerType.KRYO, new KryoSerializer());
        serializerMap.put(SerializerType.HESSIAN, new HessianSerializer());
        serializerMap.put(SerializerType.PROTOSTUFF, new ProtostuffSerializer());
    }

    /**
     * 根据指定type，获取对应的Serializer实例
     *
     * @param type
     * @return
     */
    public static final Serializer getSerializer(SerializerType type) {
        return serializerMap.get(type);
    }

}
