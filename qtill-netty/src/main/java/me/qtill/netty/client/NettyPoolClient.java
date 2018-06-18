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
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import me.qtill.netty.handler.HandlerAutoBindProcessor;
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
 * <p>
 * TODO:
 * - 支持同步异步发送，（当前只支持异步发送）
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class NettyPoolClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyPoolClient.class);

    private Bootstrap bootstrap;

    // 远端信息
    private InetSocketAddress remoteAddress;
    // 本端信息
    private InetSocketAddress localAddress;

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
    private long    heartBeatCheckPeriod    = 5000;
    private int     heartBeatCheckTolerance = 3;

    // Handler自动绑定处理器处理的package
    private String handleAutoProcessPackage;

    // 连接池
    private ChannelPool channelPool;
    // 保持连接数，默认为
    private int         keptConnections      = 1;
    // 连接池acquire等待最大毫米
    private long        acquireTimeoutMillis = 1000;
    // 最大acquire排队等待数
    private int         maxPendingAcquires   = 1;


    /**
     * 构造函数
     * @param bootstrap
     * @param remoteAddress
     */
    private NettyPoolClient(Bootstrap bootstrap, InetSocketAddress remoteAddress) {
        this.bootstrap = bootstrap;
        this.remoteAddress = remoteAddress;
    }

    /**
     * Builder模式
     * @param bootstrap
     * @param remoteIp
     * @param remotePort
     * @return
     */
    public static Builder builder(Bootstrap bootstrap, java.lang.String remoteIp, int remotePort) {
        return builder(bootstrap, new InetSocketAddress(remoteIp, remotePort));
    }


    /**
     * Builder模式
     * @param bootstrap
     * @param address
     * @return
     */
    public static Builder builder(Bootstrap bootstrap, InetSocketAddress address) {
        return new Builder(bootstrap, address);
    }


    /**
     * 启动Netty客户端
     */
    public void start() {
        if (keptConnections > 1) {
            final CountDownLatch latch = new CountDownLatch(keptConnections);
            logger.info("*** NettyPoolClient starting ...");

            final List<Channel> preConnects = Lists.newArrayListWithExpectedSize(keptConnections);
            // 事先建立所有连接
            for (int i = 0; i < keptConnections; i++) {
                channelPool.acquire().addListener(new FutureListener<Channel>() {
                    @Override
                    public void operationComplete(Future<Channel> future) throws Exception {
                        if (future.isSuccess()) {
                            Channel channel = future.getNow();
                            preConnects.add(channel);
                            latch.countDown();  // 成功建立1个连接
                            logger.info("*** Channel-[{}] established", channel.id());
                        } else {
                            logger.error("*** Channel establish failed. Cause: {}", Throwables.getStackTraceAsString(future.cause()));
                        }
                    }
                });
            }

            try {
                latch.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.error("*** NettyPoolClient started failed. Given [{}], but only [{}] connections established", keptConnections, preConnects.size());
            }

            // 返还连接池，等待取用
            for (Channel channel : preConnects) {
                channelPool.release(channel);
            }
            logger.info("*** NettyPoolClient started success");
        }
    }

    /**
     * 停止Netty客户端
     */
    public void stop() {
        if (channelPool != null) {
            channelPool.close();
            channelPool = null;
        }
    }


    /**
     * 获取链路
     * 该函数为同步调用，无限等待获取到channel，除非发生异常情况
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public Channel acquireChannel() throws ExecutionException, InterruptedException {
        Preconditions.checkState(channelPool != null, "ChannelPool is null");
        return channelPool.acquire().get();
    }

    /**
     * 获取链路
     * 该函数为同步调用，除非超过了规定的超时时间或发生异常
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws ExecutionException
     */
    public Channel acquireChannel(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException, ExecutionException {
        Preconditions.checkState(channelPool != null, "ChannelPool is null");
        return channelPool.acquire().get(timeout, unit);
    }

    /**
     * 释放链路到channelPool中
     * @param channel
     */
    public void releaseChannel(Channel channel) {
        Preconditions.checkState(channelPool != null, "ChannelPool is null");
        Future<Void> releaseFuture = channelPool.release(channel);
        releaseFuture.addListener(new FutureListener<Void>() {
            @Override
            public void operationComplete(Future<Void> future) throws Exception {
                logger.info("*** Channel-[{}] released to pool", channel.id());
            }
        });

    }


    /**
     * 异步发送消息，通过回调函数处理结果
     *
     * @param msg
     */
    public void send(byte[] msg, SendCallback callback) {
        Future<Channel> future = channelPool.acquire();
        // acquireFuture添加回调
        future.addListener(new FutureListener<Channel>() {
            @Override
            public void operationComplete(Future<Channel> acquireFuture) {
                if (acquireFuture.isSuccess()) {
                    // Channel获取成功
                    Channel channel = null;
                    try {
                        channel = acquireFuture.getNow();
                        final ByteBuf messageBuf = Unpooled.copiedBuffer(msg);
                        ChannelFuture writeFuture = channel.writeAndFlush(messageBuf);

                        // writeFuture添加回调
                        writeFuture.addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture writeFuture) throws Exception {
                                if (writeFuture.isSuccess()) {
                                    // TODO: 这里面retain调用会报错，初步估计因为异步的关系，messageBuf已经被回收了
                                    logger.info("*** NettyPoolClient send success: [{}]", ByteBufUtil.hexDump(msg));
                                    if (callback != null) {
                                        callback.onSuccess(writeFuture);
                                    }
                                } else {
                                    logger.error("*** NettyPoolClient send failed: [{}]. Cause: {}", ByteBufUtil.hexDump(msg), Throwables.getStackTraceAsString(writeFuture.cause()));
                                    if (callback != null) {
                                        callback.onFailed(writeFuture);
                                    }
                                }
                            }
                        });
                    } finally {
                        channelPool.release(channel);
                    }
                } else {
                    logger.error("*** NettyPoolClient channel acquire failed. Cause: {}", Throwables.getStackTraceAsString(acquireFuture.cause()));
                    if (enableAutoReconnect) {
                        channelPool.acquire();
                    }
                }
            }
        });
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
     * 指定Channel的异步消息发送，支持callback
     *
     * 该方法不会release链路，需要调用{@link NettyPoolClient#releaseChannel(Channel)}主动关闭，否则会造成链路一直被占用
     * @param msg
     * @param callback
     * @param channel
     */
    public void sendByChannel(byte[] msg, SendCallback callback, Channel channel) {
        final ByteBuf messageBuf = Unpooled.copiedBuffer(msg);
        ChannelFuture writeFuture = channel.writeAndFlush(messageBuf);

        // writeFuture添加回调
        writeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture writeFuture) throws Exception {
                if (writeFuture.isSuccess()) {
                    // TODO: 这里面retain调用会报错，初步估计因为异步的关系，messageBuf已经被回收了
                    logger.info("*** NettyPoolClient send success: [{}]", ByteBufUtil.hexDump(msg));
                    if (callback != null) {
                        callback.onSuccess(writeFuture);
                    }
                } else {
                    logger.error("*** NettyPoolClient send failed: [{}]. Cause: {}", ByteBufUtil.hexDump(msg), Throwables.getStackTraceAsString(writeFuture.cause()));
                    if (callback != null) {
                        callback.onFailed(writeFuture);
                    }
                }
            }
        });
    }

    /**
     * 指定channel的异步消息发送，不支持callback
     *
     * 该方法不会release链路，需要调用{@link NettyPoolClient#releaseChannel(Channel)}主动关闭，否则会造成链路一直被占用
     * @param msg
     * @param channel
     */
    public void sendByChannel(byte[] msg, Channel channel) {
        sendByChannel(msg, null, channel);
    }


    /**
     * 同步发送消息，等待消息发送成功
     * 必须先通过{@link NettyPoolClient#acquireChannel()}获取Channel
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
     * NettyPoolClient Builder
     */
    public static class Builder {
        private NettyPoolClient client;

        public NettyPoolClient build() {
            client.bootstrap.remoteAddress(client.remoteAddress);
            if (client.localAddress != null) {
                client.bootstrap.localAddress(client.localAddress);
            }

            // 构建channelPool
            client.channelPool = new FixedChannelPool(
                client.bootstrap, new NettyChannelPoolHandler(),
                ChannelHealthChecker.ACTIVE, FixedChannelPool.AcquireTimeoutAction.NEW,
                1000,
                client.keptConnections,
                1,
                true,
                true
            );
            return this.client;
        }


        public Builder(Bootstrap bootstrap, InetSocketAddress remoteAddress) {
            this.client = new NettyPoolClient(bootstrap, remoteAddress);
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

        public Builder heartbeatSendToken(java.lang.String heartbeatToken) {
            client.heartbeatSendToken = Unpooled.directBuffer().writeBytes(heartbeatToken.getBytes());
            return this;
        }

        public Builder heartbeatCheckToken(java.lang.String heartbeatToken) {
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

        public Builder heartBeatCheckTolerance(int heartBeatCheckTolerance) {
            client.heartBeatCheckTolerance = heartBeatCheckTolerance;
            return this;
        }

        public Builder handlerAutoBindProcessor(String handleAutoProcessPackage) {
            client.handleAutoProcessPackage = handleAutoProcessPackage;
            return this;
        }

        public Builder keptConnections(int keptConnections) {
            Preconditions.checkState(keptConnections >= 1, "keptConnections must be at least 1");
            client.keptConnections = keptConnections;
            return this;
        }

        public Builder acquireTimeoutMillis(int acquireTimeoutMillis) {
            client.acquireTimeoutMillis = acquireTimeoutMillis;
            return this;
        }

        public Builder maxPendingAcquires(int maxPendingAcquires) {
            Preconditions.checkState(maxPendingAcquires >= 1, "keptConnections must be at least 1");
            client.maxPendingAcquires = maxPendingAcquires;
            return this;
        }


        /**
         * 连接池
         */
        private class NettyChannelPoolHandler extends AbstractChannelPoolHandler {
            @Override
            public void channelCreated(Channel ch) throws Exception {
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

                // 自动添加handler
                if (!StringUtils.isEmpty(client.handleAutoProcessPackage)) {
                    new HandlerAutoBindProcessor(client.handleAutoProcessPackage).autoBind(ch.pipeline());
                }
            }
        }
    }
}
