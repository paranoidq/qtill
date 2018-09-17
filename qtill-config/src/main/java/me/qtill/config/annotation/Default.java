package me.qtill.config.annotation;

import java.lang.annotation.*;

/**
 * 注解配置项默认值
 * 如果配置文件没有提供相应的配置项，则取默认值
 * 如果既没有提供配置项，也没有在对应的函数上注解默认值，则在使用时会报错
 *
 * @author paranoidq
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface Default {

    /**
     * 用于注解配置项对应的函数，指定配置文件中该配置项的默认值
     * 该注解可选
     *
     * @return
     */
    String value();
}
