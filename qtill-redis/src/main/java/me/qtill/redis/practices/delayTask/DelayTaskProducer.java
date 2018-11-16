package me.qtill.redis.practices.delayTask;

import me.qtill.redis.practices.RedisClient;
import redis.clients.jedis.Jedis;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class DelayTaskProducer {

    public void produce(String newsId, long timestamp) {
        Jedis client = RedisClient.getClient();
        try {
            client.zadd(Constants.DELAY_TASK_QUEUE, timestamp, newsId);
        } finally {
            client.close();
        }
    }
}
