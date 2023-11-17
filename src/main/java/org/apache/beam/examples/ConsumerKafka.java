package org.apache.beam.examples;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ConsumerKafka {

    public static void main(String[] args) {
        Properties kafkaProps = new Properties();
        kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer_group_id");
        kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        Consumer<String, String> kafkaConsumer = new KafkaConsumer<>(kafkaProps);
        kafkaConsumer.subscribe(Collections.singletonList("output-stream"));

        try {
            while (true) {
                // Poll for records
                ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(100));

                records.forEach(record -> {
                    String kafkaValue = record.value();
                    System.out.println("Received from Kafka: " + kafkaValue);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            kafkaConsumer.close();
        }
    }
}
