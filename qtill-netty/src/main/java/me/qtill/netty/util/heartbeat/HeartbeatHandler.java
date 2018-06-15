package me.qtill.netty.util.heartbeat;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class HeartbeatHandler extends ChannelDuplexHandler {
    private static final Logger logger = Logger.getLogger(HeartbeatHandler.class.getName());

    // TODO: Netty中同一个handler只会被一个线程调用，不存在竞争问题
    private final AtomicInteger heartbeatLossCounter = new AtomicInteger(0);

    private boolean enableHeartbeatSend = true;
    private boolean enableHeartbeatCheck = false;
    private boolean normalMsgAsHeartbeat = false;
    private ByteBuf heartbeatSendToken = Unpooled.directBuffer().writeBytes(new byte[]{0x00, 0x00});
    private ByteBuf heartbeatCheckToken = Unpooled.directBuffer().writeBytes(new byte[]{0x00, 0x00});
    private int heartBeatCheckTolerance = 3;

    public HeartbeatHandler() {
    }

    public HeartbeatHandler(boolean enableHeartbeatSend, boolean enableHeartbeatCheck, boolean normalMsgAsHeartbeat, ByteBuf heartbeatSendToken, ByteBuf heartbeatCheckToken, int heartBeatCheckTolerance) {
        this.enableHeartbeatSend = enableHeartbeatSend;
        this.enableHeartbeatCheck = enableHeartbeatCheck;
        this.normalMsgAsHeartbeat = normalMsgAsHeartbeat;
        this.heartbeatSendToken = heartbeatSendToken;
        this.heartbeatCheckToken = heartbeatCheckToken;
        this.heartBeatCheckTolerance = heartBeatCheckTolerance;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (enableHeartbeatSend && e.state() == IdleState.WRITER_IDLE) {
                // write idle
                logger.info("*** Write idle, send heartbeat token [" + ByteBufUtil.hexDump(heartbeatSendToken)+ "]");
                ctx.writeAndFlush(heartbeatSendToken.retain());
            } else if (enableHeartbeatCheck && e.state() == IdleState.READER_IDLE) {
                // read idle
                int loss = heartbeatLossCounter.incrementAndGet();
                logger.warning("*** Read idle, loss peer heartbeat [" + loss + "] times");

                // 超过一定次数之后，主动关闭链路
                if (heartBeatCheckTolerance > 0 && loss >= heartBeatCheckTolerance) {
                    ChannelFuture closeFuture = ctx.channel().close().sync();
                    if (closeFuture.isSuccess()) {
                        logger.warning("*** Peer heartbeat loss more than [" + heartBeatCheckTolerance + "] times, close channel");
                    } else {
                        logger.warning("*** Peer heartbeat loss more than [" + heartBeatCheckTolerance + "] times, but close channel failed");
                    }
                }
            }
        }
    }

    /**
     * 如果收到了心跳包，则清除loss计数器
     * 如果开启了normalMsgAsHeartbeat，则收到任意报文都将清除loss计数器
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (normalMsgAsHeartbeat) {
            heartbeatLossCounter.set(0);
            ctx.fireChannelRead(msg);
        } else {
            ByteBuf rcv = (ByteBuf) msg;
            if (ByteBufUtil.equals(rcv, heartbeatCheckToken)) {
                heartbeatLossCounter.set(0);
            } else {
                ctx.fireChannelRead(msg);
            }
        }
    }
}
