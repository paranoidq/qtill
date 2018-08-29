package me.qtill.redis.luttuce;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.function.BiConsumer;

/**
 * 与jedis客户端一样，都需要自己做序列化和反序列化
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class LuttuceSamples {

    public static void main(String[] args) {
        RedisClient client = RedisClient.create("redis://localhost:6379");
        RedisCommands command = client.connect().sync();

        // 不会做反序列化，
        String rst = (String) command.get("aaa");
        System.out.println(rst);


        RedisAsyncCommands asyncCommand = client.connect().async();
        RedisFuture<String> future = asyncCommand.get("aaa");
        future.whenComplete(new BiConsumer<String, Throwable>() {
            @Override
            public void accept(String s, Throwable throwable) {
                System.out.println(s);
            }
        });
    }
}
