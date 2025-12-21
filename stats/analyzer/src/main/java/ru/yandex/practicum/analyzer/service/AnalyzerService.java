package ru.yandex.practicum.analyzer.service;

import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;

import java.util.List;

public interface AnalyzerService {
    // вопрос: раньше возвращал Stream, сейчас переделал на List. Как лучше быть?
    // будто бы одно и то же, но Stream нужно будет закрывать в контроллере или же try-with-resources
    // а для List просто в каждом методе Stream в него мапить
    List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request);

    List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request);

    List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request);
}
