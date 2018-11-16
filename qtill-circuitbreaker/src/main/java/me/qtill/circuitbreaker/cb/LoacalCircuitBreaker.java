package me.qtill.circuitbreaker.cb;

import me.qtill.circuitbreaker.state.CloseState;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class LoacalCircuitBreaker extends AbstractCircuitBreaker {

    public LoacalCircuitBreaker(String failRateForClose,
                                int idleTimeForOpen,
                                String passRateForHalfOpen, int failNumForHalfOpen) {

        this.thresholdFailRateForClose = failRateForClose;
        this.thresholdIdleTimeForOpen = idleTimeForOpen;
        this.thresholdPassRateForHalfOpen = passRateForHalfOpen;
        this.thresholdFailNumForHalfOpen = failNumForHalfOpen;
    }

    @Override
    public void reset() {
        this.setState(new CloseState());
    }

    @Override
    public boolean canPassCheck() {
        return getState().canPassCheck(this);
    }

    @Override
    public void countFailNum() {
        getState().counFailNum(this);
    }
}
