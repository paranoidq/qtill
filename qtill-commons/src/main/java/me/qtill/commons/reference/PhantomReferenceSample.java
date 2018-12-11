package me.qtill.commons.reference;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class PhantomReferenceSample {

    public static void test1() throws InterruptedException {

        ReferenceQueue<String> referenceQueue = new ReferenceQueue<>();
        new Thread(
            () -> {
                while (true) {
                    Reference<? extends String> clearRef = referenceQueue.poll();
                    if (clearRef != null) {
                        String s = clearRef.get();
                        System.out.println("引用对象被回收：ref=" + clearRef + ", value=" + s);
                    }
                }
            }
        ).start();

        /**
         * ！这里string不能用字面量，否则会被放入到metaspace中，无法被GC回收。而必须采用new的方式，新建一个分配在堆中的string才能看出效果
         */
        WeakReference<String> s1 = new WeakReference<>(new String("aa"), referenceQueue);
        WeakReference<String> s2 = new WeakReference<>(new String("aa"), referenceQueue);
        WeakReference<String> s3 = new WeakReference<>(new String("aa"), referenceQueue);

        System.out.println(s1.get());
        System.out.println(s2.get());
        System.out.println(s3.get());

        System.out.println("通知JVM开始GC");
        System.gc();



        TimeUnit.HOURS.sleep(2);
    }


    public static void main(String[] args) throws Exception {
        test1();
    }
}
