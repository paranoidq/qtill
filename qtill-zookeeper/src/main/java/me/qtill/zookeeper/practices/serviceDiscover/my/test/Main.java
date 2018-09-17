package me.qtill.zookeeper.practices.serviceDiscover.my.test;

import me.qtill.zookeeper.practices.serviceDiscover.my.Service;
import me.qtill.zookeeper.practices.serviceDiscover.my.ServiceDiscovery;
import me.qtill.zookeeper.practices.serviceDiscover.my.ServiceRegistry;
import me.qtill.zookeeper.practices.serviceDiscover.my.impl.ServiceDiscoveryImpl;
import me.qtill.zookeeper.practices.serviceDiscover.my.impl.ServiceRegistryImpl;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class Main {

    public static final String basePath = "/example/discovery";

    public static void main(String[] args) throws Exception {
        TestingServer server = new TestingServer();
        CuratorFramework client = CuratorFrameworkFactory.newClient(
            server.getConnectString(), new ExponentialBackoffRetry(1000, 4)
        );
        client.start();


        Service service1 = new Service(
            1, "cal", "localhost", "123"
        );
        Service service2 = new Service(
            2, "cal", "localhost", "124"
        );

        ServiceRegistry registry = new ServiceRegistryImpl(client, basePath);
        ServiceDiscovery discovery = new ServiceDiscoveryImpl(client, basePath);

        registry.registerService(service1);
        registry.registerService(service2);


//        Collection<Service> services = discovery.getAllServices("cal");
//        services.stream().forEach(service -> {
//            System.out.println("discover service: " + service.getId() + " - " + service.getIp() + ":" + service.getPort());
//        });

        int count = 10;

        for (int i = 0; i < count; i++) {
            Service service = discovery.getService("cal");
            System.out.println("discover service: " + service.getId() + " - " + service.getIp() + ":" + service.getPort());
        }
    }
}
