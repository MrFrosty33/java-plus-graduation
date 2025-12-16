package ru.yandex.practicum.stats.client;

import ru.practicum.ewm.stats.proto.UserActionProto;

public interface CollectorClient {
    void collectUserAction(UserActionProto request);
}
