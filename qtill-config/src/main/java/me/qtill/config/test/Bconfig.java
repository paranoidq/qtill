package me.qtill.config.test;

import me.qtill.config.annotation.ConfigSource;
import me.qtill.config.annotation.Default;

/**
 * @author paranoidq
 * @since 1.0.0
 */
@ConfigSource("classpath:folder/b.properties")
public interface Bconfig {

    public String b();


    @Default("bb-value")
    public String bb();


    public String bbb();

}
