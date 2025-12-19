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
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.aggregator.config.ActionWeightConfig;
import ru.yandex.practicum.aggregator.config.KafkaEventsSimilarityProducerConfig;
import ru.yandex.practicum.aggregator.config.TopicConfig;
import ru.yandex.practicum.aggregator.exception.JsonException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class AggregatorServiceKafka {
    private final String className = this.getClass().getSimpleName();

    private final JsonMapper jsonMapper;
    private final KafkaProducer<Void, SpecificRecordBase> eventsSimilarityProducer;
    private final TopicConfig topicConfig;
    private final ActionWeightConfig weightConfig;

    //            Map<EventId, Map<UserId, Weight>>, максимальный вес взаимодействия пользователя с мероприятием
    private final Map<Long, Map<Long, Double>> eventUserWeight = new HashMap<>();
    //            Map<EventIdA, Map<EventIdB, dotProduct>>, матрица скалярных произведений между парой событий
    private final Map<Long, Map<Long, Double>> scalarResultMatrix = new HashMap<>();


    public AggregatorServiceKafka(JsonMapper jsonMapper,
                                  KafkaEventsSimilarityProducerConfig kafkaEventsSimilarityProducerConfig,
                                  TopicConfig topicConfig, ActionWeightConfig weightConfig) {
        this.jsonMapper = jsonMapper;
        this.topicConfig = topicConfig;
        this.weightConfig = weightConfig;

        log.trace("{}: constructor received KafkaProducerConfig: {}", className, kafkaEventsSimilarityProducerConfig);
        this.eventsSimilarityProducer = new KafkaProducer<>(kafkaEventsSimilarityProducerConfig.getProperties());
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
            logJsonException(e);
            throw new JsonException("Error processing avroMessage to JSON");
        }
    }

    @KafkaListener(
            topics = "#{@topicConfig.userActions}",
            containerFactory = "userActionKafkaListenerContainerFactory"
    )
    public void consumeUserActions(UserActionAvro avro) {
        try {
            log.trace("{}: consumeUserActions() polled UserActionAvro: {}", className, avro);
            log.trace("{}: consumeUserActions() polled UserActionAvro: {}", className, avro);

            Long eventId = avro.getEventId();
            Long userId = avro.getUserId();
            Double newWeight = getActionWeight(avro.getActionType());

        } catch (Exception e) {
            log.warn("{}: exception in consumeUserActions(): ", className, e);
        }
    }

    private Optional<EventSimilarityAvro> calculateSimilarity(Long eventA, Long eventB, Double dotProduct, Instant timestamp) {
        try {
            Double normA = calculateNorm(eventA);
            Double normB = calculateNorm(eventB);

            if (normA == 0 || normB == 0) {
                log.trace("{}: similarity can't be calculated, one of normA: {} or normB: {} is zero",
                        className, normA, normB);
                return Optional.empty();
            }

            Double similarity = dotProduct / (normA * normB);
            EventSimilarityAvro similarityAvro = EventSimilarityAvro.newBuilder()
                    .setEventA(eventA)
                    .setEventB(eventB)
                    .setScore(similarity)
                    .setTimestamp(timestamp)
                    .build();

            Optional<EventSimilarityAvro> result = Optional.of(similarityAvro);
            log.trace("{}: result of calculateSimilarity(eventA={}, eventB={}, dotProduct={}, timestamp={}): {}",
                    className, eventA, eventB, dotProduct, timestamp, jsonMapper.writeValueAsString(result));

            return result;
        } catch (JsonProcessingException e) {
            logJsonException(e);
            throw new JsonException("Error processing avroMessage to JSON");
        }
    }

    private Double calculateNorm(Long eventId) {
        // Map<EventId, dotProduct>, скалярное произведение к событию
        Map<Long, Double> eventDotProductMap = scalarResultMatrix.get(eventId);
        if (eventDotProductMap == null) return 0.0;

        Double result = Math.sqrt(eventDotProductMap.getOrDefault(eventId, 0.0));
        log.trace("{}: result of calculateNorm(eventId = {}): {}", className, eventId, result);

        return result;
    }

    private Double getActionWeight(ActionTypeAvro action) {
        return switch (action) {
            case VIEW -> weightConfig.getView();
            case REGISTER -> weightConfig.getRegister();
            case LIKE -> weightConfig.getLike();
        };
    }

    private void logJsonException(Exception e) {
        // чтобы не ругалось на одинаковые сообщения в логах вынес сюда
        log.warn("{}: error processing avroMessage to JSON: {}", className, e.getMessage());
    }

    //todo в тестах analyzer возможно request-service или где ещё не отправляются сообщения
}
