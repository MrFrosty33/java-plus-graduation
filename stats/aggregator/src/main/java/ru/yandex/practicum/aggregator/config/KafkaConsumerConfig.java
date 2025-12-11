package ru.yandex.practicum.aggregator.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {
    private final KafkaEventsSimilarityConsumerConfig eventsSimilarityConsumerConfig;
    private final KafkaUserActionsConsumerConfig userActionsConsumerConfig;

    @Bean
    public ConsumerFactory<Void, UserActionAvro> userActionsConsumerFactory() {
        // DefaultKafkaConsumerFactory требует Map<String, Object>, потому такой маппинг
        Map<String, Object> configMap = new HashMap<>();
        for (String name : userActionsConsumerConfig.getProperties().stringPropertyNames()) {
            configMap.put(name, userActionsConsumerConfig.getProperties().getProperty(name));
        }

        return new DefaultKafkaConsumerFactory<>(configMap);
    }

    @Bean
    public ConsumerFactory<Void, EventSimilarityAvro> eventSimilarityAvroConsumerFactory() {
        Map<String, Object> configMap = new HashMap<>();
        for (String name : eventsSimilarityConsumerConfig.getProperties().stringPropertyNames()) {
            configMap.put(name, eventsSimilarityConsumerConfig.getProperties().getProperty(name));
        }

        return new DefaultKafkaConsumerFactory<>(configMap);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Void, UserActionAvro> userActionKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Void, UserActionAvro> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userActionsConsumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Void, EventSimilarityAvro> eventSimilarityKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Void, EventSimilarityAvro> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(eventSimilarityAvroConsumerFactory());
        return factory;
    }
}
