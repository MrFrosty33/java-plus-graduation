package ru.yandex.practicum.explore.with.me.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import ru.yandex.practicum.stat.client.StatClient;
import ru.yandex.practicum.stat.client.StatClientImpl;

@Configuration
public class StatClientConfig {

    @Bean
    public StatClient statClient(@Value("${stat.server-url}") String serverUrl) {
        RestClient client = RestClient.builder()
                .baseUrl(serverUrl)
                .build();

        return new StatClientImpl(client);
    }
}
