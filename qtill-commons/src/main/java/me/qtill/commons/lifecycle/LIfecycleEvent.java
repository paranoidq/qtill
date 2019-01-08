package me.qtill.commons.lifecycle;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class LIfecycleEvent {

    private LifecycleState state;

    public LIfecycleEvent(LifecycleState state) {
        this.state = state;
    }

    public LifecycleState getState() {
        return state;
    }
}
