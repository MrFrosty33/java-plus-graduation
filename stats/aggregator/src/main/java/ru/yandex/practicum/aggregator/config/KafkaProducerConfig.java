package ru.yandex.practicum.aggregator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Data
@Component
@ConfigurationProperties(prefix = "kafka-aggregator.events-similarity-producer")
public class KafkaProducerConfig {
    private Properties properties;
}
