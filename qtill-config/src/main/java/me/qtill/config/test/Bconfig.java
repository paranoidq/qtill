package me.qtill.config.test;

import me.qtill.config.annotation.ConfigSource;
import me.qtill.config.annotation.Default;
import me.qtill.config.annotation.EnableAutoRefresh;

import java.util.concurrent.TimeUnit;

/**
 * @author paranoidq
 * @since 1.0.0
 */
@ConfigSource("classpath:folder/b.properties")
@EnableAutoRefresh(duration = 5, timeunit = TimeUnit.SECONDS)
public interface Bconfig {

    public String b();


    @Default("bb-value")
    public String bb();


    public String bbb();

}
