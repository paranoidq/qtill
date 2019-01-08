package me.qtill.commons.lifecycle;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static me.qtill.commons.lifecycle.LifecycleState.*;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public abstract class AbstractLifecycle implements Lifecycle {

    // 监听器列表，COW list实现
    private List<LifecycleListener> listeners = new CopyOnWriteArrayList<>();

    // 当前生命周期状态
    private LifecycleState state = NEW;

    @Override
    public synchronized void init() throws LifecycleException {
        if (state != NEW) {
            return;
        }
        setStateAndFireEvent(INITIALIZING);
        try {
            init0();
        } catch (Throwable t) {
            handleException(INITIALIZING, t);
        }
        setStateAndFireEvent(INITIALIZED);
    }

    @Override
    public void start() throws LifecycleException {
        if (state == NEW) {
            init();
        }

        if (state != INITIALIZED) {
            return;
        }

        setStateAndFireEvent(STARTING);
        try {
            start0();
        } catch (Throwable t) {
            handleException(STARTING, t);
        }
        setStateAndFireEvent(LifecycleState.RUNNING);
    }

    @Override
    public void suspend() throws LifecycleException {
        if (state == SUSPENDED || state == SUSPENDING) {
            return;
        }

        if (state != RUNNING) {
            return;
        }
        setStateAndFireEvent(SUSPENDING);
        try {
            suspend0();
        } catch (Throwable t) {
            handleException(SUSPENDING, t);
        }
        setStateAndFireEvent(LifecycleState.SUSPENDED);

    }

    @Override
    public void resume() throws LifecycleException {
        if (state == RESUMING) {
            return;
        }

        if (state != SUSPENDED) {
            return;
        }
        setStateAndFireEvent(RESUMING);
        try {
            suspend0();
        } catch (Throwable t) {
            handleException(RESUMING, t);
        }
        setStateAndFireEvent(LifecycleState.RUNNING);
    }

    @Override
    public void destoy() throws LifecycleException {
        if (state == DESTROYING || state == DESTROYED) {
            return;
        }

        setStateAndFireEvent(DESTROYING);
        try {
            destroy0();
        } catch (Throwable t) {
            handleException(DESTROYING, t);
        }
        setStateAndFireEvent(LifecycleState.DESTROYED);
    }


    //////////// protected模板方法，供子类实现 ////////////

    protected abstract void init0() throws LifecycleException;

    protected abstract void start0() throws LifecycleException;

    protected abstract void suspend0() throws LifecycleException;

    protected abstract void resume0() throws LifecycleException;

    protected abstract void destroy0() throws LifecycleException;


    @Override
    public void addLifecycleListener(LifecycleListener lifecycleListener) {
        listeners.add(lifecycleListener);
    }

    @Override
    public void removeLifecycleListener(LifecycleListener lifecycleListener) {
        listeners.remove(lifecycleListener);
    }


    private void fireLifecycleEvent(LIfecycleEvent event) {
        for (LifecycleListener listener : listeners) {
            listener.lifecycleEvent(event);
        }
    }

    private String formatString(String pattern, Object... arguments) {
        return MessageFormat.format(pattern, arguments);
    }

    private void setStateAndFireEvent(LifecycleState targetState) {
        this.state = targetState;
        fireLifecycleEvent(new LIfecycleEvent(targetState));
    }

    private void handleException(LifecycleState targetState, Throwable t) throws LifecycleException {
        setStateAndFireEvent(FAILED);
        if (t instanceof LifecycleException) {
            throw new LifecycleException(formatString(
                "Failed to {0} {1}, Error Msg: {2}", targetState.name(), toString(), t.getMessage()), t);
        }
    }
}
