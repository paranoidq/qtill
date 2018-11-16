package me.qtill.commons.concurrent.sample.future_promise;

import com.google.common.base.Throwables;

import java.util.concurrent.Callable;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class Executor {


    public static <V> Future<V> submit(Callable<V> callable) {
        Future<V> future = new Future<>();
        new Thread() {
            @Override
            public void run() {
                try {
                    V result = callable.call();
                    future.setResult(result);
                } catch (Exception e) {
                    future.setUncaughtException(e);
                }
            }
        }.start();
        return future;
    }


    public static <V> void submit(Callable<V> callable, DefaultPromise<V> promise) {
        new Thread() {
            @Override
            public void run() {
                try {
                    V result = callable.call();
                    promise.setResult(result);
                    promise.onSuccess();
                } catch (Exception e) {
                    promise.onFailure(e);
                }
            }
        }.start();
    }

    public static void main(String[] args) throws InterruptedException {
        /*Future<String> future = Executor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(3));
                return "paranoidq";
            }
        });

        System.out.println(future.getResult());*/


        Executor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                throw new RuntimeException("aaa");
//                return "paranoidq";
            }
        }, new DefaultPromise<String>() {
            @Override
            public void onSuccess() {
                try {
                    System.out.println("result: " + getResult());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                System.out.println("failed: " + Throwables.getStackTraceAsString(throwable));
            }
        });
    }
}
