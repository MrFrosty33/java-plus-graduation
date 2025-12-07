package ru.yandex.practicum.collector.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Data
@Component
@ConfigurationProperties(prefix = "kafka-collector.producer")
public class KafkaProducerConfig {
    private Properties properties;
}
