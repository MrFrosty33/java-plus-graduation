package ru.yandex.practicum.stats.client;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendationsControllerGrpc;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class GrpcAnalyzerClient implements AnalyzerClient {
    private final RecommendationsControllerGrpc.RecommendationsControllerBlockingStub stub;

    public GrpcAnalyzerClient(@GrpcClient("analyzer")
                              RecommendationsControllerGrpc.RecommendationsControllerBlockingStub stub) {
        this.stub = stub;
    }

    @Override
    public Stream<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        Iterator<RecommendedEventProto> iterator = stub.getRecommendationsForUser(request);
        return asStream(iterator);
    }

    @Override
    public Stream<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        Iterator<RecommendedEventProto> iterator = stub.getSimilarEvents(request);
        return asStream(iterator);
    }

    @Override
    public Stream<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        Iterator<RecommendedEventProto> iterator = stub.getInteractionsCount(request);
        return asStream(iterator);
    }

    private Stream<RecommendedEventProto> asStream(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }
}
