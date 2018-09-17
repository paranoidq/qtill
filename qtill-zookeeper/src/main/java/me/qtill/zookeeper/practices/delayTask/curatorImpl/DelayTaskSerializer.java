package me.qtill.zookeeper.practices.delayTask.curatorImpl;

import org.apache.curator.framework.recipes.queue.QueueSerializer;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class DelayTaskSerializer implements QueueSerializer<String> {

    @Override
    public byte[] serialize(String item) {
        return item.getBytes();
    }

    @Override
    public String deserialize(byte[] bytes) {
        return new String(bytes);
    }

}
