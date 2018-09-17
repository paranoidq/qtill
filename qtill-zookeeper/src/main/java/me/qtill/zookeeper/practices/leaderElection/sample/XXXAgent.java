package me.qtill.zookeeper.practices.leaderElection.sample;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class XXXAgent {

    private String leaderPath;
    private int    agentId;

    private LeaderLatch      latch;

    public XXXAgent(int agentId, CuratorFramework client, String leaderPath) {
        this.agentId = agentId;
        latch = new LeaderLatch(client, leaderPath, "agent#" + agentId);
    }

    public void start() throws Exception {

        latch.start();

        latch.addListener(new LeaderLatchListener() {
            @Override
            public void isLeader() {
                System.out.println("agent#" + agentId + " takes leadership");
            }

            @Override
            public void notLeader() {

            }
        });
    }

    public void stop() throws IOException {
        if (latch.getState() == LeaderLatch.State.STARTED) {
            latch.close();
        }
    }

    public boolean isLeader() {
        return latch.hasLeadership();
    }

    public boolean isFollower() {
        return !isLeader();
    }

    public void work() throws Exception {
        while (true) {
            if (isLeader()) {
                System.out.println("agent#" + agentId + " do leader work");
                latch.close();
                System.out.println("agent#" + agentId + " releases leadership");
            } else {
                System.out.println("agent#" + agentId + " do follow work");
            }
            TimeUnit.SECONDS.sleep(5);
        }

    }

}
