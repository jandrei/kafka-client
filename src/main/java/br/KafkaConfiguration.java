package br;

import io.vertx.kafka.client.common.PartitionInfo;
import io.vertx.kafka.client.consumer.KafkaConsumerRecord;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface KafkaConfiguration {
    void handleRow(KafkaConsumerRecord<String, String> row);
    void callbackFechTopics(Map<String, List<PartitionInfo>> map);
    String getBrokers();
    Set<String> getTopics();
    String fetchSince();
}
