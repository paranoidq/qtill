package me.qtill.zookeeper.practices.loadbalance;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public interface RegiserProvider {

    void register(Object context) throws Exception;

    void unregister(Object context) throws Exception;
}
