package ru.yandex.practicum.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = {"ru.yandex.practicum.user", "ru.yandex.practicum.interaction.api"})
@ConfigurationPropertiesScan
@EntityScan(basePackages = {
        "ru.yandex.practicum.interaction.api.model.user"
})
public class UserApp {
    public static void main(String[] args) {
        SpringApplication.run(UserApp.class, args);
    }
    //todo comment, category & compilation вроде как по желанию, можно пока не выносить

    //todo 3) переносим какую-то свою функциональность

    //todo не забыть добавлять руты
}
