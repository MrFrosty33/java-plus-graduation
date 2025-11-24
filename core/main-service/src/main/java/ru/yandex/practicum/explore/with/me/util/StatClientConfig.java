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
public class StatClientConfig {
    private final DiscoveryClient discoveryClient;

    @Value("${stat.service-id}")
    private String statsServiceId;

    @Bean
    public ServiceInstance statClient() {
        try {
            return discoveryClient
                    .getInstances(statsServiceId)
                    .getFirst();
        } catch (Exception exception) {
            throw new StatsServerUnavailable(
                    "Stat service with id: " + statsServiceId + " unavailable",
                    exception.getMessage()
            );
        }
    }
}
