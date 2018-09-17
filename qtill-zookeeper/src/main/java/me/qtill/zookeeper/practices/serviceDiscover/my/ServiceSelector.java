package me.qtill.zookeeper.practices.serviceDiscover.my;

import java.util.Collection;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public interface ServiceSelector {

    public Service select(Collection<Service> candidates);

}
