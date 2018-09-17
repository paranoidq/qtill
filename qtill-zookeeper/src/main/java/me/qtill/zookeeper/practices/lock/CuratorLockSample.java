package me.qtill.zookeeper.practices.lock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMultiLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class CuratorLockSample {


    public static final class InterProcessMutexSample {

        private static final String lockPath = "/example/lock";

        public static void main(String[] args) throws Exception {
            try (TestingServer server = new TestingServer()) {
                CuratorFramework client = CuratorFrameworkFactory.newClient(
                    server.getConnectString(), new ExponentialBackoffRetry(1000, 3));
                client.start();

                InterProcessMutex lock = new InterProcessMutex(
                    client, lockPath);
            }
        }

    }
}
