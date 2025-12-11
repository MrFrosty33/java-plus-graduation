package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.analyzer.model.Interaction;
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

    @KafkaListener(
            topics = "#{@topicConfig.userActions}",
            containerFactory = "userActionKafkaListenerContainerFactory"
    )
    public void consumeUserActions(UserActionAvro avro) {
        try {
            log.trace("{}: consumeUserActions() polled UserActionAvro: {}", className, avro);
            Optional<Interaction> existingInteraction =
                    interactionRepository.findByUserIdAndEventId(avro.getUserId(), avro.getEventId());

            Double rating = 0.0;
            switch (avro.getActionType()) {
                case VIEW -> {
                    rating = 0.4;
                }
                case REGISTER -> {
                    rating = 0.8;
                }
                case LIKE -> {
                    rating = 1.0;
                }
            }

            if (existingInteraction.isPresent()) {
                if (existingInteraction.get().getRating() < rating) {
                    existingInteraction.get().setRating(rating);
                    //todo вопрос, надо ли обновлять ts
                    existingInteraction.get().setTimestamp(
                            LocalDateTime.ofInstant(avro.getTimestamp(), ZoneId.systemDefault()));
                }
            } else {
                Interaction interaction = Interaction.builder()
                        .userId(avro.getUserId())
                        .eventId(avro.getEventId())
                        .rating(rating)
                        .timestamp(LocalDateTime.now())
                        .build();

                interactionRepository.save(interaction);
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
            log.trace("{}: consumeEventSimilarity() polled EventSimilarityAvro: {}", className, avro);

        } catch (Exception e) {
            log.warn("{}: exception in consumeEventSimilarity(): ", className, e);
        }
    }
}
