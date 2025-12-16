package ru.yandex.practicum.stats.client;

import net.devh.boot.grpc.client.inject.GrpcClient;
import ru.practicum.ewm.stats.proto.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.UserActionProto;

public class GrpcCollectorClient implements CollectorClient {
    @GrpcClient("collector")
    private final UserActionControllerGrpc.UserActionControllerBlockingStub stub;

    public GrpcCollectorClient(UserActionControllerGrpc.UserActionControllerBlockingStub stub) {
        this.stub = stub;
    }

    @Override
    public void collectUserAction(UserActionProto request) {
        stub.collectUserAction(request);
    }
}
