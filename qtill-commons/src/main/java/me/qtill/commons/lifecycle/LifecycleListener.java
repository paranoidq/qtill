package me.qtill.commons.lifecycle;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public interface LifecycleListener {

    /**
     * 触发生命周期时间处理
     *
     * @param event 生命周期事件
     */
    public void lifecycleEvent(LIfecycleEvent event);

}
