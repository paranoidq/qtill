package me.qtill.zookeeper.practices.queue;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.recipes.queue.DistributedQueue;
import org.apache.curator.framework.recipes.queue.QueueBuilder;
import org.apache.curator.framework.recipes.queue.QueueConsumer;
import org.apache.curator.framework.recipes.queue.QueueSerializer;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class CuratorQueue {


    public static class DistributedQueueExample {
        private static final String PATH = "/example/queue";

        public static void main(String[] args) throws Exception {
            TestingServer server = new TestingServer();
            CuratorFramework client = null;

            DistributedQueue<String> queue = null;
            try {
                client = CuratorFrameworkFactory.newClient(server.getConnectString(), new ExponentialBackoffRetry(1000, 3));
                client.getCuratorListenable().addListener(new CuratorListener() {
                    @Override
                    public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
                        System.out.println("curator event: " + event.getType().name());
                    }
                });
                client.start();

                // 构建queue

                QueueConsumer<String> consumer = createQueueConsumer();
                QueueBuilder<String> builder = QueueBuilder.builder(client, consumer, createQueueSerializer(), PATH);

                queue = builder.buildQueue();
                queue.start();


                for (int i = 0; i < 10; i++) {
                    queue.put("test-" + i);
                    TimeUnit.SECONDS.sleep((long) (3 * Math.random()));
                }

                TimeUnit.HOURS.sleep(1);
            } catch (Exception e) {

            } finally {
                CloseableUtils.closeQuietly(queue);
                CloseableUtils.closeQuietly(client);
                CloseableUtils.closeQuietly(server);
            }
        }

        private static QueueSerializer<String> createQueueSerializer() {
            return new QueueSerializer<String>() {
                @Override
                public byte[] serialize(String item) {
                    return item.getBytes();
                }

                @Override
                public String deserialize(byte[] bytes) {
                    return new String(bytes);
                }
            };
        }

        private static QueueConsumer<String> createQueueConsumer() {
            return new QueueConsumer<String>() {
                @Override
                public void consumeMessage(String message) throws Exception {
                    System.out.println("concume message: " + message);
                }

                @Override
                public void stateChanged(CuratorFramework client, ConnectionState newState) {
                    System.out.println("connection new state: " + newState.name());
                }
            };
        }
    }


}
