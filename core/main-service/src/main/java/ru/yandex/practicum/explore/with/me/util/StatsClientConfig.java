package ru.yandex.practicum.explore.with.me.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.explore.with.me.exception.StatsServerUnavailable;

@Configuration
@RequiredArgsConstructor
public class StatsClientConfig {
    private final DiscoveryClient discoveryClient;

    @Value("${stats.service-id}")
    private String statsServiceId;
    //todo вопрос, будет ли это работать. Смотреть по тестам

    @Bean
    public ServiceInstance statClient() {
        try {
            return discoveryClient
                    .getInstances(statsServiceId)
                    .getFirst();
        } catch (Exception exception) {
            throw new StatsServerUnavailable(
                    "Stats service with id: " + statsServiceId + " unavailable",
                    exception.getMessage()
            );
        }
    }
}
