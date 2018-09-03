package me.qtill.zookeeper.practices.loadbalance;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class ServerHandler extends ChannelDuplexHandler {

    // 将更新操作也抽象出来，作为一个provider，因为这里的balance更新操作设计zk，所以解耦更合理
    private final        BalanceUpdateProvider balanceUpdater;
    private static final Integer               BALANCE_STEP = 1;

    public ServerHandler(BalanceUpdateProvider balanceUpdater) {
        this.balanceUpdater = balanceUpdater;
    }

    public BalanceUpdateProvider getBalanceUpdater() {
        return balanceUpdater;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 有客户端建立连接时增加负载值
        System.out.println("client connected, add balance");
        balanceUpdater.addBalance(BALANCE_STEP);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("client disconnected, reduce balance");
        balanceUpdater.reduceBalance(BALANCE_STEP);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
