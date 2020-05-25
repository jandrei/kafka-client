package br;

import com.fasterxml.jackson.databind.ser.PropertyWriter;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.common.PartitionInfo;
import io.vertx.kafka.client.consumer.KafkaConsumer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ConsumidorKafka {

    KafkaConsumer<String, String> kafkaConsumer;
    Consumidor consumidor;
    Vertx vertx;

    public ConsumidorKafka(Consumidor consumidor) {
        this.consumidor = consumidor;
    }

    public void start(boolean atualizaComboTopicos) {
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", consumidor.brokers());
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("group.id", "kafkaclient123");
        config.put("auto.offset.reset", consumidor.desde());
        config.put("enable.auto.commit", "false");

        vertx = Vertx.vertx();
        kafkaConsumer = KafkaConsumer.create(vertx, config);
        kafkaConsumer.handler(row -> consumidor.handler(row));

        if (atualizaComboTopicos) {
            //ler informacoes dos topicos
            kafkaConsumer.listTopics(ar -> {
                if (ar.succeeded()) consumidor.registerPartitions(ar.result());
            });
        }
    }

    public void subscribe() {
        if (kafkaConsumer != null) {
            kafkaConsumer.unsubscribe();
            kafkaConsumer.subscribe(consumidor.topics());
        }
    }

    public void unsubcribe() {
        if (kafkaConsumer != null) {
            kafkaConsumer.unsubscribe();
        }
    }

}
