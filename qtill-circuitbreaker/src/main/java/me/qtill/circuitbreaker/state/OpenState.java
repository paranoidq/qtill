package me.qtill.circuitbreaker.state;

import me.qtill.circuitbreaker.cb.AbstractCircuitBreaker;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class OpenState implements CBState {

    /**
     * 进入当前状态的初始化时间
     */
    private long stateTime = System.currentTimeMillis();


    @Override
    public String getStateName() {
        // 获取当前状态名称
        return this.getClass().getSimpleName();
    }

    @Override
    public void checkAndSwitchState(AbstractCircuitBreaker cb) {
        // 打开状态，检查等待时间是否已经到了，如果到了就切换到半开状态
        long now = System.currentTimeMillis();
        long idleTime = cb.thresholdIdleTimeForOpen * 1000L;
        if (stateTime + idleTime <= now) {
            cb.setState(new HalfOpenState());
        }
    }

    @Override
    public boolean canPassCheck(AbstractCircuitBreaker cb) {
        // 检查状态，及时触发状态转换
        checkAndSwitchState(cb);
        return false;
    }

    @Override
    public void counFailNum(AbstractCircuitBreaker cb) {

    }
}
