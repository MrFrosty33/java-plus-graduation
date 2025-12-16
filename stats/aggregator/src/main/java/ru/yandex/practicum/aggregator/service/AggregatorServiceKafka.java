package ru.yandex.practicum.aggregator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.aggregator.config.KafkaEventsSimilarityProducerConfig;
import ru.yandex.practicum.aggregator.config.TopicConfig;
import ru.yandex.practicum.aggregator.exception.JsonException;

@Slf4j
@Service
public class AggregatorServiceKafka {
    private final String className = this.getClass().getSimpleName();

    private final JsonMapper jsonMapper;
    private final KafkaProducer<Void, SpecificRecordBase> eventsSimilarityProducer;
    private final TopicConfig topicConfig;

    public AggregatorServiceKafka(JsonMapper jsonMapper,
                                  KafkaEventsSimilarityProducerConfig kafkaEventsSimilarityProducerConfig, TopicConfig topicConfig) {
        this.jsonMapper = jsonMapper;
        this.topicConfig = topicConfig;

        log.trace("{}: constructor received KafkaProducerConfig: {}", className, kafkaEventsSimilarityProducerConfig);
        this.eventsSimilarityProducer = new KafkaProducer<>(kafkaEventsSimilarityProducerConfig.getProperties());
    }

    @KafkaListener(topics = "#{@topicConfig.userActions}", containerFactory = "userActionKafkaListenerContainerFactory")
    public void consumeUserActions(UserActionAvro avro) {
        try {
            log.trace("{}: consumeUserActions() polled UserActionAvro: {}", className, avro);
            //todo
            // если новое - рассчитывается сходство с остальными мероприятиями
            // если очередное - обновить S_min(A, B), S_a, S_b. После вычислить коэфф схожести для каждой пары
            // где в паре A - текущее мероприятие, В - каждое из других мероприятий
            // рассчитанные значения упаковать в EventSimilarityAvro и sendAvro();
        } catch (Exception e) {
            log.warn("{}: exception in consumeUserActions(): ", className, e);
        }
    }

    public void sendAvro(EventSimilarityAvro avro) {
        String topic = topicConfig.getEventsSimilarity();
        ProducerRecord<Void, SpecificRecordBase> record = new ProducerRecord<>(topic, avro);
        try {
            eventsSimilarityProducer.send(record);
            log.trace("{}: sent EventSimilarityAvro to topic {}: {}", className, topic, jsonMapper.writeValueAsString(avro));
        } catch (KafkaException e) {
            log.warn("{}: failed to send EventSimilarityAvro: {} with topic: {}", className, e.getMessage(), topic);
            throw e;
        } catch (JsonProcessingException e) {
            log.warn("{}: error processing avroMessage to JSON: {}", className, e.getMessage());
            throw new JsonException("Error processing avroMessage to JSON");
        }
    }
}
