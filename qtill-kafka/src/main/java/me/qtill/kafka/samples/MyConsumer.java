package me.qtill.kafka.samples;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;

import java.util.*;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class MyConsumer {

    private KafkaConsumer<String, String>          consumer;
    private Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private Collection<String>                     topics;

    public class RebalanceListener implements ConsumerRebalanceListener {

        @Override
        public void onPartitionsRevoked(Collection<TopicPartition> collection) {
            // 3. 通过listener保证提交offset
            consumer.commitSync(currentOffsets);
        }

        @Override
        public void onPartitionsAssigned(Collection<TopicPartition> collection) {

        }
    }


    public void init() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "localhost:9092");
        properties.put("group.id", "CountryCounter");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("enable.auto.commit", "false");  // 关闭自动提交

        consumer = new KafkaConsumer<String, String>(properties);

    }

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                System.out.println("exit");
                consumer.close();
                consumer.wakeup();
            }

        });


        try {
            consumer.subscribe(topics, new RebalanceListener());

            int maxUncommitted = 1000;
            int currentProcessed = 0;

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> record : records) {
                    String key = record.key();
                    String value = record.value();
                    // do something with record
                    // ...

                    currentOffsets.put(
                        new TopicPartition(record.topic(), record.partition()),
                        new OffsetAndMetadata(record.offset() + 1, "no metadata")
                    );
                    currentProcessed += 1;

                    // 1. 平时按处理的量，定期进行异步提交offset
                    if (currentProcessed >= maxUncommitted) {
                        consumer.commitAsync(currentOffsets, null);
                        currentProcessed = 0;
                    }
                }
            }
        } catch (WakeupException e) {
            // no need to handle
        } catch (Exception e) {

        } finally {
            // 2. 在关闭consumer或异常发生的情况下，同步提交offset
            try {
                consumer.commitSync(currentOffsets);
            } finally {
                consumer.close();
            }
        }


        // 手动分配分区，但是分区变动的情况下，consumer无法收到感知
        List<PartitionInfo> partitionInfos = consumer.partitionsFor("example");
        if (partitionInfos != null) {
            List<TopicPartition> topicPartitions = new ArrayList<>();
            for (PartitionInfo partitionInfo : partitionInfos) {
                int partitionId = partitionInfo.partition();
                TopicPartition topicPartition = new TopicPartition(partitionInfo.topic(), partitionId);
                topicPartitions.add(topicPartition);
            }
            consumer.assign(topicPartitions);

        }
    }


    public void shutdown() {
        if (consumer != null) {
            consumer.wakeup();
        }
    }
}
