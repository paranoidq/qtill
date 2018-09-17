package me.qtill.zookeeper.client.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.List;

/**
 * Zookeeper Curator util
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class CuratorUtil {

    private CuratorFramework client;

    private CuratorUtil(CuratorFramework client) {
        if (client.getState() != CuratorFrameworkState.STARTED) {
            client.start();
        }
        this.client = client;
    }

    /**
     * new util instance
     *
     * @param client
     * @return
     */
    public static CuratorUtil getInstance(CuratorFramework client) {
        return new CuratorUtil(client);
    }

    /**
     * close curator client
     */
    public void close() {
        if (client.getState() == CuratorFrameworkState.STARTED) {
            CloseableUtils.closeQuietly(client);
        }
    }

    public String createPersistent(String path, byte[] payload) throws Exception {
        return client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, payload);
    }

    public String createEphemeral(String path, byte[] payload) throws Exception {
        return client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, payload);
    }

    public String createPersistentSequential(String path, byte[] payload) throws Exception {
        return client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(path, payload);
    }

    public String createEphemeralSequential(String path, byte[] payload) throws Exception {
        return client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path, payload);
    }

    public List<String> getChildern(String path) throws Exception {
        return client.getChildren().forPath(path);
    }

    public boolean checkExists(String path) throws Exception {
        return client.checkExists().forPath(path) != null;
    }

    public boolean checkExists(String path, CuratorWatcher watcher) throws Exception {
        return client.checkExists().usingWatcher(watcher).forPath(path) != null;
    }

    public Stat setData(String path, byte[] payload) throws Exception {
        return client.setData().forPath(path, payload);
    }

    public Stat setData(String path, byte[] payload, int version) throws Exception {
        return client.setData().withVersion(version).forPath(path, payload);
    }

    public Stat setDataAsync(String path, byte[] payload, BackgroundCallback callback) throws Exception {
        return client.setData().inBackground(callback).forPath(path, payload);
    }

    public byte[] getData(String path) throws Exception {
        return client.getData().forPath(path);
    }

    public byte[] getData(String path, Stat stat) throws Exception {
        return client.getData().storingStatIn(stat).forPath(path);
    }

    public byte[] getData(String path, Stat stat, CuratorWatcher watcher) throws Exception {
        return client.getData().storingStatIn(stat).usingWatcher(watcher).forPath(path);
    }

    public void delete(String path) throws Exception {
        client.delete().guaranteed().deletingChildrenIfNeeded().forPath(path);
    }

    public void delete(String path, int version) throws Exception {
        client.delete().guaranteed().deletingChildrenIfNeeded().withVersion(version).forPath(path);
    }
}
