package me.qtill.zookeeper.practices.leaderElection;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class LeaderSelectorTest {

    private static final int    CLIENT_QTY       = 10;
    private static final String ZOOKEEPER_SERVER = "localhost:2181";


    public static void main(String[] args) throws Exception {
        List<ZkClient> clients = new ArrayList<>();

        List<WorkServer> workServers = new ArrayList<>();

        try {
            for (int i = 0; i < CLIENT_QTY; i++) {
                ZkClient client = new ZkClient(ZOOKEEPER_SERVER, 5000, 5000, new SerializableSerializer());
                clients.add(client);

                RunningData runningData = new RunningData();
                runningData.setCid(Long.valueOf(i));
                runningData.setName("Client #" + i);

                WorkServer workServer = new WorkServer(runningData);
                workServer.setZkClient(client);

                workServers.add(workServer);

                workServer.start();
            }
        } finally {
            System.out.println("shut down");


            for (WorkServer workServer : workServers) {
                workServer.stop();
            }

            for (ZkClient client : clients) {
                client.close();
            }
        }
    }
}
