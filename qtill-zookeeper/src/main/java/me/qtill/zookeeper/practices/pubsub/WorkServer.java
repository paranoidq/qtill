package me.qtill.zookeeper.practices.pubsub;

import com.alibaba.fastjson.JSON;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class WorkServer {
    private ZkClient        zkClient;
    private String          configPath;
    private String          serversPath;
    private ServerData      serverData;
    private ServerConfig    serverConfig;
    private IZkDataListener dataListener;


    public WorkServer(ZkClient zkClient, String configPath, String serversPath, ServerData serverData, ServerConfig serverConfig) {
        this.zkClient = zkClient;
        this.configPath = configPath;
        this.serversPath = serversPath;
        this.serverData = serverData;
        this.serverConfig = serverConfig;

        this.dataListener = new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {
                String retJson = new String((byte[]) data);
                ServerConfig serverConfigLocal = JSON.parseObject(retJson, ServerConfig.class);

                updateConfig(serverConfigLocal);

                System.out.println("new work sever config: " + serverConfig.toString());
            }

            @Override
            public void handleDataDeleted(String dataPath) throws Exception {

            }
        };
    }

    public void start() {
        System.out.println("work server start");
        initRunning();
    }

    public void stop() {
        System.out.println("work server stop");
        zkClient.unsubscribeDataChanges(configPath, dataListener);
    }

    private void initRunning() {
        registerMe();

        // 监听configPath子节点的变化
        zkClient.subscribeDataChanges(configPath, dataListener);
    }

    /**
     * 将自己注册到serversPath节点下
     * <p>
     * 这里是让集群知道到底有多少个workServer，从而发布config时进行通知
     */
    private void registerMe() {
        String mePath = serversPath.concat("/").concat(serverData.getAddress());
        try {
            zkClient.createEphemeral(mePath, JSON.toJSONString(serverData).getBytes());
        } catch (ZkNodeExistsException e) {
            // 通过检测异常类创建父节点
            zkClient.createPersistent(serversPath, true);
            registerMe();
        }
    }


    private void updateConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }
}
