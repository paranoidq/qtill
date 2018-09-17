package me.qtill.zookeeper.practices.serviceDiscover.my.impl;


import me.qtill.zookeeper.practices.serviceDiscover.my.Service;
import me.qtill.zookeeper.practices.serviceDiscover.my.ServiceRegistry;
import me.qtill.zookeeper.practices.serviceDiscover.my.serialization.SerializerType;
import me.qtill.zookeeper.practices.serviceDiscover.my.serialization.SerializerUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;

import java.util.TreeMap;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class ServiceRegistryImpl implements ServiceRegistry {

    private CuratorFramework client;
    private String           basePath;

    public ServiceRegistryImpl(CuratorFramework client, String basePath) {
        this.client = client;
        this.basePath = basePath;
    }

    @Override
    public void registerService(Service service) {
        String serviceName = service.getName();
        String instancePath = ZKPaths.makePath(basePath, serviceName, String.valueOf(service.getId()));
        try {
            byte[] data = SerializerUtil.getSerializer(SerializerType.JDK).serialize(service);
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(instancePath, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
