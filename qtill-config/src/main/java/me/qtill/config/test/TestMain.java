package me.qtill.config.test;

import me.qtill.config.ConfigSupport;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class TestMain {

    public static void main(String[] args) {
        TestConfig testConfig = ConfigSupport.getInstance().get(TestConfig.class);
        System.out.println(testConfig.name());
    }
}
