package ru.yandex.practicum.explore.with.me;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"ru.yandex.practicum.explore.with.me", "ru.yandex.practicum.stats"})
@EnableFeignClients(basePackages = "ru.yandex.practicum.interaction.api.feign")
@ConfigurationPropertiesScan
public class MainApp {
    public static void main(String[] args) {
        SpringApplication.run(MainApp.class, args);
    }
}
