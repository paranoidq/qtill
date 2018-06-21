package me.qtill.netty.handler;

import io.netty.channel.ChannelHandler;

/**
 * 封装自定义ChannelHandler的元信息
 *
 * 元信息包括：Class对象、ChannelHandler名、ChannelHandler排序位置
 * {@link ChannelHandlerAutoBindProcessor}根据元信息自动添加标注了{@link ChannelHandlerAutoBind}注解的ChannelHandler实现类到Netty的handler链中
 *
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class ChannelHandlerMeta {

    // 自定义ChannelHandler类
    private Class<? extends ChannelHandler> handler;
    // 自定义ChannelHandler类注册到Netty时的名称，默认为空
    private String                          name;
    // 自定义的ChannelHandler类注册到Netty时的次序，越小值越先注册
    private int                             index;


    public ChannelHandlerMeta(Class<? extends ChannelHandler> handler, String name, int index) {
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
