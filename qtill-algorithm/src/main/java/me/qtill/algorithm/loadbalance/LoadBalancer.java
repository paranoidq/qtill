package me.qtill.algorithm.loadbalance;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public interface LoadBalancer<K, T> {

    Resource<T> get(K key);

    void add(Resource<T> resource);

    void remove(Resource<T> resource);

}
