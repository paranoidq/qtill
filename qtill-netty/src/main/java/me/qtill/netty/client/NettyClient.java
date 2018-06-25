package me.qtill.netty.client;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import me.qtill.netty.handler.ChannelHandlerAutoBindProcessor;
import me.qtill.netty.util.connection.AutoReconnectionHandler;
import me.qtill.netty.util.connection.ConnectCommand;
import me.qtill.netty.util.heartbeat.HeartbeatHandler;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 1. 支持参数可配置化
 * 2. 支持心跳包
 * 3. 支持断线自动重连
 * 4. 支持异步发送回调
 * 5. 支持同步异步发送
 *
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class NettyClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    // Netty Bootstrap实例
    private Bootstrap bootstrap;

    // 远端信息
    private InetSocketAddress remoteAddress;
    // 本端信息
    private InetSocketAddress localAddress;

    // 是否正在运行
    private volatile boolean running = false;

    // 自动断线重连
    private boolean       enableAutoReconnect         = false;
    private int           autoReconnectMaxTimes       = 5;
    private long          autoReconnectIntervalMillis = 2000;
    private AtomicInteger autoReconnectCurrentTimes   = new AtomicInteger(0);

    // 自动心跳包发送
    private boolean enableHeartbeatSend     = false;
    private boolean enableHeartbeatCheck    = false;
    private boolean normalMsgAsHeartbeat    = false;
    private ByteBuf heartbeatSendToken      = Unpooled.directBuffer().writeBytes(new byte[]{0x00, 0x00});
    private ByteBuf heartbeatCheckToken     = Unpooled.directBuffer().writeBytes(new byte[]{0x00, 0x00});
    private long    heartBeatSendPeriod     = 5000;
    private long    heartBeatCheckPeriod    = 10000;
    private int     heartBeatCheckTolerance = 3;

    // Handler自动绑定处理器处理的package
    private String channelHandlerAutoBindScanPackage;

    // 链路
    private volatile Channel channel;

    // 连续tryConnect次数
    private int tryConnectMaxTimes = 3;
    private AtomicInteger tryConnectCurrentTimes = new AtomicInteger(0);


    /**
     * 构造函数
     *
     * @param bootstrap
     * @param remoteAddress
     */
    private NettyClient(Bootstrap bootstrap, InetSocketAddress remoteAddress) {
        this.bootstrap = bootstrap;
        this.remoteAddress = remoteAddress;
    }

    /**
     * Builder模式
     *
     * @param bootstrap
     * @param remoteIp
     * @param remotePort
     * @return
     */
    public static Builder builder(Bootstrap bootstrap, String remoteIp, int remotePort) {
        return builder(bootstrap, new InetSocketAddress(remoteIp, remotePort));
    }


    /**
     * Builder模式
     *
     * @param bootstrap
     * @param address
     * @return
     */
    public static Builder builder(Bootstrap bootstrap, InetSocketAddress address) {
        return new Builder(bootstrap, address);
    }


    /**
     * 启动NettyClient
     */
    public void start() {
        logger.info("*** NettyClient starting ...");
        running = true;
        connect();
        if (channel == null) {
            logger.error("*** NettyClient started failed. Cannot connect to server");
        } else {
            logger.info("*** NettyClient started success");
        }
    }

    /**
     * 关闭NettyClient
     */
    public void shutdown() {
        if (channel != null) {
            bootstrap.config().group().shutdownGracefully().syncUninterruptibly();
            running = false;
            bootstrap = null;
            channel.close();
        }
    }

    /**
     * 发起连接，并返回连接后的Channel
     * @return
     */
    private void connect() {
        logger.info("*** NettyClient connecting (try-{})", tryConnectCurrentTimes.incrementAndGet());

        ChannelFuture connectFuture = null;
        if (localAddress == null) {
            connectFuture = bootstrap.connect(remoteAddress);
        } else {
            connectFuture = bootstrap.connect(remoteAddress, localAddress);
        }
        try {
            ChannelFuture future = connectFuture.sync();
            if (future.isSuccess()) {
                // 将值赋值给channel实例域
                this.channel = future.channel();
                // 重置tryConnect次数
                tryConnectCurrentTimes.set(0);
            } else {
                logger.error("*** NettyClient connect failed. Cause: ", future.cause());
                if (tryConnectCurrentTimes.get() <= tryConnectMaxTimes) {
                    connect();
                } else {
                    logger.error("*** NettyClient connect tried to many times, max=[{}]", tryConnectMaxTimes);
                }
            }
        } catch (InterruptedException e) {
            logger.error("*** NettyClient connect interrupted. Cause: ", e);
        }

    }

    /**
     * 异步发送消息，通过回调函数处理结果
     *
     * @param msg
     */
    public void send(byte[] msg, SendCallback callback) {
        try {
            final ByteBuf messageBuf = Unpooled.copiedBuffer(msg);
            ChannelFuture writeFuture = channel.writeAndFlush(messageBuf);

            // writeFuture添加回调
            writeFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture writeFuture) throws Exception {
                    if (writeFuture.isSuccess()) {
                        // TODO: 这里面retain调用会报错，初步估计因为异步的关系，messageBuf已经被回收了
                        logger.info("*** NettyPoolingClient send success: [{}]", ByteBufUtil.hexDump(msg));
                        if (callback != null) {
                            callback.onSuccess(writeFuture);
                        }
                    } else {
                        logger.error("*** NettyPoolingClient send failed: [{}]. Cause: {}", ByteBufUtil.hexDump(msg), Throwables.getStackTraceAsString(writeFuture.cause()));
                        if (callback != null) {
                            callback.onFailed(writeFuture);
                        }
                    }
                }
            });
        } catch (Exception e) {

        }
    }


    /**
     * 异步发送消息，不支持callback
     *
     * @param msg
     */
    public void send(byte[] msg) {
        send(msg, null);
    }

    /**
     * 同步发送消息，等待消息发送成功
     * 必须先通过{@link NettyClient#acquireChannel()}获取Channel
     *
     * @param msg
     * @param timeout
     * @param unit
     * @return
     */
    public boolean sendSync(byte[] msg, Channel channel, long timeout, TimeUnit unit) throws InterruptedException {
        Preconditions.checkArgument(channel != null, "Channel is null");
        final ByteBuf messageBuf = Unpooled.copiedBuffer(msg);
        ChannelFuture writeFuture = channel.writeAndFlush(messageBuf);
        return writeFuture.await(timeout, unit);
    }


    /**
     * NettyPoolingClient Builder
     */
    public static class Builder {
        private NettyClient client;

        public NettyClient build() {
            client.bootstrap.remoteAddress(client.remoteAddress);
            if (client.localAddress != null) {
                client.bootstrap.localAddress(client.localAddress);
            }

            client.bootstrap.handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    // 心跳包
                    ch.pipeline().addLast(new IdleStateHandler(client.heartBeatCheckPeriod, client.heartBeatSendPeriod, Math.max(
                        client.heartBeatCheckPeriod, client.heartBeatSendPeriod), TimeUnit.MILLISECONDS));
                    ch.pipeline().addLast(new HeartbeatHandler(
                        client.enableHeartbeatSend,
                        client.enableHeartbeatCheck,
                        client.normalMsgAsHeartbeat,
                        client.heartbeatSendToken,
                        client.heartbeatCheckToken,
                        client.heartBeatCheckTolerance
                    ));

                    // 添加自动重连机制
                    if (client.enableAutoReconnect) {
                        ch.pipeline().addLast(new AutoReconnectionHandler(new ConnectCommand() {
                            @Override
                            public void invoke() {
                                client.connect();
                            }
                        }));
                    }

                    // 自动添加handler
                    if (!StringUtils.isEmpty(client.channelHandlerAutoBindScanPackage)) {
                        new ChannelHandlerAutoBindProcessor(client.channelHandlerAutoBindScanPackage).autoBind(ch.pipeline());
                    }
                }
            });
            return client;
        }


        public Builder(Bootstrap bootstrap, InetSocketAddress remoteAddress) {
            this.client = new NettyClient(bootstrap, remoteAddress);
        }

        public Builder localAddress(InetSocketAddress localAddress) {
            client.localAddress = localAddress;
            return this;
        }

        public Builder enableAutoReconnect(boolean enableReconnect) {
            client.enableAutoReconnect = enableReconnect;
            return this;
        }

        public Builder autoReconnectMaxTimes(int reconnectMaxTimes) {
            client.autoReconnectMaxTimes = reconnectMaxTimes;
            return this;
        }

        public Builder autoReconnectIntervalMillis(long autoReconnectIntervalMillis) {
            client.autoReconnectIntervalMillis = autoReconnectIntervalMillis;
            return this;
        }

        public Builder enableHeartbeatSend(boolean enableHeartbeatSend) {
            client.enableHeartbeatSend = enableHeartbeatSend;
            return this;
        }

        public Builder enableHeartbeatCheck(boolean enableHeartbeatCheck) {
            client.enableHeartbeatCheck = enableHeartbeatCheck;
            return this;
        }

        public Builder normalMsgAsHeartbeat(boolean normalMsgAsHeartbeat) {
            client.normalMsgAsHeartbeat = normalMsgAsHeartbeat;
            return this;
        }

        public Builder heartbeatSendToken(String heartbeatToken) {
            client.heartbeatSendToken = Unpooled.directBuffer().writeBytes(heartbeatToken.getBytes());
            return this;
        }

        public Builder heartbeatCheckToken(String heartbeatToken) {
            client.heartbeatCheckToken = Unpooled.directBuffer().writeBytes(heartbeatToken.getBytes());
            return this;
        }

        public Builder heartBeatSendPeriodMillis(long heartBeatSendPeriod) {
            client.heartBeatSendPeriod = heartBeatSendPeriod;
            return this;
        }

        public Builder heartBeatCheckPeriodMillis(long heartBeatCheckPeriod) {
            client.heartBeatCheckPeriod = heartBeatCheckPeriod;
            return this;
        }

        /**
         * 如果heartBeatCheckTolerance大于0，则超过次数没有收到心跳包会主动断开连接
         * 如果heartBeatCheckTolerance小于等于0，则不会主动断开连接
         *
         * @param heartBeatCheckTolerance
         * @return
         */
        public Builder heartBeatCheckTolerance(int heartBeatCheckTolerance) {
            client.heartBeatCheckTolerance = heartBeatCheckTolerance;
            return this;
        }

        public Builder handlerAutoBindProcessor(String handleAutoProcessPackage) {
            client.channelHandlerAutoBindScanPackage = handleAutoProcessPackage;
            return this;
        }
    }
}
