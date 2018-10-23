package me.qtill.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 配置类自动刷新
 * 标记了该注解的配置类，在启用后开启自动刷新
 *
 * 自动刷新与{@link me.qtill.config.ConfigSupport#get(Class)}类似，遵循懒加载的机制
 * 除非真正用到配置项或显式调用了{@link me.qtill.config.ConfigSupport#enableAutoRefresh(Class, long, TimeUnit)}
 * 才会真正开启后台自动刷新线程
 *
 * @author paranoidq
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableAutoRefresh {

    /**
     * 刷新时间，默认为5s
     *
     * @return
     */
    long duration() default 5;


    /**
     * 刷新时间单位，默认为秒
     *
     * @return
     */
    TimeUnit timeunit() default TimeUnit.SECONDS;


}
