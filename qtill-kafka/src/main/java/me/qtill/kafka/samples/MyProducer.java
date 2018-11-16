package me.qtill.kafka.samples;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class MyProducer {

    private KafkaProducer<String, String> producer;


    public void init() {

        Properties properties = new Properties();
        properties.put("bootstrap.servers", "localhost:9092");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<String, String>(properties);
    }


    void send(String topic, String key, String value) {
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(
            topic, key, value
        );
        try {
            producer.send(record);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
