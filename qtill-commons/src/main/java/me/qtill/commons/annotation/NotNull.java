package me.qtill.commons.annotation;

import java.lang.annotation.*;

/**
 * 标注参数不为null
 * @author paranoidq
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.PARAMETER})
@Documented
@Retention(RetentionPolicy.SOURCE)
public @interface NotNull {
}
