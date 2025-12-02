package ru.yandex.practicum.explore.with.me.stats;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.stats.client.StatsClient;
import ru.yandex.practicum.stats.dto.EndpointHitCreate;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class StatsSaver {
    private final StatsClient statsClient;
    @Value("${spring.application.name}")
    private String app;

    public void save(HttpServletRequest request, String controllerName) {
        EndpointHitCreate hitCreate = EndpointHitCreate.builder()
                .app(app)
                .ip(request.getRemoteAddr())
                .uri(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        System.out.println(hitCreate);
        log.info("STAT SAVER: {} saving stats with endpoint: {}", controllerName, hitCreate.getUri());
        ResponseEntity<Void> statResult = statsClient.createHit(hitCreate);
        if (statResult.getStatusCode().is2xxSuccessful()) {
            log.info("STAT SAVER: {} stats saved successfully", controllerName);
        } else {
            log.info("STAT SAVER: {} error acquired when saving stats", controllerName);
        }
    }
}
