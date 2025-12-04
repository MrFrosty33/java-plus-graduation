package ru.yandex.practicum.interaction.api.model.request;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;

@Convert
public class ParticipationRequestStatusConverter implements AttributeConverter<ParticipationRequestStatus, String> {
    @Override
    public String convertToDatabaseColumn(ParticipationRequestStatus status) {
        return status == null ? null : status.name().toLowerCase();
    }

    @Override
    public ParticipationRequestStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ParticipationRequestStatus.valueOf(dbData.toUpperCase());
    }
}
