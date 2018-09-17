package me.qtill.config;

/**
 * 类型转换器
 * 用于加载配置文件时，将配置文件中的项与配置接口函数对应，转换为接口函数的返回类型
 *
 * 目前只支持基本类型
 *
 * TODO:
 * - 支持列表、数组等高级类型
 *
 * @author paranoidq
 * @since 1.0.0
 */
public enum Converter {

    // 原始类型
    PRIMITIVE {
        @Override
        Object tryConvert(String text, Class<?> targetType) {
            if (!targetType.isPrimitive()) return SKIP;
            if (targetType == Byte.TYPE) return Byte.parseByte(text);
            if (targetType == Short.TYPE) return Short.parseShort(text);
            if (targetType == Integer.TYPE) return Integer.parseInt(text);
            if (targetType == Long.TYPE) return Long.parseLong(text);
            if (targetType == Boolean.TYPE) return Boolean.parseBoolean(text);
            if (targetType == Float.TYPE) return Float.parseFloat(text);
            if (targetType == Double.TYPE) return Double.parseDouble(text);
            return SKIP;
        }
    },;

    abstract Object tryConvert(String text, Class<?> targetType);

    /**
     * The NULL object: when tryConvert returns this object, the conversion result is null.
     */
    static final Object NULL = new Object();

    /**
     * The SKIP object: when tryConvert returns this object the conversion is skipped in favour of the next one.
     */
    static final Object SKIP = new Object();

}

