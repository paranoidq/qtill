package me.qtill.zookeeper.practices.leaderElection;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkInterruptedException;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.zookeeper.CreateMode;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class WorkServer {

    private volatile     boolean         running     = false;
    private              ZkClient        zkClient;
    private static final String          MASTER_PATH = "/master";
    private              IZkDataListener dataListener;
    private              RunningData     serverData;
    private              RunningData     masterData;

    private ScheduledExecutorService delayExecutor = Executors.newScheduledThreadPool(1);
    private int                      delayTime     = 5;


    public WorkServer(RunningData rd) {
        this.serverData = rd;
        this.dataListener = new IZkDataListener() {

            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {
                // do nothing
            }

            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
                if (masterData != null
                    && masterData.getName().equals(serverData.getName())) {
                    // 自己就是上一轮的Master服务器，则直接抢
                    takeMaster();
                } else {
                    // 否则，延迟5秒后再抢。主要是应对网络抖动，给上一轮的Master服务器优先抢占master的权利，避免不必要的数据迁移开销
                    delayExecutor.schedule(new Runnable() {
                        @Override
                        public void run() {
                            takeMaster();
                        }
                    }, delayTime, TimeUnit.SECONDS);
                }
            }
        };
    }

    public ZkClient getZkClient() {
        return zkClient;
    }

    public void setZkClient(ZkClient zkClient) {
        this.zkClient = zkClient;
    }


    public void start() throws Exception {
        if (running) {
            return;
        }
        running = true;
        // 订阅master节点删除事件
        zkClient.subscribeDataChanges(MASTER_PATH, dataListener);
        //抢占master权利
        takeMaster();
    }


    public void stop() throws Exception {
        if (!running) {
            return;
        }
        running = false;
        // 停止延迟处理器
        delayExecutor.shutdown();
        // 取消订阅master节点事件
        zkClient.unsubscribeDataChanges(MASTER_PATH, dataListener);
        // 释放master权利
        releaseMaster();
    }

    private void takeMaster() {
        if (!running) {
            return;
        }
        try {
            zkClient.create(MASTER_PATH, serverData, CreateMode.EPHEMERAL);
            masterData = serverData;
            System.out.println(serverData.getName() + " is master");

            //
        } catch (ZkNodeExistsException e) {
            // 竞争master权利失败，需要读取master节点，并记录到本地
            RunningData runningData = zkClient.readData(MASTER_PATH, true);
            if (runningData == null) {
                // 没读到，读取瞬间Master节点宕机了，有机会再次争抢
                takeMaster();
            } else {
                masterData = runningData;
            }
        } catch (Exception e) {
            // ignore or log
        }
    }

    private void releaseMaster() {
        // 只有自己是master，才能释放master权利
        if (iAmMaster()) {
            zkClient.delete(MASTER_PATH);
        }
    }


    private boolean iAmMaster() {
        try {
            RunningData eventData = zkClient.readData(MASTER_PATH);
            masterData = eventData;
            if (masterData.getName().equals(serverData.getName())) {
                return true;
            }
            return false;
        } catch (ZkNoNodeException e) {
            return false; // 节点不存在，肯定不是master
        } catch (ZkInterruptedException e) {
            return iAmMaster();
        } catch (ZkException e) {
            return false;
        }
    }
}

