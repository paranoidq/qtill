package me.qtill.zookeeper.practices.serviceDiscover.my.impl;

import com.google.common.collect.Lists;
import me.qtill.zookeeper.practices.serviceDiscover.my.Service;
import me.qtill.zookeeper.practices.serviceDiscover.my.ServiceDiscovery;
import me.qtill.zookeeper.practices.serviceDiscover.my.ServiceSelector;
import me.qtill.zookeeper.practices.serviceDiscover.my.serialization.SerializerType;
import me.qtill.zookeeper.practices.serviceDiscover.my.serialization.SerializerUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;

import java.util.Collection;
import java.util.List;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class ServiceDiscoveryImpl implements ServiceDiscovery {

    private CuratorFramework client;
    private String           basePath;
    private ServiceSelector  serviceSelector = new DefaultServiceSelector();


    public ServiceDiscoveryImpl(CuratorFramework client, String basePath) {
        this.client = client;
        this.basePath = basePath;
    }

    public ServiceDiscoveryImpl(CuratorFramework client, String basePath, ServiceSelector serviceSelector) {
        this(client, basePath);
        this.serviceSelector = serviceSelector;
    }

    @Override
    public Service getService(String name) {
        try {
            // 调用selector进行选择
            return serviceSelector.select(candidates(name));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Service getService(String name, ServiceSelector prodvidedSelector) {
        try {
            return prodvidedSelector.select(candidates(name));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Collection<Service> getAllServices(String name) {
        try {
            return candidates(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Collection<Service> candidates(String name) throws Exception {
        // 获取所有的service下的所有顺序子节点
        String znodePath = ZKPaths.makePath(basePath, name);
        List<String> childrenPath = client.getChildren().forPath(znodePath);

        List<Service> candidates = Lists.newArrayList();
        for (String childPath : childrenPath) {
            // 获取的children是相对路径
            byte[] data = client.getData().forPath(ZKPaths.makePath(znodePath, childPath));
            Service service = SerializerUtil.getSerializer(SerializerType.JDK).deserialize(data, Service.class);
            candidates.add(service);
        }
        return candidates;
    }
}
