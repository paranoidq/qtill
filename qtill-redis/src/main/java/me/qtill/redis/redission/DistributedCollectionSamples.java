package me.qtill.redis.redission;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.connection.ConnectionListener;

import java.net.InetSocketAddress;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class DistributedCollectionSamples {

    private static RedissonClient client;

    static {
        Config config = new Config();
        config.useSingleServer()
            .setAddress("redis://127.0.0.1:6379");

        // 默认jsoncodec
        client = Redisson.create(config);
    }


    public static void testMap() {
        RMap<String, User> map = client.getMap("redission-map4");
        User user = new User(1, "qianwei");
        map.put(user.getName(), user);

        user = map.get("qianwei");
        System.out.println(user.getId());
    }

    public static void testLock() {
        RLock lock = client.getLock("redisson-lock");
        lock.lock();
    }

    public static void testNode() {
        client.getNodesGroup().addConnectionListener(new ConnectionListener() {
            @Override
            public void onConnect(InetSocketAddress addr) {
                System.out.println("连接redis server");
            }

            @Override
            public void onDisconnect(InetSocketAddress addr) {
            }
        });
    }


    public static void main(String[] args) {
        testLock();
    }
}
