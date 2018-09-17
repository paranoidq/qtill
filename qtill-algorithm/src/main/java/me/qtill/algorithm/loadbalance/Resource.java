package me.qtill.algorithm.loadbalance;

import java.util.UUID;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class Resource<T> {

    private String resourceId;
    private T      payload;

    public Resource(T payload) {
        this.payload = payload;
        resourceId = UUID.randomUUID().toString();
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public String getResourceId() {
        return resourceId;
    }

    @Override
    public boolean equals(Object obj) {
        // TODO
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        // TODO
        return super.hashCode();
    }
}
