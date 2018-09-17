package me.qtill.zookeeper.practices.serviceDiscover;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class ServerPayload {

    private String name;
    private int    id;


    public ServerPayload(String name, int id) {
        this.name = name;
        this.id = id;
    }
}
