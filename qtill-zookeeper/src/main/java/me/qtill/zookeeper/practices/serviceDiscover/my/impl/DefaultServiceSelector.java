package me.qtill.zookeeper.practices.serviceDiscover.my.impl;

import me.qtill.zookeeper.practices.serviceDiscover.my.Service;
import me.qtill.zookeeper.practices.serviceDiscover.my.ServiceSelector;

import java.util.Collection;
import java.util.Random;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class DefaultServiceSelector implements ServiceSelector {

    @Override
    public Service select(Collection<Service> candidates) {
        Random random = new Random();
        int index = random.nextInt(candidates.size());
        return candidates.toArray(new Service[0])[index];
    }
}
