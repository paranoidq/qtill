package me.qtill.logging.log4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class Log4jTest {

    private static Logger logger = LoggerFactory.getLogger(Log4jTest.class);

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            logger.info("test log4j info configuration");
        }

        for (int i = 0; i < 10; i++) {
            logger.debug("test log4j debug configuration");
        }
    }
}
