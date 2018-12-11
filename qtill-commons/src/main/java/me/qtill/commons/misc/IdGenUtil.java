package me.qtill.commons.misc;

import com.google.common.annotations.Beta;
import me.qtill.commons.codec.CodecUtil;
import me.qtill.commons.number.RandomUtil;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * id生成工具
 *
 * note：该工具没有考虑大并发的情况，需要额外进行优化
 *
 * TODO：优化高并发场景
 * @author paranoidq
 * @since 1.0.0
 */
@Beta
public class IdGenUtil {

    private static SecureRandom random = new SecureRandom();

    /**
     * 封装JDK自带的UUID, 通过Random数字生成, 中间有-分割.
     */
    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * 封装JDK自带的UUID, 通过Random数字生成, 中间无-分割.
     */
    public static String uuid2() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 使用SecureRandom随机生成Long.
     */
    public static long randomLong() {
        return RandomUtil.nextLong();
    }

    /**
     * 基于URLSafeBase64编码的SecureRandom随机生成bytes.
     */
    public static String randomBase64(int length) {
        byte[] randomBytes = new byte[length];
        random.nextBytes(randomBytes);
        return CodecUtil.encodeBase64UrlSafeString(randomBytes);
    }

    /**
     * snowflake算法生成递增ID
     *
     * <code>
     *      SnowflakeUniqueId.nextId()
     * </code>
     * @param workderId
     * @param dataCenter
     * @return
     */
    public static SnowflakeUniqueId snowflakeId(long workderId, long dataCenter) {
        return new SnowflakeUniqueId(workderId, dataCenter);
    }
}
