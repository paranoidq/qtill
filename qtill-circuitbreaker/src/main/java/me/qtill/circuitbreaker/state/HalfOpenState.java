package me.qtill.circuitbreaker.state;

import me.qtill.circuitbreaker.cb.AbstractCircuitBreaker;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class HalfOpenState implements CBState {
    /**
     * 进入当前状态的初始化时间
     */
    private long stateTime = System.currentTimeMillis();

    /**
     * 半开状态，失败计数器
     */
    private AtomicInteger failNum = new AtomicInteger(0);

    /**
     * 半开状态，允许通过的计数器
     */
    private AtomicInteger passNum = new AtomicInteger(0);

    @Override
    public String getStateName() {
        // 获取当前状态名称
        return this.getClass().getSimpleName();
    }

    @Override
    public void checkAndSwitchState(AbstractCircuitBreaker cb) {
        long idleTime = Long.valueOf(cb.thresholdPassRateForHalfOpen.split("/")[1]) * 1000L;
        long now = System.currentTimeMillis();
        if (now >= stateTime + idleTime) {
            int maxFailNum = cb.thresholdFailNumForHalfOpen;
            // 根据半开期间的运行统计，判断进入close或open状态
            if (failNum.get() >= maxFailNum) {
                cb.setState(new OpenState());
            } else {
                cb.setState(new CloseState());
            }
        }
    }

    @Override
    public boolean canPassCheck(AbstractCircuitBreaker cb) {
        checkAndSwitchState(cb);

        // 超过了阀值，不再放量
        int maxPassNum = Integer.valueOf(cb.thresholdPassRateForHalfOpen.split("/")[0]);
        if (passNum.get() > maxPassNum) {
            return false;
        }
        // 检查是否超过阈值
        if (passNum.incrementAndGet() <= maxPassNum) {
            return true;
        }

        return false;
    }

    @Override
    public void counFailNum(AbstractCircuitBreaker cb) {
        failNum.incrementAndGet();

        checkAndSwitchState(cb);
    }
}
