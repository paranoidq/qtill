package me.qtill.circuitbreaker.state;

import me.qtill.circuitbreaker.cb.AbstractCircuitBreaker;

/**
 * CBState将具体的工作代理给{@link me.qtill.circuitbreaker.cb.CircuitBreaker}实例
 *
 * @author paranoidq
 * @since 1.0.0
 */
public interface CBState {

    /**
     * 获取当前状态名称
     *
     * @return
     */
    String getStateName();

    /**
     * 检查以及校验当前状态是否需要扭转
     */
    void checkAndSwitchState(AbstractCircuitBreaker cb);

    /**
     * 是否允许通过熔断器
     *
     * @return
     */
    boolean canPassCheck(AbstractCircuitBreaker cb);

    /**
     * 统计失败次数
     *
     * @param cb
     */
    void counFailNum(AbstractCircuitBreaker cb);
}
