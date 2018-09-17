package me.qtill.zookeeper.practices.counter;

import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.framework.recipes.shared.SharedCount;
import org.apache.curator.framework.recipes.shared.SharedCountListener;
import org.apache.curator.framework.recipes.shared.SharedCountReader;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.test.TestingServer;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class CuratorCounterSample {

    public static final class SharedCountSample {

        private static final int    QTY  = 5;
        public static final  String PATH = "/example/counter";

        public static void main(String[] args) throws Exception {
            final Random rand = new Random();

            try (TestingServer server = new TestingServer()) {
                CuratorFramework client = CuratorFrameworkFactory.newClient(
                    server.getConnectString(), new ExponentialBackoffRetry(1000, 3));
                client.start();

                SharedCount baseCount = new SharedCount(client, PATH, 0);
                baseCount.addListener(new SharedCountListener() {
                    @Override
                    public void countHasChanged(SharedCountReader sharedCount, int newCount) throws Exception {
                        System.out.println("Counter's value is changed to " + newCount);
                    }

                    @Override
                    public void stateChanged(CuratorFramework client, ConnectionState newState) {
                        System.out.println("State changed: " + newState.toString());
                    }
                });
                baseCount.start();


                List<SharedCount> examples = Lists.newArrayList();
                ExecutorService executorService = Executors.newFixedThreadPool(QTY);
                for (int i = 0; i < QTY; i++) {
                    final SharedCount count = new SharedCount(client, PATH, 0);
                    examples.add(count);

                    Callable<Void> task = new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            count.start();
                            TimeUnit.SECONDS.sleep(1);
                            System.out.println("increment: " +
                                count.trySetCount(count.getVersionedValue(), count.getCount() + rand.nextInt(100)));
                            return null;
                        }
                    };

                    executorService.submit(task);
                }

                executorService.shutdown();
                executorService.awaitTermination(10, TimeUnit.MINUTES);

                for (int i = 0; i < QTY; i++) {
                    examples.get(i).close();
                }

                baseCount.close();
            }
        }
    }


    public static final class DistributedAtomicLongExample {
        private static final int    count = 5;
        private static final String path  = "/exapmles/counter";

        public static void main(String[] args) throws Exception {
            try (TestingServer server = new TestingServer()) {
                CuratorFramework client = CuratorFrameworkFactory.newClient(
                    server.getConnectString(), new ExponentialBackoffRetry(1000, 3));
                client.start();


                List<DistributedAtomicLong> examples = Lists.newArrayList();
                ExecutorService executor = Executors.newFixedThreadPool(count);
                for (int i = 0; i < count; i++) {

                    final DistributedAtomicLong counter =
                        new DistributedAtomicLong(client, path, new RetryNTimes(10, 10));
                    examples.add(counter);

                    Callable<Void> task = new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            try {
                                AtomicValue<Long> retInfo = counter.increment();

                                /*
                                 * 必须检查返回结果的succeeded()， 它代表此操作是否成功。 如果操作成功， preValue()代表操作前的值， postValue()代表操作后的值。
                                 */
                                System.out.println("succeed: " + retInfo.succeeded());
                                if (retInfo.succeeded()) {
                                    System.out.println("Increment from " + retInfo.preValue() +
                                        " to " + retInfo.postValue());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    };

                    executor.submit(task);
                }

                executor.shutdown();
                executor.awaitTermination(10, TimeUnit.MINUTES);
            } catch (Exception e) {

            } finally {

            }
        }
    }

}
