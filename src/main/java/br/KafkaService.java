package br;

import io.vertx.core.Vertx;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KafkaService {

    KafkaConsumer<String, String> kafkaConsumer;
    KafkaConfiguration kafkaConfiguration;
    KafkaProducer kafkaProducer;
    String actualBrokers = "";
    Vertx vertx;

    public KafkaService(KafkaConfiguration kafkaConfiguration) {
        this.kafkaConfiguration = kafkaConfiguration;
        this.vertx = Vertx.vertx();
    }


    public void createConsumer(boolean atualizaComboTopicos) {
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", kafkaConfiguration.getBrokers());
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("group.id", "GRUPOLOCAL-"+UUID.randomUUID().toString());
        config.put("auto.offset.reset", kafkaConfiguration.fetchSince());
        config.put("enable.auto.commit", "false");

        vertx = Vertx.vertx();
        kafkaConsumer = KafkaConsumer.create(vertx, config);
        kafkaConsumer.handler(row -> kafkaConfiguration.handleRow(row));

        if (atualizaComboTopicos) {
            //ler informacoes dos topicos
            kafkaConsumer.listTopics(ar -> {
                if (ar.succeeded()) kafkaConfiguration.callbackFechTopics(ar.result());
            });
        }
    }
    
    

    public void subscribe() {
        if (kafkaConsumer != null) {
            kafkaConsumer.unsubscribe();
            kafkaConsumer.subscribe(kafkaConfiguration.getTopics());
        }
    }

    public void unsubcribe() {
        if (kafkaConsumer != null) {
            kafkaConsumer.unsubscribe();
        }
    }


    public void createProducer() {
        if (actualBrokers.isEmpty() || !actualBrokers.equals(kafkaConfiguration.getBrokers())) {
            actualBrokers = kafkaConfiguration.getBrokers();

            Map<String, String> config = new HashMap<>();
            config.put("bootstrap.servers", actualBrokers);
            config.put("key.serializer", "io.vertx.kafka.client.serialization.BufferSerializer");
            config.put("value.serializer", "io.vertx.kafka.client.serialization.BufferSerializer");
            config.put("acks", "1");

            Vertx vertx = Vertx.vertx();
            kafkaProducer = KafkaProducer.create(vertx, config, String.class, String.class);
        }
    }

    public void send(String topic, String message) {
        if (kafkaProducer != null) {
            KafkaProducerRecord record = KafkaProducerRecord.create(topic, message);
            kafkaProducer.send(record, result -> {
                System.out.println(result);
            });
        }
    }

}
