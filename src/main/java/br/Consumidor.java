package br;

import io.vertx.core.Handler;
import io.vertx.kafka.client.common.PartitionInfo;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.consumer.KafkaConsumerRecord;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Consumidor {

    void handler(KafkaConsumerRecord<String, String> row);
    void registerPartitions(Map<String, List<PartitionInfo>> map);
    
    String brokers();
    Set<String> topics();
    String desde();
}
