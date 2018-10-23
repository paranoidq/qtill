package me.qtill.config.test;

import me.qtill.config.ConfigSupport;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class TestMain {

    public static void main(String[] args) throws InterruptedException {
        AConfig aConfig = ConfigSupport.getInstance().get(AConfig.class);
        System.out.println(aConfig.a());
        System.out.println(aConfig.aa());

        Bconfig bconfig = ConfigSupport.getInstance().get(Bconfig.class);
        System.out.println(bconfig.b());
        System.out.println(bconfig.bb());

        // expected 异常
//        System.out.println(bconfig.bbb());


        Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {

            AConfig aConfig = ConfigSupport.getInstance().get(AConfig.class);
            Bconfig bconfig = ConfigSupport.getInstance().get(Bconfig.class);

            {
                ConfigSupport.getInstance().enableAutoRefresh(AConfig.class, 2, TimeUnit.SECONDS);
            }

            @Override
            public void run() {
                while (true) {
                    System.out.println(aConfig.a());
                    System.out.println(aConfig.aa());

                    System.out.println(bconfig.b());
                    System.out.println(bconfig.bb());

                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        TimeUnit.HOURS.sleep(1);
    }
}
