package me.qtill.algorithm.loadbalance.test;

import me.qtill.algorithm.loadbalance.HashLoaderBalancer;
import me.qtill.algorithm.loadbalance.LoadBalancer;
import me.qtill.algorithm.loadbalance.Resource;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class Test1 {

    public static void main(String[] args) {
        LoadBalancer<String, String> loadBalancer =
            new HashLoaderBalancer<>();

        loadBalancer.add(new Resource<>("localhost:1"));
        loadBalancer.add(new Resource<>("localhost:2"));

        loadBalancer.get("1");

    }
}
