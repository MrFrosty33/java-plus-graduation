package ru.yandex.practicum.collector.service;

import org.apache.avro.specific.SpecificRecordBase;

public interface AvroKafkaProducer {
    void sendAvro(String topic, SpecificRecordBase avroMessage);
}
