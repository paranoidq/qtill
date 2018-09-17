package me.qtill.zookeeper.practices.leaderElection;

import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class CuratorLeaderSample {

    public static final class LeaderLatchExample {
        private static final int    count = 10;
        private static final String path  = "/example/leader";


        public static void main(String[] args) throws Exception {

            List<CuratorFramework> clients = Lists.newArrayList();
            List<LeaderLatch> latches = Lists.newArrayList();

            try (TestingServer server = new TestingServer()) {
                CuratorFramework client = CuratorFrameworkFactory.newClient(
                    server.getConnectString(), new ExponentialBackoffRetry(1000, 3)
                );
                client.start();
                for (int i = 0; i < count; i++) {


//                    client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
//                        @Override
//                        public void stateChanged(CuratorFramework client, ConnectionState newState) {
//
//                        }
//                    });

                    LeaderLatch latch = new LeaderLatch(
                        client, path, "Client #" + i
                    );

                    // 强烈推荐添加ConnectionStateListener

                    clients.add(client);
                    latches.add(latch);


                    latch.start();

                }

                TimeUnit.SECONDS.sleep(5);

                LeaderLatch currentLeader = null;
                for (int i = 0; i < count; i++) {
                    LeaderLatch latch = latches.get(i);
                    if (latch.hasLeadership()) {
                        currentLeader = latch;
                    }
                }

                System.out.println("current leader: " + currentLeader.getId());

                System.out.println("release the leader " + currentLeader.getId());
                currentLeader.close();
                currentLeader.await(2, TimeUnit.SECONDS);

                System.out.println("Client #0 maybe is elected as the leader or not although it want to be");
                System.out.println("the new leader is " + latches.get(0).getLeader().getId());
            } finally {
                // close resources
                // ...
            }
        }
    }
}
