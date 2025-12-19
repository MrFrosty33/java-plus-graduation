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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

            Long eventId = avro.getEventId();
            Long userId = avro.getUserId();
            Double newWeight = getActionWeight(avro.getActionType());

        } catch (Exception e) {
            log.warn("{}: exception in consumeUserActions(): ", className, e);
        }
    }

    private Optional<EventSimilarityAvro> calculateSimilarity(Long eventA, Long eventB, Double dotProduct, Instant timestamp) {
        try {
            log.trace("{}: calculateSimilarity(eventA={}, eventB={}, dotProduct={}, timestamp={})",
                    className, eventA, eventB, dotProduct, timestamp);

            double normA = calculateNorm(eventA);
            double normB = calculateNorm(eventB);

            if (normA == 0 || normB == 0) {
                log.trace("{}: similarity can't be calculated, one of normA: {} or normB: {} is zero",
                        className, normA, normB);
                return Optional.empty();
            }

            double similarity = dotProduct / (normA * normB);
            EventSimilarityAvro similarityAvro = EventSimilarityAvro.newBuilder()
                    .setEventA(eventA)
                    .setEventB(eventB)
                    .setScore(similarity)
                    .setTimestamp(timestamp)
                    .build();

            Optional<EventSimilarityAvro> result = Optional.of(similarityAvro);
            log.info("{}: result of calculateSimilarity(): {}", className, jsonMapper.writeValueAsString(result));

            return result;
        } catch (JsonProcessingException e) {
            logJsonException(e);
            throw new JsonException("Error processing avroMessage to JSON");
        }
    }

    private List<EventSimilarityAvro> recalculateSimilarities(Long eventId, Long userId,
                                                              Double newWeight, Double oldWeight,
                                                              Instant timestamp) {
        log.trace("{}: recalculateSimilarities(eventId={}, userId={}, newWeight={}, oldWeight={}, timestamp={})",
                className, eventId, userId, newWeight, oldWeight, timestamp);

        List<EventSimilarityAvro> updatedSimilarities = new ArrayList<>();

        // Map<EventId, dotProduct>, скалярное произведение к событию
        Map<Long, Double> eventDotProductMap = scalarResultMatrix.computeIfAbsent(eventId, e -> new HashMap<>());
        double dotProduct = eventDotProductMap.getOrDefault(eventId, 0.0);
        log.trace("{}: dotProduct: {}", className, dotProduct);

        // находим разницу
        double delta;
        if (oldWeight == null) {
            delta = newWeight;
        } else {
            delta = newWeight - oldWeight;
        }
        // обновляем eventDotProductMap
        double newDotProductValue = dotProduct + delta;
        eventDotProductMap.put(eventId, newDotProductValue);
        log.trace("{}: eventDotProductMap updated, eventId: {}, newDotProductValue: {}",
                className, eventId, newDotProductValue);

        // обновляем dot-products для каждого другого события
        for (Long otherEventId : eventUserWeight.keySet()) {
            // отсеиваем дубликаты текущего eventId
            if (!eventId.equals(otherEventId)) {
                Map<Long, Double> otherUserWeights = eventUserWeight.get(otherEventId);

                // отсеиваем ивенты, у которых ещё не было взаимодействий
                if (otherUserWeights != null) {
                    Double otherWeight = otherUserWeights.get(userId);

                    // отсеиваем ивенты, с которым наш пользователь не взаимодействовал
                    if (otherWeight != null) {
                        // чтобы ивенты всегда шли от меньшего к большему
                        long eventA = Math.min(eventId, otherEventId);
                        long eventB = Math.max(eventId, otherEventId);

                        Map<Long, Double> dotMap = scalarResultMatrix.computeIfAbsent(eventA, k -> new HashMap<>());
                        double currentDot = dotMap.getOrDefault(eventB, 0.0);

                        double oldMinWeight;
                        if (oldWeight == null) {
                            oldMinWeight = 0.0;
                        } else {
                            oldMinWeight = Math.min(oldWeight, otherWeight);
                        }
                        log.trace("{}: oldMinWeight: {}", currentDot, oldMinWeight);

                        double newMinWeight = Math.min(newWeight, otherWeight);
                        log.trace("{}: newMinWeight: {}", currentDot, newMinWeight);

                        double dotDelta = newMinWeight - oldMinWeight;
                        double updatedDot = currentDot + dotDelta;
                        log.trace("{}: updatedDot: {}", currentDot, updatedDot);

                        dotMap.put(eventB, updatedDot);

                        // рассчитываем similarity и добавляем в список, если она была рассчитана
                        Optional<EventSimilarityAvro> similarity = calculateSimilarity(eventA, eventB, updatedDot, timestamp);
                        similarity.ifPresent(updatedSimilarities::add);
                    }
                }
            }
        }

        log.info("{}: result of recalculateSimilarities(): {}", className, updatedSimilarities);
        return updatedSimilarities;
    }

    private double calculateNorm(Long eventId) {
        // Map<EventId, dotProduct>, скалярное произведение к событию
        Map<Long, Double> eventDotProductMap = scalarResultMatrix.get(eventId);
        if (eventDotProductMap == null) return 0.0;

        double result = Math.sqrt(eventDotProductMap.getOrDefault(eventId, 0.0));
        log.info("{}: result of calculateNorm(eventId = {}): {}", className, eventId, result);

        return result;
    }

    private double getActionWeight(ActionTypeAvro action) {
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
