package me.qtill.zookeeper.client.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class Samples {

    private CuratorFramework client;

    public Samples() {

        RetryPolicy retryPolicy = new RetryUntilElapsed(
            5000, 1000);

        client = CuratorFrameworkFactory.builder()
            .connectString("localhost:2181")
            .sessionTimeoutMs(5000)
            .connectionTimeoutMs(5000)
            .retryPolicy(retryPolicy)
            .build();

        client.start();

    }


    /**
     * 1. 序列化需要自己完成
     *
     * @throws Exception
     */
    public void createNode() throws Exception {
        String path = client.create()
            .creatingParentsIfNeeded()
            .withMode(CreateMode.EPHEMERAL)
            .forPath("/test/1", "123".getBytes());
        System.out.println(path);

    }

    public void deleteNode() throws Exception {
        client.delete()
            .guaranteed()
            .deletingChildrenIfNeeded()
            .withVersion(-1)
            .forPath("/test/1");
    }

    public void getChildren() throws Exception {
        List<String> cList = client.getChildren().forPath("/test/1");

    }

    public void getData() throws Exception {
        Stat stat = new Stat();
        byte[] ret = client.getData().storingStatIn(stat).forPath("/test1");
        System.out.println(new String(ret));
    }

    public void setData() throws Exception {
        Stat stat = new Stat();
        byte[] ret = client.getData().storingStatIn(stat).forPath("/test1");

        client.setData().withVersion(stat.getVersion()).forPath("/test/1", "123".getBytes());

    }

    public void testNode() throws Exception {
        ExecutorService es = Executors.newFixedThreadPool(5);

//        client.checkExists().inBackground(new BackgroundCallback() {
//            @Override
//            public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
//                Stat stat = event.getStat();
//
//            }
//        }, "123", es).forPath("/test/1");

        Stat stat = client.checkExists().forPath("/zookeeper");
        System.out.println(stat != null);

    }

    public void testListener() throws Exception {
        final NodeCache cache = new NodeCache(client, "/test/1");
        cache.start();
        cache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                byte[] ret = cache.getCurrentData().getData();
                System.out.println(new java.lang.String(ret));
            }
        });
    }

    public static void main(String[] args) throws Exception {
        new Samples().testNode();

    }
}
