package me.qtill.zookeeper.practices.leaderElection.sample;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class Main {

    public static final int    count      = 5;
    public static final String leaderPath = "/examples/leaderss";

    public static void main(String[] args) throws Exception {

        TestingServer server = new TestingServer();

        CuratorFramework client = CuratorFrameworkFactory.newClient(
            server.getConnectString(), new ExponentialBackoffRetry(1000, 4)
        );
        client.start();

        ExecutorService executor = Executors.newFixedThreadPool(count);
        for (int i = 0; i < count; i++) {
            int agentId = i;
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    XXXAgent agent = new XXXAgent(agentId, client, leaderPath);
                    try {
                        agent.start();
                        agent.work();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }


        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);
    }
}
