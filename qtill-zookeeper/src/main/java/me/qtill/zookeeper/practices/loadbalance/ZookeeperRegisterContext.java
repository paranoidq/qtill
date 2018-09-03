package me.qtill.zookeeper.practices.loadbalance;

import org.I0Itec.zkclient.ZkClient;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class ZookeeperRegisterContext {

    private String path;
    private ZkClient zkClient;
    private Object data;

    public ZookeeperRegisterContext(String path, ZkClient zkClient, Object data) {
        this.path = path;
        this.zkClient = zkClient;
        this.data = data;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ZkClient getZkClient() {
        return zkClient;
    }

    public void setZkClient(ZkClient zkClient) {
        this.zkClient = zkClient;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
