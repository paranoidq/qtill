package me.qtill.netty.client;

import com.google.common.base.Preconditions;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.socket.oio.OioSocketChannel;

import java.util.concurrent.TimeUnit;

/**
 * Netty客户端构造辅助类
 * <p>
 * 1. 该构造器整合了{@link Bootstrap}的各种配置和功能，并且提供了默认值
 * 2. 可配置参数
 * 3. 设置了发送区和接收区缓存，避免上层代码无界导致的拥堵和资源耗尽
 * 4. 可共享EventLoopGroup
 *
 * @author paranoidq
 * @since 1.0.0
 */
public final class NettyClientBootstrapBuilder {

    // Bootstrap实例
    private Bootstrap      bootstrap;
    // EventLoopGroup实例
    private EventLoopGroup eventLoopGroup;
    // 是否共享EventLoopGroup
    private boolean        sharedEventLoopGroup          = false;
    // 基本配置
    private boolean        useEpoll                      = false;
    private boolean        keepAlive                     = true;
    private int            connectTimeMillis             = 1000;
    private int            socketTimeSeconds             = 5;
    private boolean        tcpNoDelay                    = true;
    private int            eventLoopThreads              = 5;
    // 写缓冲区配置
    private int            writeBufferLowWaterMarkBytes  = 32 * 1024;
    private int            writeBufferHighWaterMarkBytes = 64 * 1024;
    // 读缓冲区配置
    private int            recvByteBufSizeMin            = 64;
    private int            recvByteBufSizeInit           = 1024;
    private int            recvByteBufSizeMax            = 65536;
    private int            recvBufferSizeBytes           = recvByteBufSizeInit;


    /**
     * 构造Bootstrap实例
     * @return
     */
    public Bootstrap build() {
        Class<? extends SocketChannel> channelClass = NioSocketChannel.class;
        if (!sharedEventLoopGroup) {
            this.eventLoopGroup = new NioEventLoopGroup(eventLoopThreads);
            if (useEpoll) {
                channelClass = EpollSocketChannel.class;
                this.eventLoopGroup = new EpollEventLoopGroup(eventLoopThreads);
            }
        }
        bootstrap.channel(channelClass);

        bootstrap.option(ChannelOption.SO_KEEPALIVE, keepAlive)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeMillis)
            // SO_TIMEOUT只有在OIO的模式下才有效
            .option(ChannelOption.SO_TIMEOUT, (channelClass.isAssignableFrom(OioSocketChannel.class)) ? socketTimeSeconds : null)
            .option(ChannelOption.TCP_NODELAY, tcpNoDelay)

            // 控制当socket层阻塞时，应用层的buffer不会无限增长。应用层可以通过channel.isWritable判断是否继续写数据
            .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(writeBufferLowWaterMarkBytes, writeBufferHighWaterMarkBytes))
            .option(ChannelOption.AUTO_READ, true)
            .option(ChannelOption.SO_RCVBUF, recvBufferSizeBytes)
            .option(ChannelOption.RCVBUF_ALLOCATOR,
                    new AdaptiveRecvByteBufAllocator(recvByteBufSizeMin, recvByteBufSizeInit, recvByteBufSizeMax));

        bootstrap.group(eventLoopGroup);
        return bootstrap;
    }



    /**
     * 获取NettyClientBootstrapBuilder实例
     * <p>
     * 初始化默认参数，新建EventLoopGroup
     *
     * @return
     */
    public static NettyClientBootstrapBuilder getInstance() {
        return new NettyClientBootstrapBuilder();
    }

    /**
     * 获取NettyClientBootstrapBuilder实例，并传入指定的EventLoopGroup
     * <p>
     * 初始化默认参数，共享传入的EventLoopGroup
     *
     * @param eventLoopGroup
     * @return
     */
    public static NettyClientBootstrapBuilder getInstance(EventLoopGroup eventLoopGroup) {
        return new NettyClientBootstrapBuilder(eventLoopGroup);
    }

    private NettyClientBootstrapBuilder() {
        this.bootstrap = new Bootstrap();
    }

    private NettyClientBootstrapBuilder(EventLoopGroup eventLoopGroup) {
        this();
        this.eventLoopGroup = eventLoopGroup;
        // 共享EventLoopGroup
        sharedEventLoopGroup = true;
    }

    /**
     * 是否底层采用Epoll
     * 该设置只对Linux有效
     *
     * @param useEpoll
     * @return
     */
    public NettyClientBootstrapBuilder useEpoll(boolean useEpoll) {
        this.useEpoll = useEpoll;
        return this;
    }

    /**
     * 设置是否采用长连接
     *
     * @param keepAlive
     * @return
     */
    public NettyClientBootstrapBuilder keepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

    /**
     * 设置连接超时时间
     *
     * @param connectTimeMillis
     * @return
     */
    public NettyClientBootstrapBuilder connectTimeMillis(int connectTimeMillis) {
        if (connectTimeMillis >= 0) {
            this.connectTimeMillis = connectTimeMillis;
        }
        return this;
    }

    /**
     * 设置传输超时时间
     *
     * @param socketTimeMillis
     * @return
     */
    public NettyClientBootstrapBuilder socketTimeMillis(int socketTimeMillis) {
        if (socketTimeMillis >= 0) {
            this.socketTimeSeconds = (int) TimeUnit.MILLISECONDS.toSeconds(socketTimeMillis);
        }
        return this;
    }

    /**
     * 设置EventLoopGroup线程数
     * 如果采用了共享EventLoopGroup，则该设置无效
     *
     * @param nThreads
     * @return
     */
    public NettyClientBootstrapBuilder eventLoopThreads(int nThreads) {
        Preconditions.checkState(!sharedEventLoopGroup, "Use shared EventLoopGroup, cannot configure eventLoopThreads");
        this.eventLoopThreads = nThreads;
        return this;
    }

    public NettyClientBootstrapBuilder tcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
        return this;
    }

    public NettyClientBootstrapBuilder writeBufferLowWaterMarkBytes(int writeBufferLowWaterMarkBytes) {
        this.writeBufferLowWaterMarkBytes = writeBufferLowWaterMarkBytes;
        return this;
    }

    public NettyClientBootstrapBuilder writeBufferHighWaterMarkBytes(int writeBufferHighWaterMarkBytes) {
        this.writeBufferHighWaterMarkBytes = writeBufferHighWaterMarkBytes;
        return this;
    }

    public NettyClientBootstrapBuilder recvByteBufSizeMin(int recvByteBufSizeMin) {
        this.recvByteBufSizeMin = recvByteBufSizeMin;
        return this;
    }

    public NettyClientBootstrapBuilder recvByteBufSizeInit(int recvByteBufSizeInit) {
        this.recvByteBufSizeInit = recvByteBufSizeInit;
        return this;
    }

    public NettyClientBootstrapBuilder recvByteBufSizeMax(int recvByteBufSizeMax) {
        this.recvByteBufSizeMax = recvByteBufSizeMax;
        return this;
    }

    public NettyClientBootstrapBuilder recvBufferSizeBytes(int recvBufferSizeBytes) {
        this.recvBufferSizeBytes = recvBufferSizeBytes;
        return this;
    }
}
