package me.qtill.zookeeper.practices.loadbalance;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public interface BalanceUpdateProvider {

    boolean addBalance(Integer step);

    boolean reduceBalance(Integer step);

}
