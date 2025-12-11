package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;
import ru.yandex.practicum.analyzer.mapper.InteractionMapper;
import ru.yandex.practicum.analyzer.mapper.SimilarityMapper;
import ru.yandex.practicum.analyzer.model.Interaction;
import ru.yandex.practicum.analyzer.model.Similarity;
import ru.yandex.practicum.analyzer.repository.InteractionRepository;
import ru.yandex.practicum.analyzer.repository.SimilarityRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyzerServiceImpl implements AnalyzerService {
    private final InteractionRepository interactionRepository;
    private final SimilarityRepository similarityRepository;
    private final InteractionMapper interactionMapper;
    private final SimilarityMapper similarityMapper;

    private final String className = this.getClass().getSimpleName();

    @Override
    public Stream<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        return Stream.empty();
    }

    @Override
    public Stream<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        List<Similarity> similarEvents = similarityRepository.findByEventIdAOrEventIdB(request.getEventId());
        List<Interaction> interactionsForUser = interactionRepository.findByUserId(request.getUserId());

        // собираем eventId, с которыми было взаимодействие
        Set<Long> interactedEvents = interactionsForUser.stream()
                .map(Interaction::getEventId)
                .collect(Collectors.toSet());

        return similarEvents.stream()
                .filter(similarity -> {
                    boolean interactedA = interactedEvents.contains(similarity.getEventIdA());
                    boolean interactedB = interactedEvents.contains(similarity.getEventIdB());

                    // отфильтровываем те схожжие события, в которых пользователь взаимодействовал с обоими
                    return !(interactedA && interactedB);
                })
                .peek(similarity -> log.trace("{}: result of getSimilarEvents() after filter: {}", className, similarity))
                // сортируем по-убыванию коэффицента похожести
                .sorted(Comparator.comparing(Similarity::getSimilarity).reversed())
                .peek(similarity -> log.trace("{}: result of getSimilarEvents() after sort: {}", className, similarity))
                // ограничиваем размер списка
                .limit(request.getMaxResults())
                .peek(similarity -> log.trace("{}: result of getSimilarEvents() after limit: {}", className, similarity))
                // маппим в RecommendedEventProto
                .map(similarity -> {
                    // в similarity либо eventA будет равен eventId из запроса
                    if (similarity.getEventIdA().equals(request.getEventId())) {
                        return RecommendedEventProto.newBuilder()
                                // тогда берём в качестве рекомендуемого B
                                .setEventId(similarity.getEventIdB())
                                .setScore(similarity.getSimilarity())
                                .build();
                    }
                    // либо же eventB
                    else {
                        return RecommendedEventProto.newBuilder()
                                // и берём в качестве рекомендуемого A
                                .setEventId(similarity.getEventIdA())
                                .setScore(similarity.getSimilarity())
                                .build();
                    }
                })
                .peek(proto -> log.trace("{}: result of getSimilarEvents() mapped toRecommendedEventProto : {}", className, proto));
    }

    @Override
    public Stream<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        List<Interaction> interactions = interactionRepository.findByEventIdIn(request.getEventIdList());

        // группируем по eventId и считаем сумму всех rating
        Map<Long, Double> eventRatingMap = interactions.stream()
                .collect(Collectors.groupingBy(
                        Interaction::getEventId,
                        Collectors.summingDouble(Interaction::getRating)
                ));

        // маппим в RecommendedEventProto
        return eventRatingMap.entrySet().stream()
                .map(entry -> RecommendedEventProto.newBuilder()
                        .setEventId(entry.getKey())
                        .setScore(entry.getValue())
                        .build()
                )
                .peek(proto -> log.trace("{}: result of getInteractionsCount: {}", className, proto));
    }
}
