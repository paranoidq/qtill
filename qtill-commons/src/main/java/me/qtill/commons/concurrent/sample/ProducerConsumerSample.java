package me.qtill.commons.concurrent.sample;

import java.util.List;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class ProducerConsumerSample {

        public static class Producer extends Thread {
            private List<String> storage;

            public Producer(List<String> storage) {
                this.storage = storage;
            }

            @Override
            public void run() {
                while (true) {
                    synchronized (storage) {
                        
                    }
                }
            }
        }

}
