package ru.yandex.practicum.analyzer.service;

import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.analyzer.config.ActionWeightConfig;
import ru.yandex.practicum.analyzer.model.Interaction;
import ru.yandex.practicum.analyzer.model.Similarity;
import ru.yandex.practicum.analyzer.repository.InteractionRepository;
import ru.yandex.practicum.analyzer.repository.SimilarityRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyzerServiceKafka {
    private final String className = this.getClass().getSimpleName();
    private final InteractionRepository interactionRepository;
    private final SimilarityRepository similarityRepository;
    private final ActionWeightConfig weightConfig;

    private final JsonMapper jsonMapper;

    @KafkaListener(
            topics = "#{@topicConfig.userActions}",
            containerFactory = "userActionKafkaListenerContainerFactory"
    )
    public void consumeUserActions(UserActionAvro avro) {
        try {
            //todo по какой-то причине этот метод вообще не вызывается, хотя collector отправляет в топик данные
            log.trace("{}: consumeUserActions() polled UserActionAvro: {}", className, jsonMapper.writeValueAsString(avro));
            Optional<Interaction> existingInteraction =
                    interactionRepository.findByUserIdAndEventId(avro.getUserId(), avro.getEventId());

            Double rating = getActionWeight(avro.getActionType());

            if (existingInteraction.isPresent()) {
                if (existingInteraction.get().getRating() < rating) {
                    existingInteraction.get().setRating(rating);
                    existingInteraction.get().setTimestamp(
                            LocalDateTime.ofInstant(avro.getTimestamp(), ZoneId.systemDefault()));

                    log.trace("{}: interaction was updated: {}", className, existingInteraction.get());
                }
            } else {
                Interaction interaction = Interaction.builder()
                        .userId(avro.getUserId())
                        .eventId(avro.getEventId())
                        .rating(rating)
                        .timestamp(LocalDateTime.now())
                        .build();

                interactionRepository.save(interaction);
                log.trace("{}: new interaction was created: {}", className, interaction);
            }
        } catch (Exception e) {
            log.warn("{}: exception in consumeUserActions(): ", className, e);
        }
    }

    @KafkaListener(
            topics = "#{@topicConfig.eventsSimilarity}",
            containerFactory = "eventSimilarityKafkaListenerContainerFactory"
    )
    public void consumeEventSimilarity(EventSimilarityAvro avro) {
        try {
            log.trace("{}: consumeEventSimilarity() polled EventSimilarityAvro: {}", className, jsonMapper.writeValueAsString(avro));
            Optional<Similarity> existingSimilarity =
                    similarityRepository.findByEventIdAAndEventIdB(avro.getEventA(), avro.getEventB());

            if (existingSimilarity.isPresent()) {
                existingSimilarity.get().setSimilarity(avro.getScore());
                existingSimilarity.get().setTimestamp(LocalDateTime.now());

                log.trace("{}: similarity was updated: {}", className, existingSimilarity.get());
            } else {
                Similarity similarity = Similarity.builder()
                        .eventIdA(avro.getEventA())
                        .eventIdB(avro.getEventB())
                        .similarity(avro.getScore())
                        .timestamp(LocalDateTime.now())
                        .build();

                similarityRepository.save(similarity);
                log.trace("{}: new similarity was created: {}", className, similarity);
            }
        } catch (Exception e) {
            log.warn("{}: exception in consumeEventSimilarity(): ", className, e);
        }
    }

    private double getActionWeight(ActionTypeAvro action) {
        return switch (action) {
            case VIEW -> weightConfig.getView();
            case REGISTER -> weightConfig.getRegister();
            case LIKE -> weightConfig.getLike();
        };
    }
}
