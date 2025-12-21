package ru.yandex.practicum.analyzer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "action-weights")
public class ActionWeightConfig {
    private Double view;
    private Double register;
    private Double like;
}
