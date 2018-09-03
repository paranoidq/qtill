package me.qtill.zookeeper.practices.queue;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class DistributedBlockingQueue<T> extends DistributedSimpleQueue<T> {

    public DistributedBlockingQueue(ZkClient zkClient, String root) {
        super(zkClient, root);
    }

    @Override
    public boolean offer(T element) throws Exception {
        return super.offer(element);
    }

    @Override
    public T poll() throws Exception {
        while (true) {
            final CountDownLatch latch = new CountDownLatch(1);
            final IZkChildListener childListener = new IZkChildListener() {
                @Override
                public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                    latch.countDown();
                }
            };

            zkClient.subscribeChildChanges(root, childListener);
            try {
                T node = super.poll();
                if (node != null) {
                    return node;
                } else {
                    latch.await(); // 阻塞等待空队列
                }
            } finally {
                zkClient.unsubscribeChildChanges(root, childListener);
            }
        }
    }
}
