package me.qtill.algorithm.loadbalance;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class HashLoaderBalancer<K, T> implements LoadBalancer<K, T> {

    private List<Resource<T>> cache;

    public HashLoaderBalancer() {
        this.cache = Lists.newArrayList();
    }

    @Override
    public Resource<T> get(K key) {
        int hash = hash(key);
        return !cache.isEmpty() ? cache.get(hash % cache.size()) : null;
    }

    @Override
    public void add(Resource<T> resource) {
        cache.add(resource);
    }

    @Override
    public void remove(Resource<T> resource) {
        cache.remove(resource);
    }

    private int hash(K key) {
        // TODO: generate hash according to key
        return -1;
    }
}
