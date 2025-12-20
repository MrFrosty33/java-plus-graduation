package ru.yandex.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;
import ru.yandex.practicum.analyzer.config.NearestNeighbours;
import ru.yandex.practicum.analyzer.model.Interaction;
import ru.yandex.practicum.analyzer.model.Similarity;
import ru.yandex.practicum.analyzer.repository.InteractionRepository;
import ru.yandex.practicum.analyzer.repository.SimilarityRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
    private final NearestNeighbours nearestNeighbours;

    private final String className = this.getClass().getSimpleName();

    //todo возможно стоит добавить логирование trace всякого барахла
    //todo score считается неверно :(((((

    @Override
    public Stream<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        List<Interaction> interactionsForUser = interactionRepository.findByUserId(request.getUserId());
        // если не было взаимодействий, то и рекомендовать пока что нечего
        if (interactionsForUser.isEmpty()) {
            return Stream.empty();
        }

        // сортируем и ограничиваем список взаимодействий
        interactionsForUser = interactionsForUser.stream()
                .sorted(Comparator.comparing(Interaction::getTimestamp).reversed())
                .peek(interaction -> {
                    log.trace("{}: getRecommendationsForUser() -> value of interactionsForUser after sort: {}", className, interaction);
                })
                .limit(request.getMaxResults())
                .peek(interaction -> {
                    log.trace("{}: getRecommendationsForUser() -> value of interactionsForUser after limit: {}", className, interaction);
                })
                .toList();

        // список уже просмотренных событий
        Set<Long> alreadyWatchedEvents = interactionsForUser.stream()
                .map(Interaction::getEventId)
                .peek(eventId -> {
                    log.trace("{}: getRecommendationsForUser() -> value of alreadyWatchedEvents: {}", className, eventId);
                })
                .collect(Collectors.toSet());

        // ищем похожие мероприятия
        List<Similarity> similarEvents = similarityRepository.findByEventIdAOrEventIdBIn(
                interactionsForUser.stream()
                        .map(Interaction::getEventId)
                        .collect(Collectors.toList())
        );

        // старый вариант
//        Set<Long> notInteractedEvents = similarEvents.stream()
//                // сортируем по коэффиценту похожести, от большего к меньшему
//                .sorted(Comparator.comparing(Similarity::getSimilarity).reversed())
//                .peek(similarity -> {
//                    log.trace("{}: getRecommendationsForUser() -> value of notInteractedEvents after sort: {}", className, similarity);
//                })
//                // ограничиваем выборку
//                .limit(request.getMaxResults())
//                .peek(similarity -> {
//                    log.trace("{}: getRecommendationsForUser() -> value of notInteractedEvents after limit: {}", className, similarity);
//                })
//                // берём все айдишники мероприятий, будь то A или B
//                .flatMap(similarity -> Stream.of(similarity.getEventIdA(), similarity.getEventIdB()))
//                .peek(similarity -> {
//                    log.trace("{}: getRecommendationsForUser() -> value of notInteractedEvents after flatMap: {}", className, similarity);
//                })
//                // отфильтровываем уже просмотренные
//                .filter(id -> !alreadyWatchedEvents.contains(id))
//                .peek(similarity -> {
//                    log.trace("{}: getRecommendationsForUser() -> value of notInteractedEvents after filter: {}", className, similarity);
//                })
//                .collect(Collectors.toSet());

        Set<Long> notInteractedEvents = similarEvents.stream()
                // преобразовываем схожести в кандидатов
                .flatMap(similarity -> Stream.of(
                        Map.entry(similarity.getEventIdA(), similarity.getSimilarity()),
                        Map.entry(similarity.getEventIdB(), similarity.getSimilarity())
                ))
                .peek(similarity -> {
                    log.trace("{}: getRecommendationsForUser() -> value of notInteractedEvents after flatMap: {}", className, similarity);
                })
                // отсеиваем просмотренные
                .filter(e -> !alreadyWatchedEvents.contains(e.getKey()))
                .peek(similarity -> {
                    log.trace("{}: getRecommendationsForUser() -> value of notInteractedEvents after filter: {}", className, similarity);
                })
                // берём максимальную схожесть для события
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        Double::max
                ))
                .entrySet().stream()
                // сортируем
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .peek(similarity -> {
                    log.trace("{}: getRecommendationsForUser() -> value of notInteractedEvents after sort: {}", className, similarity);
                })
                // ограничиваем
                .limit(request.getMaxResults())
                .peek(similarity -> {
                    log.trace("{}: getRecommendationsForUser() -> value of notInteractedEvents after flatMap: {}", className, similarity);
                })
                // берём ключи и собираем в сет
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());


        // K является константой, берётся из конфига. Наставник посоветовал задать 20.
        int K = nearestNeighbours.getValue(); // количество ближайших соседей
        Map<Long, List<Similarity>> nearestNeighbors = new HashMap<>();

        for (Interaction interaction : interactionsForUser) {
            Long recentEventId = interaction.getEventId();

            // список схожих к текущему событию
            List<Similarity> similarities = similarityRepository.findByEventIdAOrEventIdB(recentEventId);

            List<Similarity> neighbors = similarities.stream()
                    // берём только те, с которыми уже было взаимодействие
                    .filter(similarity -> {
                        Long neighborId;

                        if (similarity.getEventIdA().equals(recentEventId)) {
                            neighborId = similarity.getEventIdB();
                        } else {
                            neighborId = similarity.getEventIdA();
                        }

                        return alreadyWatchedEvents.contains(neighborId);
                    })
                    .peek(similarity -> {
                        log.trace("{}: getRecommendationsForUser() -> value of neighbors after filter: {}", className, similarity);
                    })
                    // сортируем по коэффиценту похожести, от большего к меньшему
                    .sorted(Comparator.comparing(Similarity::getSimilarity).reversed())
                    .peek(similarity -> {
                        log.trace("{}: getRecommendationsForUser() -> value of neighbors after sort: {}", className, similarity);
                    })
                    .limit(K)
                    .peek(similarity -> {
                        log.trace("{}: getRecommendationsForUser() -> value of neighbors after limit: {}", className, similarity);
                    })
                    .toList();

            nearestNeighbors.put(recentEventId, neighbors);
        }

        // id соседних мероприятий
        Set<Long> neighborEventIds = findNeighborEventIds(nearestNeighbors);

        // мапа событие - оценка
        Map<Long, Double> neighborRatingMap = interactionRepository
                .findByUserIdAndEventIdIn(request.getUserId(), new ArrayList<>(neighborEventIds))
                .stream()
                .collect(Collectors.toMap(Interaction::getEventId, Interaction::getRating));


        // чтобы использовать в stream, должно быть final / effectively final
        List<Interaction> finalInteractionsForUser = interactionsForUser;

        return notInteractedEvents.stream()
                .map(newEventId -> {
                    // находим соседей нового события среди недавно просмотренных
                    List<Similarity> neighbors = finalInteractionsForUser.stream()
                            .flatMap(interaction -> similarityRepository.findByEventIdAOrEventIdB(interaction.getEventId()).stream())
                            .filter(similarity -> {
                                Long neighborId;
                                if (similarity.getEventIdA().equals(newEventId)) {
                                    neighborId = similarity.getEventIdB();
                                } else {
                                    neighborId = similarity.getEventIdA();
                                }
                                return alreadyWatchedEvents.contains(neighborId);
                            })
                            // сортируем по убыванию похожести
                            .sorted(Comparator.comparing(Similarity::getSimilarity).reversed())
                            // берём K ближайших соседей
                            .limit(K)
                            .toList();

                    // сумма взвешенных оценок
                    double weightedSum = neighbors.stream()
                            .mapToDouble(sim -> {
                                Long neighborId;
                                if (sim.getEventIdA().equals(newEventId)) {
                                    neighborId = sim.getEventIdB();
                                } else {
                                    neighborId = sim.getEventIdA();
                                }

                                Double rating = neighborRatingMap.get(neighborId);
                                if (rating == null) {
                                    return 0.0;
                                }

                                double similarity = sim.getSimilarity();
                                return rating * similarity;
                            })
                            .sum();
                    log.trace("{}: getRecommendationsForUser() -> value of weightedSum: {}", className, weightedSum);

                    // сумма коэффицента подобия
                    double similaritySum = neighbors.stream()
                            .mapToDouble(Similarity::getSimilarity)
                            .sum();
                    log.trace("{}: getRecommendationsForUser() -> value of similaritySum: {}", className, similaritySum);

                    // ожидаемый рейтинг
                    double predictedRating;
                    if (similaritySum == 0.0) {
                        predictedRating = 0.0;
                    } else {
                        predictedRating = weightedSum / similaritySum;
                    }
                    log.trace("{}: getRecommendationsForUser() -> value of predictedRating: {}", className, predictedRating);

                    return RecommendedEventProto.newBuilder()
                            .setEventId(newEventId)
                            .setScore(predictedRating)
                            .build();
                });
    }

    private Set<Long> findNeighborEventIds(Map<Long, List<Similarity>> nearestNeighbors) {
        return nearestNeighbors.values().stream()
                // берём value List<Similarity>
                .flatMap(List::stream)
                .peek(similarity -> {
                    log.trace("{}: getRecommendationsForUser() -> value of neighborEventIds after flatMap: {}", className, similarity);
                })
                // мапим id соседа
                .map(similarity -> {
                    if (nearestNeighbors.containsKey(similarity.getEventIdA())) {
                        return similarity.getEventIdB();
                    } else {
                        return similarity.getEventIdA();
                    }
                })
                .peek(similarity -> {
                    log.trace("{}: getRecommendationsForUser() -> value of neighborEventIds after map: {}", className, similarity);
                })
                .collect(Collectors.toSet());
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
        log.trace("{}: request содержит следующий айдишники: {}", className, request.getEventIdList());
        List<Interaction> interactions = interactionRepository.findByEventIdIn(request.getEventIdList());
        log.trace("{}: нашел следующие interactions: {}", className, interactions);

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
