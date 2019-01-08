package me.qtill.commons.lifecycle;

import com.google.common.util.concurrent.Service;

/**
 * 提供默认实现，子类无需实现所有方法，只需override需要的生命周期方法
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class LifecycleAdapter extends AbstractLifecycle {
    @Override
    protected void init0() throws LifecycleException {

    }

    @Override
    protected void start0() throws LifecycleException {

    }

    @Override
    protected void suspend0() throws LifecycleException {

    }

    @Override
    protected void resume0() throws LifecycleException {

    }

    @Override
    protected void destroy0() throws LifecycleException {

    }
}
