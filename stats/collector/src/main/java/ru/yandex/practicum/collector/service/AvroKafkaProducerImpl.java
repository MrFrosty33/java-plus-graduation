package ru.yandex.practicum.collector.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.collector.config.KafkaProducerConfig;
import ru.yandex.practicum.collector.exception.JsonException;

import java.time.Duration;

@Slf4j
@Service
public class AvroKafkaProducerImpl implements AvroKafkaProducer, AutoCloseable {
    private final String className = this.getClass().getSimpleName();

    private final JsonMapper jsonMapper;
    private final KafkaProducer<Void, SpecificRecordBase> producer;

    public AvroKafkaProducerImpl(JsonMapper jsonMapper,
                                 KafkaProducerConfig kafkaProducerConfig) {
        this.jsonMapper = jsonMapper;

        log.trace("{}: constructor received KafkaProducerConfig: {}", className, kafkaProducerConfig);
        this.producer = new KafkaProducer<>(kafkaProducerConfig.getProperties());
    }


    @Override
    public void sendAvro(String topic, SpecificRecordBase avroMessage) {
        ProducerRecord<Void, SpecificRecordBase> record = new ProducerRecord<>(topic, avroMessage);
        try {
            producer.send(record);
            log.trace("{}: sent Avro message to topic {}: {}", className, topic, jsonMapper.writeValueAsString(avroMessage));
        } catch (KafkaException e) {
            log.warn("{}: failed to send Avro message: {} with topic: {}", className, e.getMessage(), topic);
            throw e;
        } catch (JsonProcessingException e) {
            log.warn("{}: error processing avroMessage to JSON: {}", className, e.getMessage());
            throw new JsonException("Error processing avroMessage to JSON");
        }
    }

    @Override
    public void close() throws Exception {
        producer.flush();
        producer.close(Duration.ofSeconds(10));
    }
}
