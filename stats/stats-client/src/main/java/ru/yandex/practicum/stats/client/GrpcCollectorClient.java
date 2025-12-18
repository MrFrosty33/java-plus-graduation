package ru.yandex.practicum.stats.client;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;

@Component
public class GrpcCollectorClient implements CollectorClient {
    private final UserActionControllerGrpc.UserActionControllerBlockingStub stub;

    public GrpcCollectorClient(@GrpcClient("collector")
                               UserActionControllerGrpc.UserActionControllerBlockingStub stub) {
        this.stub = stub;
    }

    @Override
    public void collectUserAction(UserActionProto request) {
        stub.collectUserAction(request);
    }
}
