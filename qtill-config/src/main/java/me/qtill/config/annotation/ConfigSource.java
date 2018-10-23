package me.qtill.config.annotation;

import me.qtill.config.ConfigSupport;

import java.lang.annotation.*;

/**
 * 配置源注解
 * <p>
 * 标注配置接口类，{@link ConfigSupport}扫描注解，并根据{@link ConfigSource#value()}制定的路径加载配置文件
 * 目前只支持单个配置文件
 * <p>
 * TODO: 多配置文件支持
 *
 * @author paranoidq
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface ConfigSource {

    /**
     * 配置源路径，基于classpath，可以省略classpath前缀
     * e.g. classpath:test/a.properties
     *
     * @return
     */
    String value();

}
