package ru.yandex.practicum.stats.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.yandex.practicum.stats.dto.EndpointHitCreate;
import ru.yandex.practicum.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class StatsClientImpl implements StatsClient {
    private final RestClient client;
    @Value("${stat.server-url}")
    private String serverUrl;

    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl(serverUrl)
                .build();
    }

    public ResponseEntity<Void> createHit(EndpointHitCreate endpointHitCreate) {
        log.trace("STAT CLIENT: createHit() call with endpointHitCreate body: {}", endpointHitCreate);

        ResponseEntity<Void> result = client
                .post()
                .uri("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(endpointHitCreate)
                .retrieve()
                .toEntity(Void.class);

        if (result.getStatusCode().is2xxSuccessful()) {
            log.info("STAT CLIENT: createHit() success with status: {}",
                    result.getStatusCode());
        } else {
            log.warn("STAT CLIENT: createHit() failure with status: {}",
                    result.getStatusCode());
        }

        return result;
    }

    public ResponseEntity<List<ViewStats>> getStats(LocalDateTime start,
                                                    LocalDateTime end,
                                                    List<String> uris,
                                                    boolean unique) {
        log.info("STAT CLIENT: getStats() call with params: start={}, end={}, uris={}, unique={}",
                start, end, uris, unique);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(serverUrl + "/stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("unique", unique);
        uris.forEach(uri -> builder.queryParam("uris", uri));
        String path = builder.toUriString();

        log.info("STAT CLIENT: final Uri : {}", path);

        ResponseEntity<List<ViewStats>> result = client
                .get()
                .uri(path)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<ViewStats>>() {
                });

        if (result.getStatusCode().is2xxSuccessful()) {
            log.info("STAT CLIENT: getStats() success with status: {}, body: {}",
                    result.getStatusCode(), result.getBody());
        } else {
            log.info("STAT CLIENT: getStats() failure with status: {}, body: {}",
                    result.getStatusCode(), result.getBody());
        }

        return result;
    }
}