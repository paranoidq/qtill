package me.qtill.netty.client;

import com.google.common.base.Throwables;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import me.qtill.netty.handler.HandlerAutoBindProcessor;
import me.qtill.netty.pool.NettyChannelPoolHandler;
import me.qtill.netty.util.connection.AutoReconnectionHandler;
import me.qtill.netty.util.connection.ConnectCommand;
import me.qtill.netty.util.heartbeat.HeartbeatHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * 1. 支持参数可配置化
 * 2. 支持心跳包
 * 3. 支持断线自动重连
 * 4. 支持异步发送回调
 *
 * TODO:
 * - 支持同步异步发送，（当前只支持异步发送）
 *
 *
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class NettyPoolClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyPoolClient.class);

    private Bootstrap bootstrap;

    // 远端信息
    private String remoteIp;
    private int    remotePort;

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

    // 自动报文重发
    private boolean enableAutoResend   = false;
    private int     autoResendMaxTimes = 3;

    // Handler自动绑定处理器
    private HandlerAutoBindProcessor handlerAutoBindProcessor;

    // 连接池
    private ChannelPool channelPool;


    private NettyPoolClient(Bootstrap bootstrap, String remoteIp, int remotePort) {
        this.bootstrap = bootstrap;
        this.remoteIp = remoteIp;
        this.remotePort = remotePort;
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
        return new Builder(bootstrap, remoteIp, remotePort);
    }


    /**
     * NettyPoolClient Builder
     */
    public static class Builder {
        private NettyPoolClient client;

        public NettyPoolClient build() {
            client.bootstrap.remoteAddress(client.remoteIp, client.remotePort);
            client.bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {

//                    // 自动重连
//                    if (client.enableAutoReconnect) {
//                        ch.pipeline().addLast(new AutoReconnectionHandler().enableAutoReconnect(client.autoReconnectMaxTimes,
//                            new ConnectCommand() {
//                                @Override
//                                public ChannelFuture doConnect() {
//                                    return client.doConnect();
//                                }
//                            })
//                        );
//                    }

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
                    if (client.handlerAutoBindProcessor != null) {
                        client.handlerAutoBindProcessor.autoBind(ch.pipeline());
                    }
                }
            });

            this.client.channelPool = new FixedChannelPool(
                client.bootstrap, new NettyChannelPoolHandler(),
                ChannelHealthChecker.ACTIVE, FixedChannelPool.AcquireTimeoutAction.NEW,
                1000,
                2,
                1,
                true,
                true
            );
            return this.client;
        }


        public Builder(Bootstrap bootstrap, String remoteIp, int remotePort) {
            this.client = new NettyPoolClient(bootstrap, remoteIp, remotePort);
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

        public Builder heartBeatCheckTolerance(int heartBeatCheckTolerance) {
            client.heartBeatCheckTolerance = heartBeatCheckTolerance;
            return this;
        }

        public Builder handlerAutoBindProcessor(HandlerAutoBindProcessor processor) {
            client.handlerAutoBindProcessor = processor;
            return this;
        }
    }

    /**
     * 异步发送消息，通过回调函数处理结果
     *
     * @param msg
     */
    public void send(byte[] msg, SendCallback callback) {
        Future<Channel> acquireFuture = channelPool.acquire();
        // acquireFuture添加回调
        acquireFuture.addListener(new FutureListener<Channel>() {
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
}
