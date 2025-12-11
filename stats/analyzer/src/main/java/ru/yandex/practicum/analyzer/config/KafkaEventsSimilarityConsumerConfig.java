package ru.yandex.practicum.analyzer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Data
@Component
@ConfigurationProperties(prefix = "kafka-analyzer.events-similarity-consumer")
public class KafkaEventsSimilarityConsumerConfig {
    private Properties properties;
}
