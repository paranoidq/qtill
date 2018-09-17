package me.qtill.zookeeper.practices.serviceDiscover;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;


/**
 * @author paranoidq
 * @since 1.0.0
 */
public class ServiceDiscoverSample {

    private static final String path        = "/examples/services";
    private static final String serviceName = "serviceX";


    public static void main(String[] args) throws Exception {
        TestingServer server = new TestingServer();

        CuratorFramework client = CuratorFrameworkFactory.newClient(
            server.getConnectString(), new ExponentialBackoffRetry(1000, 3)
        );
        client.start();

        ServiceDiscovery<ServerPayload> registry = ServiceDiscoveryBuilder.builder(ServerPayload.class)
            .client(client)
            .serializer(new JsonInstanceSerializer<>(ServerPayload.class))
            .basePath(path)
            .build();

        ServiceInstance<ServerPayload> serviceInstance = ServiceInstance.<ServerPayload>builder()
            .id("host1")
            .name(serviceName)
            .port(21888)
            .address("10.99.10.1")
            .payload(new ServerPayload("HZ", 5))
            .uriSpec(new UriSpec("{scheme}://{address}:{port}"))
            .build();

        registry.registerService(serviceInstance);


        Collection<ServiceInstance<ServerPayload>> serviceInstances = registry.queryForInstances(serviceName);
        Iterator<ServiceInstance<ServerPayload>> iterator = serviceInstances.iterator();
        while (iterator.hasNext()) {
            ServiceInstance<ServerPayload> instance = iterator.next();

        }
    }
}
