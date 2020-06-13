package com.raksit.example;

import org.apache.commons.io.FileUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class NotificationReceiver {

  private final String topic;

  public NotificationReceiver(String topic) {
    this.topic = topic;
  }

  public List<String> poll(int seconds) throws IOException {

    List<String> records = new ArrayList<>();

    KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(getProperties());

    kafkaConsumer.subscribe(Collections.singletonList(topic));

    try {
      long maxAllowedLatency = seconds * 1000;
      long endPollingTimestamp = System.currentTimeMillis() + maxAllowedLatency;

      while ( System.currentTimeMillis() < endPollingTimestamp ) {
        ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.of(1, ChronoUnit.SECONDS));
        for ( ConsumerRecord<String, String> next : consumerRecords ) {
          System.out.println(next.value());
          records.add(next.value());
        }
      }
    } finally {
      kafkaConsumer.close();
    }
    return records;
  }

  private Properties getProperties() throws IOException {
    FileReader reader = new FileReader(FileUtils.getFile("src", "test", "resources", "kafka.properties"));
    Properties properties = new Properties();
    properties.load(reader);
    properties.setProperty("bootstrap.servers", System.getProperty("kafkaBootstrapServers"));

    return properties;
  }
}
