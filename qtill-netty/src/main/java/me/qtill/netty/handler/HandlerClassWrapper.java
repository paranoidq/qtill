package me.qtill.netty.handler;

import io.netty.channel.ChannelHandler;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class HandlerClassWrapper {

    private Class<? extends ChannelHandler> handler;
    private String                          name;
    private int                             index;


    public HandlerClassWrapper(Class<? extends ChannelHandler> handler, String name, int index) {
        this.handler = handler;
        this.name = name;
        this.index = index;
    }

    public Class<? extends ChannelHandler> getHandler() {
        return handler;
    }

    public void setHandler(Class<? extends ChannelHandler> handler) {
        this.handler = handler;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
