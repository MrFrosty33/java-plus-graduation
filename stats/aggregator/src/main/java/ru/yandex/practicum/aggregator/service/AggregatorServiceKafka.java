package ru.yandex.practicum.aggregator.service;

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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class AggregatorServiceKafka {
    private final String className = this.getClass().getSimpleName();
    private final KafkaProducer<Void, SpecificRecordBase> eventsSimilarityProducer;
    private final TopicConfig topicConfig;
    private final ActionWeightConfig weightConfig;

    //            Map<EventId, Map<UserId, Weight>>, максимальный вес взаимодействия пользователя с мероприятием
    private final Map<Long, Map<Long, Double>> eventUserWeight = new HashMap<>();
    //            Map<EventIdA, Map<EventIdB, dotProduct>>, матрица скалярных произведений между парой событий
    private final Map<Long, Map<Long, Double>> scalarResultMatrix = new HashMap<>();


    public AggregatorServiceKafka(KafkaEventsSimilarityProducerConfig kafkaEventsSimilarityProducerConfig,
                                  TopicConfig topicConfig, ActionWeightConfig weightConfig) {
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
            log.trace("{}: consumeEventSimilarity() polled EventSimilarityAvro: (eventA={}, eventB={}, score={}, timestamp={})",
                    className, avro.getEventA(), avro.getEventB(), avro.getScore(), avro.getTimestamp());
        } catch (KafkaException e) {
            log.warn("{}: failed to send EventSimilarityAvro: {} with topic: {}", className, e.getMessage(), topic);
            throw e;
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

            // Обновляем вес пользователя для события и получаем список similarity для отправки
            List<EventSimilarityAvro> similaritiesToSend = updateEventWeight(eventId, userId, newWeight, avro.getTimestamp());

            // Сортируем и отправляем разом
            similaritiesToSend.stream()
                    .sorted(Comparator.comparingLong(EventSimilarityAvro::getEventA)
                            .thenComparingLong(EventSimilarityAvro::getEventB))
                    .forEach(this::sendAvro);

        } catch (Exception e) {
            log.warn("{}: exception in consumeUserActions(): ", className, e);
        }
    }

    private List<EventSimilarityAvro> updateEventWeight(Long eventId, Long userId, Double newWeight, Instant timestamp) {
        log.trace("{}: calculateSimilarity(eventId={}, userId={}, newWeight={}, timestamp={})",
                className, eventId, userId, newWeight, timestamp);

        // находим или же создаём мапу весов
        Map<Long, Double> userWeights = eventUserWeight.computeIfAbsent(eventId, key -> new HashMap<>());
        Double oldWeight = userWeights.get(userId);

        // обновляем и пересчитываем схожесть только в том случае, если новый вес больше старого
        if (oldWeight == null || newWeight > oldWeight) {
            userWeights.put(userId, newWeight);
            List<EventSimilarityAvro> result = recalculateSimilarities(eventId, userId, newWeight, oldWeight, timestamp);
            log.trace("{}: result of updateEventWeight(): {}", className, result);
            return result;
        }

        log.trace("{}: result of updateEventWeight(): no need to update event weight", className);
        return Collections.emptyList();
    }

    private Optional<EventSimilarityAvro> calculateSimilarity(Long eventA, Long eventB, Double dotProduct, Instant timestamp) {
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
        log.info("{}: result of calculateSimilarity(): {}", className, result);

        return result;
    }

    private List<EventSimilarityAvro> recalculateSimilarities(Long eventId, Long userId,
                                                              Double newWeight, Double oldWeight,
                                                              Instant timestamp) {
        log.trace("{}: recalculateSimilarities(eventId={}, userId={}, newWeight={}, oldWeight={}, timestamp={})",
                className, eventId, userId, newWeight, oldWeight, timestamp);

        List<EventSimilarityAvro> updatedSimilarities = new ArrayList<>();

        // Map<EventId, dotProduct>, скалярное произведение к событию
        Map<Long, Double> eventDotProductMap = scalarResultMatrix.computeIfAbsent(eventId, key -> new HashMap<>());
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
}
