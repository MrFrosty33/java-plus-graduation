package ru.yandex.practicum.kafka.serializer;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

public class EventsSimilarityDeserializer extends BaseAvroDeserializer<EventSimilarityAvro> {
    public EventsSimilarityDeserializer() {
        super(EventSimilarityAvro.getClassSchema());
    }
}