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

            maxWeight
                    // если записи нет - помещаем новую
                    .computeIfAbsent(avro.getEventId(), e -> new HashMap<>())
                    // если же есть, помещает только в том случае, если weight выше того, что уже хранится
                    .merge(avro.getUserId(), weight, Math::max);

            if (eventWeightSum.containsKey(avro.getEventId())) {
                // если уже есть сумма весов, добавляем к значению
                eventWeightSum.put(avro.getEventId(), eventWeightSum.get(avro.getEventId()) + weight);
            } else {
                // если же нет, просто создаём новую запись
                eventWeightSum.put(avro.getEventId(), weight);
            }


        } catch (
                Exception e) {
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
        Double sMin = minWeightSum
                // берём вложенную Map<EventIdB, S_minWeight>
                .getOrDefault(first, Collections.emptyMap())
                // берём значение для second, если оно есть
                .get(second);
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
