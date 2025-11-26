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
    //todo comment, category & compilation вроде как по желанию, можно пока не выносить
    //todo circuit breaker, если сервис недоступен - возвращает значение 0

    //todo 1) user service
    // перенести контроллер, репозиторий, сервис.
    // копировать исключения, дтошки, приложение
    // копировать схему, вырезать лишнее. Копировать application.yml, заменить имя
    // настроить рут
    // добавить в конфиг сервер конфиги

    //todo дальше удалить внешние ключи на таблицу users
    // везде, где использовался User -> меняем на Long id
    // userRepository -> userClient в отдельном модуле, в клиенте необходимые методы через FeignClient

    //todo в event был пользователь, теперь его не будет, формируем список айдишников пользователей
    // потом проходимся и формируем userDto или что требуется

    //todo 2) переносим request service

    //todo 3) переносим какую-то свою функциональность

    //todo 4) переименовываем main-service -> event-service
}
