package ru.yandex.practicum.comment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = {"ru.yandex.practicum.comment", "ru.yandex.practicum.interaction.api"})
@ConfigurationPropertiesScan
@EntityScan(basePackages = {
        "ru.yandex.practicum.interaction.api.model.comment"
})
public class CommentApp {
    public static void main(String[] args) {
        SpringApplication.run(CommentApp.class, args);
    }
}
