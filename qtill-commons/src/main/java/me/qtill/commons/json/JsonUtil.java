package me.qtill.commons.json;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 使用{@link JsonMapper}完成JSON String<->Java Object的转换
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class JsonUtil {

    /**
     * 创建只输出非Null的属性到Json字符串的Mapper.
     */
    public static JsonMapper nonNullMapper() {
        return new JsonMapper(JsonInclude.Include.NON_NULL);
    }

    /**
     * 创建只输出非Null且非Empty(如List.isEmpty)的属性到Json字符串的Mapper.
     *
     * 注意，要小心使用, 特别留意empty的情况.
     */
    public static JsonMapper nonEmptyMapper() {
        return new JsonMapper(JsonInclude.Include.NON_EMPTY);
    }

    /**
     * 默认的全部输出的Mapper, 区别于INSTANCE，可以做进一步的配置
     */
    public static JsonMapper defaultMapper() {
        return new JsonMapper();
    }
}
