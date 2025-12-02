package ru.yandex.practicum.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = {"ru.yandex.practicum.request", "ru.yandex.practicum.interaction.api"})
@ConfigurationPropertiesScan
@EntityScan(basePackages = {
        "ru.yandex.practicum.interaction.api.model.request"
})
public class RequestApp {
    public static void main(String[] args) {
        SpringApplication.run(RequestApp.class, args);
    }
}
