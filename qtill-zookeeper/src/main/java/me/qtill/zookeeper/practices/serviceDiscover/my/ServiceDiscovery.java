package me.qtill.zookeeper.practices.serviceDiscover.my;

import java.util.Collection;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public interface ServiceDiscovery {

    public Service getService(String name);

    public Service getService(String name, ServiceSelector selector);

    public Collection<Service> getAllServices(String name);

}
