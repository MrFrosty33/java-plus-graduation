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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AggregatorServiceKafka {
    private final String className = this.getClass().getSimpleName();

    private final JsonMapper jsonMapper;
    private final KafkaProducer<Void, SpecificRecordBase> eventsSimilarityProducer;
    private final TopicConfig topicConfig;

    //            Map<EventId, Map<UserId, Weight>>, максимальный вес взаимодействия пользователя с мероприятием
    private final Map<Long, Map<Long, Double>> maxWeight = new HashMap<>();
    //            Map<EventIdA, Map<EventIdB, S_minWeight>>, сумма минимальных весов для пары мероприятий
    private final Map<Long, Map<Long, Double>> minWeightSum = new HashMap<>();
    //            Map<EventId, Weight>, сумма весов мероприятия
    private final Map<Long, Double> eventWeightSum = new HashMap<>();
    //            Map<EventPair, Similarity>, схожесть пары мероприятий
    private final Map<EventPair, Double> eventsSimilarity = new HashMap<>();


    public AggregatorServiceKafka(JsonMapper jsonMapper,
                                  KafkaEventsSimilarityProducerConfig kafkaEventsSimilarityProducerConfig, TopicConfig topicConfig) {
        this.jsonMapper = jsonMapper;
        this.topicConfig = topicConfig;

        log.trace("{}: constructor received KafkaProducerConfig: {}", className, kafkaEventsSimilarityProducerConfig);
        this.eventsSimilarityProducer = new KafkaProducer<>(kafkaEventsSimilarityProducerConfig.getProperties());
    }

    @KafkaListener(
            topics = "#{@topicConfig.userActions}",
            containerFactory = "userActionKafkaListenerContainerFactory"
    )
    public void consumeUserActions(UserActionAvro avro) {
        try {
            log.trace("{}: consumeUserActions() polled UserActionAvro: {}", className, avro);

            Long eventA = avro.getEventId();
            Long userId = avro.getUserId();

            // назначаем вес
            Double weight = 0.0;
            switch (avro.getActionType()) {
                case VIEW -> {
                    weight = 0.4;
                }
                case REGISTER -> {
                    weight = 0.8;
                }
                case LIKE -> {
                    weight = 1.0;
                }
            }
            log.trace("{}: calculated weight: {} for ActionType: {}", className, weight, avro.getActionType());

            // находим старый вес, или же 0.0, если его нет
            Double oldWeightA = maxWeight
                    .getOrDefault(eventA, Collections.emptyMap())
                    .getOrDefault(userId, 0.0);
            log.trace("{}: found oldWeightA: {}", className, oldWeightA);

            // высчитываем новый вес
            Double newWeightA = Math.max(oldWeightA, weight);
            log.trace("{}: calculated newWeightA: {}", className, newWeightA);

            // если вес поменялся, обновляем в мапе
            if (!newWeightA.equals(oldWeightA)) {
                maxWeight
                        .computeIfAbsent(eventA, e -> new HashMap<>())
                        .put(userId, newWeightA);
                log.trace("{}: maxWeight updated for userId: {}, new weight value: {}", className, userId, newWeightA);
            }

            // обновляем (S_a), сумму весов мероприятий
            Double deltaA = newWeightA - oldWeightA;
            eventWeightSum.merge(eventA, deltaA, Double::sum);
            log.trace("{}: eventWeightSum updated for eventA: {}, new value: {}",
                    className, eventA, eventWeightSum.get(eventA));

            for (Long eventB : maxWeight.keySet()) {
                // сверяем с каждым другим событием и только для события, связанного с userId
                if (!eventB.equals(eventA) && maxWeight.get(eventB).containsKey(userId)) {

                    Double weightB = maxWeight
                            .getOrDefault(eventB, Collections.emptyMap())
                            .getOrDefault(userId, 0.0);
                    log.trace("{}: found weightB: {}", className, weightB);

                    // S_min до и после, а также разница S_min
                    Double oldWeightMin = Math.min(oldWeightA, weightB);
                    log.trace("{}: calculated old S_min: {}", className, oldWeightMin);
                    Double newWeightMin = Math.min(newWeightA, weightB);
                    log.trace("{}: calculated new S_min: {}", className, newWeightMin);
                    Double deltaWeightMin = newWeightMin - oldWeightMin;
                    log.trace("{}: calculated delta between old S_min and new S_min: {}", className, deltaWeightMin);

                    // обновляем S_min(A, B), если они отличаются
                    if (deltaWeightMin > 0) {
                        Double newSMin = getMinWeightSum(eventA, eventB) + deltaWeightMin;
                        putMinWeightSum(eventA, eventB, newSMin);
                        log.trace("{}: minWeightSum updated for eventA: {}, eventB: {}, new value: {}",
                                className, eventA, eventB, newSMin);
                    }

                    // высчитываем similarity и собираем сообщение
                    Double similarity = calculateSimilarity(eventA, eventB);
                    log.trace("{}: calculated similarity between eventA: {} and eventB: {}, value: {}",
                            className, eventA, eventB, similarity);

                    EventPair eventPair = new EventPair(Math.min(eventA, eventB), Math.max(eventA, eventB));

                    Double oldSimilarity = eventsSimilarity.get(eventPair);
                    if (oldSimilarity == null || Double.compare(oldSimilarity, similarity) != 0) {
                        eventsSimilarity.put(eventPair, similarity);


                        EventSimilarityAvro similarityAvro = EventSimilarityAvro.newBuilder()
                                .setEventA(Math.min(eventA, eventB))
                                .setEventB(Math.max(eventA, eventB))
                                .setScore(similarity)
                                .setTimestamp(avro.getTimestamp())
                                .build();

                        sendAvro(similarityAvro);
                    }
                }
            }

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

    private void putMinWeightSum(Long eventA, Long eventB, Double sum) {
        Long first = Math.min(eventA, eventB);
        Long second = Math.max(eventA, eventB);

        minWeightSum
                .computeIfAbsent(first, e -> new HashMap<>())
                .put(second, sum);
    }

    private Double getMinWeightSum(Long eventA, Long eventB) {
        Long first = Math.min(eventA, eventB);
        Long second = Math.max(eventA, eventB);

        return minWeightSum
                .computeIfAbsent(first, e -> new HashMap<>())
                .getOrDefault(second, 0.0);
    }

    private Double calculateSimilarity(Long eventA, Long eventB) {
        // чтобы считалось одинаково, вне зависимости от порядка
        Long first = Math.min(eventA, eventB);
        Long second = Math.max(eventA, eventB);

        // S_min(Ip, Iq)
        Double sMin = getMinWeightSum(first, second);
        if (sMin == null) {
            // если не было найдено значение для second, назначаем 0.0
            sMin = 0.0;
        }

        // суммы уже должны храниться, берём их от-туда или же 0.0
        Double sIp = eventWeightSum.getOrDefault(eventA, 0.0);
        Double sIq = eventWeightSum.getOrDefault(eventB, 0.0);

        // проверка, что в знаменателе нет 0
        if (sIp == 0 || sIq == 0) {
            return 0.0;
        }

        // возвращаем значение по формуле = S_min(Ip, Iq) / корень(SIp) * корень(SIq)
        return sMin / (Math.sqrt(sIp) * Math.sqrt(sIq));
    }
}
