package me.qtill.config.test;

import me.qtill.config.annotation.ConfigSource;
import me.qtill.config.annotation.Default;
import me.qtill.config.annotation.Key;

/**
 * @author paranoidq
 * @since 1.0.0
 */
@ConfigSource("classpath:folder/a.properties")
public interface AConfig {

    @Key("a")
    public String a();


    @Default("aa-value")
    public String aa();

}
