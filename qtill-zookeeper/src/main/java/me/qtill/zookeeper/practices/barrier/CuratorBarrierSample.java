package me.qtill.zookeeper.practices.barrier;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.framework.recipes.barriers.DistributedDoubleBarrier;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class CuratorBarrierSample {

    public static final class DistributedBarrierExample {
        private static final int    count = 5;
        private static final String path  = "/example/barrier";


        public static void main(String[] args) throws Exception {
            try (TestingServer server = new TestingServer()) {
                CuratorFramework client =
                    CuratorFrameworkFactory.newClient(server.getConnectString(), new ExponentialBackoffRetry(1000, 3));
                client.start();


                ExecutorService service = Executors.newFixedThreadPool(count);
                DistributedBarrier controlBarrier = new DistributedBarrier(
                    client, path);
                controlBarrier.setBarrier();

                for (int i = 0; i < count; i++) {
                    final DistributedBarrier barrier = new DistributedBarrier(client, path);
                    final int index = i;
                    service.submit(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            System.out.println("client #" + index + " wait on barrier");
                            barrier.waitOnBarrier();
                            System.out.println("Client #" + index + " begins");
                            return null;
                        }
                    });
                }
                TimeUnit.SECONDS.sleep(10);
                System.out.println("all Barrier instances should wait the condition");

                controlBarrier.removeBarrier();

                service.shutdown();
                service.awaitTermination(10, TimeUnit.MINUTES);

            }
        }
    }


    public static final class DisteibutedDoubleBarrierSample {
        public static final  int    count = 5;
        private static final String path  = "/example/barrier";

        public static void main(String[] args) throws Exception {
            try (TestingServer server = new TestingServer()) {
                CuratorFramework client = CuratorFrameworkFactory.newClient(
                    server.getConnectString(), new ExponentialBackoffRetry(1000, 3)
                );
                client.start();

                ExecutorService service = Executors.newFixedThreadPool(count);
                for (int i = 0; i < count; i++) {
                    final DistributedDoubleBarrier barrier = new DistributedDoubleBarrier(
                        client, path, count);
                    final int index = i;

                    service.submit(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            System.out.println("Client #" + index + " enters");

                            // 阻塞，直到所有参与者达到barrier之后
                            barrier.enter();

                            System.out.println("Client #" + index + " begins");

                            TimeUnit.SECONDS.sleep(3);


                            // 阻塞，直到所有参与者达到barrier之后
                            barrier.leave();

                            System.out.println("Client #" + index + " left");
                            return null;
                        }
                    });
                }
                service.shutdown();
                service.awaitTermination(10, TimeUnit.MINUTES);
            }
        }


    }

}
