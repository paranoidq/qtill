package me.qtill.zookeeper.practices.pubsub;

import com.alibaba.fastjson.JSON;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;

import java.util.List;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class ManageServer {

    private String           serversPath;
    private String           commandPath;
    private String           configPath;
    private ZkClient         zkClient;
    private ServerConfig     config;
    // 监听servers节点的子节点列表变化
    private IZkChildListener childListener;
    private List<String>     workServeList;
    // 监听command节点数据内容的变化
    private IZkDataListener  dataListener;

    public ManageServer(String serversPath, String commandPath, String configPath, ZkClient zkClient, ServerConfig config, List<String> workServeList) {
        this.serversPath = serversPath;
        this.commandPath = commandPath;
        this.configPath = configPath;
        this.zkClient = zkClient;
        this.config = config;
        this.workServeList = workServeList;

        this.childListener = new IZkChildListener() {
            @Override
            public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                // 更新workSever列表
                ManageServer.this.workServeList = currentChilds;
                System.out.println("work server list chanded, refresh it");
                execList();
            }
        };

        this.dataListener = new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {
                String cmd = new String((byte[]) data);
                System.out.println("cmd: " + cmd);
                exeCmd(cmd); // 指定命令
            }

            @Override
            public void handleDataDeleted(String dataPath) throws Exception {

            }
        };
    }

    // 启动工作服务器
    public void start() {
        initRunning();
    }

    // 停止工作服务器
    public void stop() {
        zkClient.unsubscribeChildChanges(serversPath, childListener);
        zkClient.unsubscribeDataChanges(commandPath, dataListener);
    }

    private void initRunning() {
        zkClient.subscribeDataChanges(commandPath, dataListener);
        zkClient.subscribeChildChanges(serversPath, childListener);
    }

    /**
     * 根据指令执行不同的操作
     * <p>
     * 指令也保存到zk节点下，这样做的目的？
     *
     * @param cmdType
     */
    private void exeCmd(String cmdType) {
        if ("list".equals(cmdType)) {
            execList();

        } else if ("create".equals(cmdType)) {
            execCreate();
        } else if ("modify".equals(cmdType)) {
            execModify();
        } else {
            System.out.println("error command!" + cmdType);
        }
    }


    private void execList() {
        System.out.println(workServeList.toString());
    }

    private void execCreate() {
        if (!zkClient.exists(configPath)) {
            try {
                zkClient.createPersistent(configPath, true);
                zkClient.writeData(configPath, JSON.toJSONString(config).getBytes());
            } catch (ZkNodeExistsException e) {
                // conig节点已经存在，写入内容即可
                zkClient.writeData(configPath, JSON.toJSONString(config).getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void execModify() {
        // 我们随意修改config的一个属性就可以了
        // TODO
        config.setDbUser(config.getDbUser() + "_modify");

        try {
            zkClient.writeData(configPath, JSON.toJSONString(config).getBytes());
        } catch (ZkNoNodeException e) {
            execCreate(); // 写入时config节点还未存在，则创建它
        }
    }
}
