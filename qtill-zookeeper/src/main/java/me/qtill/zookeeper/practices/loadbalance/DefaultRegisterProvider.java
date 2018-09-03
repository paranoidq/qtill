package me.qtill.zookeeper.practices.loadbalance;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class DefaultRegisterProvider implements RegiserProvider {
    @Override
    public void register(Object context) throws Exception {
        ZookeeperRegisterContext registerContext = (ZookeeperRegisterContext) context;

        String path = registerContext.getPath();
        ZkClient zc = registerContext.getZkClient();

        try {
            zc.createEphemeral(path, registerContext.getData());
        } catch (ZkNoNodeException e) {
            String parentDir = path.substring(0, path.lastIndexOf('/'));
            zc.createPersistent(parentDir, true);
            register(registerContext);
        }
    }

    @Override
    public void unregister(Object context) throws Exception {
        // TODO
    }
}
