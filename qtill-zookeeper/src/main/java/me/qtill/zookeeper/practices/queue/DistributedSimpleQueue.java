package me.qtill.zookeeper.practices.queue;

import org.I0Itec.zkclient.ExceptionUtil;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.apache.curator.framework.recipes.queue.DistributedPriorityQueue;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class DistributedSimpleQueue<T> {
    protected final        ZkClient zkClient;
    protected final        String   root;
    protected static final String   NODE_NAME = "n_";

    public DistributedSimpleQueue(ZkClient zkClient, String root) {
        this.zkClient = zkClient;
        this.root = root;
    }


    public int size() {
        return zkClient.getChildren(root).size();
    }

    public boolean isEmpty() {
        return zkClient.getChildren(root).size() == 0;
    }

    public boolean offer(T element) throws Exception {
        String elementNodePath = root.concat("/").concat(NODE_NAME);
        try {
            zkClient.createPersistentSequential(elementNodePath, element);
        } catch (ZkNoNodeException e) {
            zkClient.createPersistent(root);
            offer(element);
        } catch (Exception e) {
            throw ExceptionUtil.convertToRuntimeException(e);
        }
        return true;
    }

    public T poll() throws Exception {
        try {
            List<String> list = zkClient.getChildren(root);
            if (list.size() == 0) {
                return null;
            }

            Collections.sort(list, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return getNodeNumer(o1, NODE_NAME).compareTo(getNodeNumer(o2, NODE_NAME));
                }
            });

            // 循环每个顺序节点名
            for (String nodeName : list) {
                String nodeFullPath = root.concat("/").concat(nodeName);
                try {
                    T node = (T) zkClient.readData(nodeFullPath);
                    // 删除顺序节点
                    zkClient.delete(nodeFullPath);
                    return node;
                } catch (ZkNoNodeException e) {
                    // ignore 由其他客户端把这个顺序节点消费掉了
                }
            }
            return null;
        } catch (Exception e) {
            throw ExceptionUtil.convertToRuntimeException(e);
        }
    }

    private String getNodeNumer(String str, String nodeName) {
        int index = str.lastIndexOf(nodeName);
        if (index >= 0) {
            index += NODE_NAME.length();
            return index <= str.length() ? str.substring(index) : "";
        }
        return str;
    }

}
