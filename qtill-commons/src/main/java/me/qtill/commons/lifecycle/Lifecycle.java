package me.qtill.commons.lifecycle;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public interface Lifecycle {

    public void init() throws LifecycleException;

    public void start() throws LifecycleException;

    public void suspend() throws LifecycleException;

    public void resume() throws LifecycleException;

    public void destoy() throws LifecycleException;

    public void addLifecycleListener(LifecycleListener lifecycleListener);

    public void removeLifecycleListener(LifecycleListener lifecycleListener);
}
