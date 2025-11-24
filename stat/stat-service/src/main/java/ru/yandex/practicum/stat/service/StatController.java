package ru.yandex.practicum.stat.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.stat.dto.EndpointHitCreate;
import ru.yandex.practicum.stat.dto.ViewStats;
import ru.yandex.practicum.stat.service.exception.StatValidationException;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping
@RestController
@Slf4j
@Validated
public class StatController {
    private final StatService statService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void createHit(@Valid @RequestBody EndpointHitCreate hitCreate) {
        log.info("STAT CONTROLLER: Create hit: {}", hitCreate);
        statService.saveHit(hitCreate);
    }

    @GetMapping("/stats")
    public List<ViewStats> getStats(@RequestParam
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                        @NotNull
                                        LocalDateTime start,
                                    @RequestParam
                                        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                        @NotNull
                                        LocalDateTime end,
                                    @RequestParam(required = false) List<String> uris,
                                    @RequestParam(defaultValue = "false") boolean unique) {
        log.info("STAT CONTROLLER: Get stats: {}, {}, {}, {}", start, end, uris, unique);
        if (end.isBefore(start)) {
            throw new StatValidationException("Start date must be before end date");
        }
        return statService.getStats(start, end, uris, unique);
    }

}
