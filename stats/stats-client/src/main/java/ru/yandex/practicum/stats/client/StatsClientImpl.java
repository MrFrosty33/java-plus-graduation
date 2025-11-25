package ru.yandex.practicum.stats.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.yandex.practicum.stats.dto.EndpointHitCreate;
import ru.yandex.practicum.stats.dto.ViewStats;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class StatsClientImpl implements StatsClient {
    private final RestClient client;
    private final DiscoveryClient discoveryClient;
    private final RetryTemplate retryTemplate;
    private final String statsServiceId;

    public StatsClientImpl(@Autowired DiscoveryClient discoveryClient,
                           @Value("${stats.service-id}") String statsServiceId) {
        this.discoveryClient = discoveryClient;
        this.statsServiceId = statsServiceId;
        this.client = RestClient.builder()
                .build();
        this.retryTemplate = new RetryTemplate();

        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(3000L);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        MaxAttemptsRetryPolicy retryPolicy = new MaxAttemptsRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);
    }

    public ResponseEntity<Void> createHit(EndpointHitCreate endpointHitCreate) {
        log.trace("STAT CLIENT: createHit() call with endpointHitCreate body: {}", endpointHitCreate);

        ResponseEntity<Void> result = client
                .post()
                .uri(makeUri("/hit"))
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
                .fromUri(makeUri("/stats"))
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

    private URI makeUri(String path) {
        ServiceInstance instance = retryTemplate.execute(cxt -> getStatsServiceInstance());
        return URI.create("http://" + instance.getHost() + ":" + instance.getPort() + path);
    }

    private ServiceInstance getStatsServiceInstance() {
        try {
            return discoveryClient
                    .getInstances(statsServiceId)
                    .getFirst();
        } catch (Exception exception) {
            log.warn("Stats service with id: {} is unavailable", statsServiceId);
            throw new RuntimeException("service with id:" + statsServiceId + " is unavailable");
        }
    }
}