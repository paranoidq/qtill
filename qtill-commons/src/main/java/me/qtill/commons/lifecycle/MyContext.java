package me.qtill.commons.lifecycle;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class MyContext extends LifecycleAdapter {

    @Override
    protected void start0() throws LifecycleException {
        // 实现必要的方法
    }


    @Override
    protected void suspend0() throws LifecycleException {
        // 实现必要的方法
    }


    public static void main(String[] args) throws LifecycleException {
        MyContext context = new MyContext();
        context.resume0();
    }
}
