package me.qtill.config.annotation;

import java.lang.annotation.*;

/**
 * 配置项key注解
 *
 * @author paranoidq
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface Key {

    /**
     * 用于注解配置项对应的函数，指定配置文件中该配置项的key
     * 该注解可选，如果没有标记，默认以函数名作为配置项获取的key
     *
     * @return
     */
    String value();
}
