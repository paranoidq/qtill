package me.qtill.zookeeper.practices.loadbalance;

import java.io.Serializable;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class ServerData implements Serializable, Comparable<ServerData> {

    private static final long serialVersionUID = -243916616067621127L;

    private Integer balance;
    private String  host;
    private Integer port;


    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public int compareTo(ServerData o) {
        return this.getBalance().compareTo(o.getBalance());
    }
}
