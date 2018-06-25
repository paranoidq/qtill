package me.qtill.netty.util.connection;

import io.netty.channel.ChannelFuture;
import io.netty.util.internal.ThrowableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 连接命令，实现类需要实现具体的连接方法
 *
 * @author paranoidq
 * @since 1.0.0
 */
public interface ConnectCommand {

    void invoke();
}
