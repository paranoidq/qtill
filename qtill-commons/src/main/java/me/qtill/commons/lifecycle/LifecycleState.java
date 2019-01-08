package me.qtill.commons.lifecycle;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public enum LifecycleState {

    NEW,
    INITIALIZING, INITIALIZED,
    STARTING, RUNNING,
    SUSPENDING, SUSPENDED,
    RESUMING,
    DESTROYING, DESTROYED,
    FAILED;

}
