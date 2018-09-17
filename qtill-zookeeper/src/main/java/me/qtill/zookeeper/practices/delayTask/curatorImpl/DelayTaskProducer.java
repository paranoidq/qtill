package me.qtill.zookeeper.practices.delayTask.curatorImpl;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.queue.DistributedDelayQueue;
import org.apache.curator.framework.recipes.queue.QueueBuilder;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class DelayTaskProducer {

    private static final String                        CONNECT_ADDRESS = "localhost:2181";
    private static final int                           SESSION_TIMEOUT = 5000;
    private static final String                        NAMESPACE       = "delayTask";
    private static final String                        QUEUE_PATH      = "/queue";
    private static final String                        LOCK_PATH       = "/lock";
    private              CuratorFramework              curatorFramework;
    private              DistributedDelayQueue<String> delayQueue;


    {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
        curatorFramework = CuratorFrameworkFactory.builder()
            .connectString(CONNECT_ADDRESS)
            .sessionTimeoutMs(SESSION_TIMEOUT)
            .retryPolicy(retryPolicy)
            .namespace(NAMESPACE)
            .build();
        curatorFramework.start();
        delayQueue = QueueBuilder.builder(
            curatorFramework,
            new DelayTaskConsumer(),
            new DelayTaskSerializer(),
            QUEUE_PATH
        ).lockPath(LOCK_PATH).buildDelayQueue();

        try {
            delayQueue.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void produce(String id, long timestamp) {
        try {
            delayQueue.put(id, timestamp);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
