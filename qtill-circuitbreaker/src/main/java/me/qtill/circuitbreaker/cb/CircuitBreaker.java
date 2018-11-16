package me.qtill.circuitbreaker.cb;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public interface CircuitBreaker {

    /**
     * 重置熔断器
     */
    void reset();

    /**
     * 是否允许通过熔断器
     * @return
     */
    boolean canPassCheck();

    /**
     * 统计失败次数
     */
    void countFailNum();
}
