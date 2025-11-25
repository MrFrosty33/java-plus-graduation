package ru.yandex.practicum.explore.with.me;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = {"ru.yandex.practicum.explore.with.me", "ru.yandex.practicum.stats"})
@ConfigurationPropertiesScan
public class MainApp {
    public static void main(String[] args) {
        SpringApplication.run(MainApp.class, args);
    }
}
