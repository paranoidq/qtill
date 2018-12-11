package me.qtill.hystrix.test;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class HelloworldCommand extends HystrixCommand<String> {

    public HelloworldCommand(HystrixCommandGroupKey group) {
        super(group);
    }

    @Override
    protected String run() throws Exception {
        throw new RuntimeException();
    }

    @Override
    protected String getFallback() {
        return "fallback";
    }
}
