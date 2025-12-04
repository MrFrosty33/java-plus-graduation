package ru.yandex.practicum.user.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.interaction.api.feign.UserClient;
import ru.yandex.practicum.interaction.api.model.user.UserDto;
import ru.yandex.practicum.user.service.UserService;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
@Validated
public class UserInternalController implements UserClient {
    private final UserService service;
    private final String controllerName = this.getClass().getSimpleName();

    @GetMapping
    public UserDto findById(@RequestParam
                            @Positive
                            Long id) {
        log.trace("{}: findById() call with id: {}", controllerName, id);
        return service.findById(id);
    }
}
