package me.qtill.zookeeper.curator.lock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.sql.Time;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class CuratorDistributedLock {

    private static final CuratorFramework client;
    private static final String           connectString = "localhost:2181";
    private static final String           mutexPath     = "/mutex";
    private static final String           namespace     = "curator_samples";

    static {
        client = CuratorFrameworkFactory.newClient(connectString, new ExponentialBackoffRetry(1000, 3));
        client.start();
        client.usingNamespace(namespace);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                client.close();
            }
        });
    }

    public static void main(String[] args) throws Exception {
        InterProcessMutex mutex = new InterProcessMutex(client, mutexPath);

        int count = 2;
        for (int j = 0; j < count; j++) {
            Thread t = new Thread(new Task(mutex, j));
            t.start();
        }
        TimeUnit.HOURS.sleep(1);
    }

    private static class Task implements Runnable {

        private InterProcessMutex lock;
        private int id;

        public Task(InterProcessMutex lock, int id) {
            this.lock = lock;
            this.id = id;
        }

        @Override
        public void run() {
            try {
                while (lock.acquire(1000, TimeUnit.MILLISECONDS)) {
                    try {
                        System.out.println("Thread-" + id + " get lock and executing");
                        TimeUnit.SECONDS.sleep(5);
                    } finally {
                        System.out.println("Thread-" + id + " release lock");
                        lock.release();
                        TimeUnit.SECONDS.sleep(1);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }
}
