package me.qtill.netty.handler;

import java.lang.annotation.*;

/**
 * @author paranoidq
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface ChannelHandlerAutoBind {

    /**
     * 指定ChannelHandler在Bootstrap装配时的名称
     *
     * @return
     */
    String handlerName() default "";


    /**
     * 指定ChanelHandler装配的顺序
     * 越小值代表ChannelHandler越靠前
     *
     * @return
     */
    int indexAtChannel();
}
