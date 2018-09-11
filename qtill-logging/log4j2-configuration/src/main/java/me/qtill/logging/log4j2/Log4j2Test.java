package me.qtill.logging.log4j2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class Log4j2Test {
    
    private static Logger logger = LoggerFactory.getLogger(Log4j2Test.class);

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            logger.info("test log4j2 info configuration");
        }

        for (int i = 0; i < 10; i++) {
            logger.debug("test log4j2 debug configuration");
        }
    }
}
