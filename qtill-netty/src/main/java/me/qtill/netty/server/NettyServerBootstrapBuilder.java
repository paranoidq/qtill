
package me.qtill.netty.server;


import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.oio.OioSocketChannel;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Netty服务端构造器
 *
 * 1. 该构造器整合了{@link Bootstrap}的各种配置和功能，并且提供了默认值
 * 2. 允许可配置
 * 3. 设置了发送区和接收区缓存，避免上层代码无界导致的拥堵和资源耗尽
 * 4. 可共享acceptEventLoopGroup和ioEventLoopGroup
 *
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class NettyServerBootstrapBuilder {


    private boolean sharedEventLoopGroup;
    private EventLoopGroup bossEventLoopGroup;
    private EventLoopGroup workerEventLoopGroup;
    private ServerBootstrap serverBootstrap;

    private boolean useEpoll;
    private boolean keepAlive;
    private int socketTimeSeconds;
    private boolean tcpNoDelay;
    private int bossEventLoopThreads;
    private int backlog;
    private int workerEventLoopThreads;

    private int writeBufferLowWaterMarkBytes;
    private int writeBufferHighWaterMarkBytes;

    private int recvByteBufSizeMin;
    private int recvByteBufSizeInit;
    private int recvByteBufSizeMax;

    private int recvBufferSizeBytes;

    private List<ChannelHandler> bossHandlers;
    private List<ChannelHandler> workerHandlers;


    public static NettyServerBootstrapBuilder getInstance() {
        return new NettyServerBootstrapBuilder();
    }

    /**
     * 指定EventLoop共享
     * @param acceptEventLoopGroup
     * @param ioEventLoopGroup
     * @return
     */
    public static NettyServerBootstrapBuilder getInstance(EventLoopGroup acceptEventLoopGroup, EventLoopGroup ioEventLoopGroup) {
        return new NettyServerBootstrapBuilder(acceptEventLoopGroup, ioEventLoopGroup);
    }

    /**
     * 私有构造函数，赋默认值
     */
    private NettyServerBootstrapBuilder() {
        this.serverBootstrap = new ServerBootstrap();
        this.useEpoll = false;
        this.keepAlive = true;
        this.socketTimeSeconds = 5;
        this.tcpNoDelay = true;
        this.bossEventLoopThreads = 2;
        this.workerEventLoopThreads = 8;
        this.backlog = 1024;
        this.writeBufferLowWaterMarkBytes = 32 * 1024;
        this.writeBufferHighWaterMarkBytes = 64 * 1024;
        this.recvByteBufSizeMin = 64;
        this.recvByteBufSizeInit = 1024;
        this.recvByteBufSizeMax = 65536;

        this.recvBufferSizeBytes = recvByteBufSizeInit;

        this.bossHandlers = new LinkedList<>();
        this.workerHandlers = new LinkedList<>();
    }

    private NettyServerBootstrapBuilder(EventLoopGroup bossEventLoopGroup, EventLoopGroup workerEventLoopGroup) {
        this();
        sharedEventLoopGroup = true;
        this.bossEventLoopGroup = bossEventLoopGroup;
        this.workerEventLoopGroup = workerEventLoopGroup;
    }

    public NettyServerBootstrapBuilder useEpoll(boolean useEpoll) {
        this.useEpoll = useEpoll;
        return this;
    }

    public NettyServerBootstrapBuilder keepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }


    public NettyServerBootstrapBuilder socketTimeMillis(int socketTimeMillis) {
        this.socketTimeSeconds = (int) TimeUnit.MILLISECONDS.toSeconds(socketTimeMillis);
        return this;
    }

    public NettyServerBootstrapBuilder bossEventLoopThreads(int nThreads) {
        if (sharedEventLoopGroup) {
            throw new IllegalArgumentException("Use shared EventLoopGroup, cannot configure eventLoopThreads");
        }
        this.bossEventLoopThreads = nThreads;
        return this;
    }

    public NettyServerBootstrapBuilder workerEventLoopThreads(int nThreads) {
        if (sharedEventLoopGroup) {
            throw new IllegalArgumentException("Use shared EventLoopGroup, cannot configure eventLoopThreads");
        }
        this.workerEventLoopThreads = nThreads;
        return this;
    }

    public NettyServerBootstrapBuilder backlog(int soBackLog) {
        this.backlog = soBackLog;
        return this;
    }

    public NettyServerBootstrapBuilder tcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
        return this;
    }

    public NettyServerBootstrapBuilder writeBufferLowWaterMarkBytes(int writeBufferLowWaterMarkBytes) {
        this.writeBufferLowWaterMarkBytes = writeBufferLowWaterMarkBytes;
        return this;
    }

    public NettyServerBootstrapBuilder writeBufferHighWaterMarkBytes(int writeBufferHighWaterMarkBytes) {
        this.writeBufferHighWaterMarkBytes = writeBufferHighWaterMarkBytes;
        return this;
    }

    public NettyServerBootstrapBuilder recvByteBufSizeMin(int recvByteBufSizeMin) {
        this.recvByteBufSizeMin = recvByteBufSizeMin;
        return this;
    }

    public NettyServerBootstrapBuilder recvByteBufSizeInit(int recvByteBufSizeInit) {
        this.recvByteBufSizeInit = recvByteBufSizeInit;
        return this;
    }

    public NettyServerBootstrapBuilder setRecvByteBufSizeMax(int recvByteBufSizeMax) {
        this.recvByteBufSizeMax = recvByteBufSizeMax;
        return this;
    }

    public NettyServerBootstrapBuilder recvBufferSizeBytes(int recvBufferSizeBytes) {
        this.recvBufferSizeBytes = recvBufferSizeBytes;
        return this;
    }

    public NettyServerBootstrapBuilder bossHandlers(ChannelHandler handler) {
        this.bossHandlers.add(handler);
        return this;
    }

    public NettyServerBootstrapBuilder workerHandlers(ChannelHandler handler) {
        this.workerHandlers.add(handler);
        return this;
    }


    public EventLoopGroup getBossEventLoopGroup() {
        return this.bossEventLoopGroup;
    }

    public EventLoopGroup getWorkerEventLoopGroup() {
        return this.workerEventLoopGroup;
    }


    /**
     * 构造ServerBootStrap实例
     *
     * @return
     */
    public ServerBootstrap build() {
        Class<? extends ServerSocketChannel> channelClass = NioServerSocketChannel.class;
        if (this.bossEventLoopGroup == null) {
            this.bossEventLoopGroup = new NioEventLoopGroup(bossEventLoopThreads);
            this.workerEventLoopGroup = new NioEventLoopGroup(workerEventLoopThreads);
            if (useEpoll) {
                channelClass = NioServerSocketChannel.class;
                this.bossEventLoopGroup = new EpollEventLoopGroup(bossEventLoopThreads);
                this.workerEventLoopGroup = new EpollEventLoopGroup(workerEventLoopThreads);
            }
        }
        serverBootstrap.group(getBossEventLoopGroup(), getWorkerEventLoopGroup());
        serverBootstrap.channel(channelClass);
        serverBootstrap
            .option(ChannelOption.SO_BACKLOG, backlog)
            .childOption(ChannelOption.SO_KEEPALIVE, keepAlive)
            // SO_TIMEOUT只有在OIO的模式下才有效
            .option(ChannelOption.SO_TIMEOUT, (channelClass.isAssignableFrom(OioSocketChannel.class)) ? socketTimeSeconds : null)
            .childOption(ChannelOption.TCP_NODELAY, tcpNoDelay)
            // 控制当socket层阻塞时，应用层的buffer不会无限增长。应用层可以通过channel.isWritable判断是否继续写数据
            .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(writeBufferLowWaterMarkBytes, writeBufferHighWaterMarkBytes))

            // 打开会自动读取，关闭之后不会读取，数据会停留在socket网络层，这样做有利于通过TCP自动的flow control机制控制滑动窗口大小，从而限制对端的发送速率
            // 要有高低水位机制，不要一超过阈值就关闭或打开AUTO_READ，会造成性能损耗
            .childOption(ChannelOption.AUTO_READ, true)
            .childOption(ChannelOption.SO_RCVBUF, recvBufferSizeBytes)
            .childOption(ChannelOption.RCVBUF_ALLOCATOR,
                new AdaptiveRecvByteBufAllocator(recvByteBufSizeMin, recvByteBufSizeInit, recvByteBufSizeMax))
        ;
        return serverBootstrap;
    }


}
