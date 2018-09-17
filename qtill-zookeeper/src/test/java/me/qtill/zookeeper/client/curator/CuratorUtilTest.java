package me.qtill.zookeeper.client.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class CuratorUtilTest {

    private TestingServer testingServer;
    private CuratorUtil   curatorUtil;

    @Before
    public void setUp() throws Exception {
        testingServer = new TestingServer();

        CuratorFramework client = CuratorFrameworkFactory.builder()
            .connectString(testingServer.getConnectString())
            .retryPolicy(new ExponentialBackoffRetry(1000, 3))
            .namespace("test")
            .build();

        client.start();
        curatorUtil = CuratorUtil.getInstance(client);
    }

    @After
    public void tearDown() throws Exception {
        curatorUtil.close();
        CloseableUtils.closeQuietly(testingServer);
    }


    @Test
    public void testUtils() throws Exception {
        String path = curatorUtil.createPersistent("/a", "aa".getBytes());
        // 不包含namespace
        assertEquals("/a", path);
    }

    @Test
    public void testSequential() throws Exception {
        String path = curatorUtil.createPersistentSequential("/a", "aa".getBytes());
        // /a0000000000
        System.out.println(path);
    }

    @Test
    public void testgetChildern() throws Exception {
        curatorUtil.createPersistent("/a", "aa".getBytes());
        curatorUtil.createPersistent("/b", "bb".getBytes());
        List<String> path = curatorUtil.getChildern("/");

        System.out.println(path.toString());

        // 不带斜杠
        assertEquals("a", path.get(0));
        assertEquals("b", path.get(1));

    }
}