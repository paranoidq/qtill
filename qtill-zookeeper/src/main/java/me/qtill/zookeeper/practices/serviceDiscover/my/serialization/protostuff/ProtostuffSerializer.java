package me.qtill.zookeeper.practices.serviceDiscover.my.serialization.protostuff;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import me.qtill.zookeeper.practices.serviceDiscover.my.serialization.Serializer;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.io.IOException;

/**
 * Protostuff序列化
 *
 * Protostuff是Google Protobuff的升级版，支持动态编译，适合RPC通信的特性
 *
 * objenesis.newInstance(clz) 可以由 clz.newInstance() 代替，后者也可以实例化一个对象，但如果对象缺少无参构造函数，则会报错。
 * 借助于objenesis 可以绕开无参构造器实例化一个对象，且性能优于直接反射创建。
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class ProtostuffSerializer implements Serializer {
    // Objenesis对象调用无状态，线程安全
    private Objenesis objenesis = new ObjenesisStd();

    @Override
    public byte[] serialize(Object object) throws IOException {
        Class clazz = object.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema schema = RuntimeSchema.createFrom(clazz);
            return ProtostuffIOUtil.toByteArray(object, schema, buffer);
        } catch (Exception e) {
            throw e;
        } finally {
            buffer.clear();
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException {
        T object = objenesis.newInstance(clazz);
        Schema<T> schema = RuntimeSchema.createFrom(clazz);
        ProtostuffIOUtil.mergeFrom(bytes, object, schema);
        return object;
    }
}
