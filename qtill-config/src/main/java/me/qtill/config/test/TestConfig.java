package me.qtill.config.test;

import me.qtill.config.annotation.ConfigSource;
import me.qtill.config.annotation.Default;
import me.qtill.config.annotation.Key;

/**
 * @author paranoidq
 * @since 1.0.0
 */
@ConfigSource("classpath:folder/test-config.properties")
public interface TestConfig {

    @Key("name")
    @Default("default")
    public String name();

}
