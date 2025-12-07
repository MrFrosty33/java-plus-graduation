package ru.yandex.practicum.collector.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.yandex.practicum.collector.exception.ValueMismatchException;
import ru.yandex.practicum.collector.service.AvroKafkaProducer;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionHandler {
    private final String className = this.getClass().getSimpleName();

    private final AvroKafkaProducer producer;

    public void handle(String topic, UserActionProto proto) {
        UserActionAvro userActionAvro = UserActionAvro.newBuilder()
                .setUserId(proto.getUserId())
                .setEventId(proto.getEventId())
                .setActionType(mapActionType(proto.getActionType()))
                .setTimestamp(Instant.ofEpochSecond(proto.getTimestamp().getSeconds(), proto.getTimestamp().getNanos()))
                .build();

        producer.sendAvro(topic, userActionAvro);
        log.info("{}: UserActionAvro: {} sent to kafka", className, userActionAvro);
    }

    private ActionTypeAvro mapActionType(ActionTypeProto actionType) {
        switch (actionType) {
            case ACTION_VIEW -> {
                return ActionTypeAvro.VIEW;
            }
            case ACTION_REGISTER -> {
                return ActionTypeAvro.REGISTER;
            }
            case ACTION_LIKE -> {
                return ActionTypeAvro.LIKE;
            }
            default -> {
                log.warn("{}: cannot map ActionTypeProto with value: {} to ActionTypeAvro", className, actionType);
                throw new ValueMismatchException("Unrecognized action type received");
            }
        }
    }
}
