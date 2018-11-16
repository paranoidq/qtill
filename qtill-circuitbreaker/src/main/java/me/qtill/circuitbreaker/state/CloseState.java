package me.qtill.circuitbreaker.state;

import me.qtill.circuitbreaker.cb.AbstractCircuitBreaker;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class CloseState implements CBState {

    /**
     * 进入当前状态的初始化时间
     */
    private long stateTime = System.currentTimeMillis();

    /**
     * 关闭状态，失败计数器，以及失败计数器初始化时间
     */
    private AtomicInteger failNum          = new AtomicInteger(0);
    private long          failNumClearTime = System.currentTimeMillis();


    @Override
    public String getStateName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void checkAndSwitchState(AbstractCircuitBreaker cb) {
        // 阈值判断，如果达到失败的阈值，切换状态到打开状态
        long maxFailNum = Long.valueOf(cb.thresholdFailRateForClose.split("/")[0]);
        if (failNum.get() >= maxFailNum) {
            cb.setState(new OpenState());
        }
    }

    @Override
    public boolean canPassCheck(AbstractCircuitBreaker cb) {
        // 关闭状态，允许请求通过
        return true;
    }

    @Override
    public void counFailNum(AbstractCircuitBreaker cb) {
        // 检查计数器是否过期，否则重新计数
        long period = Long.valueOf(cb.thresholdFailRateForClose.split("/")[1]) * 1000;
        long now = System.currentTimeMillis();
        if (failNumClearTime + period <= now) {
            failNum.set(0);
        }
        // 失败计数
        failNum.incrementAndGet();

        // 检查是否切换状态（为什么这里需要调用检查？？？）
        checkAndSwitchState(cb);
    }
}
