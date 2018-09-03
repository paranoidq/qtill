package me.qtill.zookeeper.client.zkClient;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.apache.zookeeper.data.Stat;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class Samples {

    private ZkClient client;

    public void createSession() {
        client = new ZkClient("localhost:2181", 1000, 1000, new SerializableSerializer());
    }


    public void getData() {
        Stat stat = new Stat();
        client.readData("/test", stat);

        // client读取数据后回写Stat对象
        System.out.println(stat.getVersion());
    }
}
