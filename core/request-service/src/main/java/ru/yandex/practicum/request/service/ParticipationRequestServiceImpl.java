package ru.yandex.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.explore.with.me.model.event.Event;
import ru.yandex.practicum.explore.with.me.repository.EventRepository;
import ru.yandex.practicum.explore.with.me.repository.ParticipationRequestRepository;
import ru.yandex.practicum.interaction.api.exception.ConflictException;
import ru.yandex.practicum.interaction.api.exception.NotFoundException;
import ru.yandex.practicum.interaction.api.feign.UserFeignClient;
import ru.yandex.practicum.interaction.api.mapper.ParticipationRequestMapper;
import ru.yandex.practicum.interaction.api.model.request.CancelParticipationRequest;
import ru.yandex.practicum.interaction.api.model.request.NewParticipationRequest;
import ru.yandex.practicum.interaction.api.model.request.ParticipationRequest;
import ru.yandex.practicum.interaction.api.model.request.ParticipationRequestDto;
import ru.yandex.practicum.interaction.api.model.request.ParticipationRequestStatus;
import ru.yandex.practicum.interaction.api.util.DataProvider;
import ru.yandex.practicum.interaction.api.util.ExistenceValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ParticipationRequestServiceImpl implements ParticipationRequestService,
        ExistenceValidator<ParticipationRequest>, DataProvider<ParticipationRequestDto, ParticipationRequest> {

    private final String className = this.getClass().getSimpleName();
    private static final String OBJECT_NOT_FOUND = "Required object was not found.";

    private final ParticipationRequestRepository participationRequestRepository;
    private final UserFeignClient userFeignClient;
    private final EventRepository eventRepository;
    private final ExistenceValidator<Event> eventExistenceValidator;
    private final ParticipationRequestMapper participationRequestMapper;


    @Override
    public List<ParticipationRequestDto> find(Long userId) {
        validateExists(userId);

        List<ParticipationRequestDto> result = participationRequestRepository.findAllByRequesterId(userId).stream()
                .map(this::getDto)
                .toList();
        log.info("{}: result of find(): {}", className, result);
        return result;
    }

    @Override
    @Transactional
    public ParticipationRequestDto create(NewParticipationRequest newParticipationRequest) {
        Long requesterId = newParticipationRequest.getUserId();
        Long eventId = newParticipationRequest.getEventId();

        if (participationRequestRepository.existsByRequesterIdAndEventId(
                requesterId, eventId)) {
            log.info("{}: attempt to create already existent participationRequest with requesterId: {}, eventId: {}",
                    className, requesterId, eventId);
            throw new ConflictException("Duplicate request.", "participationRequest with requesterId: " + requesterId +
                    ", and eventId: " + eventId + " already exists");
        }

        eventExistenceValidator.validateExists(eventId);
        validateUserExists(requesterId);

        Event event = eventRepository.findById(eventId).get();

        if (event.getInitiatorId().equals(requesterId)) {
            log.info("{}: attempt to create participationRequest by an event initiator with requesterId: {}, eventId: {}, " +
                    "initiatorId: {}", className, requesterId, eventId, event.getInitiatorId());
            throw new ConflictException("Initiator can't create participation request.", "requesterId: "
                    + requesterId + " equals to initiatorId: " + event.getInitiatorId());
        }

        if (event.getPublishedOn() == null) {
            log.info("{}: attempt to create participationRequest for not published event with " +
                    "requesterId: {}, eventId: {}", className, requesterId, eventId);
            throw new ConflictException("Can't create participation request for unpublished event.",
                    "event with id: " + eventId + " is not published yet");
        }

        if (event.getParticipantLimit() != 0) {
            List<ParticipationRequest> alreadyConfirmed = participationRequestRepository
                    .findAllByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
            AtomicInteger remainingSpots = new AtomicInteger(event.getParticipantLimit() - alreadyConfirmed.size());
            if (remainingSpots.get() <= 0) {
                log.info("{}: attempt to create participationRequest, but participantLimit: {} is reached",
                        className, event.getParticipantLimit());
                throw new ConflictException("Participant limit is reached.", "event with id: " + eventId +
                        " has participant limit of: " + event.getParticipantLimit());
            }
        }

        ParticipationRequest request = mapEntity(newParticipationRequest);
        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(ParticipationRequestStatus.CONFIRMED);
        }

        ParticipationRequestDto result = getDto(participationRequestRepository.save(request));
        log.info("{}: result of create():: {}", className, result);
        return result;
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancel(CancelParticipationRequest cancelParticipationRequest) {
        ParticipationRequest request = participationRequestRepository
                .findById(cancelParticipationRequest.getRequestId())
                .orElseThrow(() -> {
                    log.info("{}: attempt to find participationRequest with id: {}",
                            cancelParticipationRequest, cancelParticipationRequest.getRequestId());
                    return new NotFoundException("The required object was not found.",
                            "ParticipationRequest with id=" + cancelParticipationRequest.getRequestId() +
                                    " was not found");
                });
        validateUserExists(cancelParticipationRequest.getUserId());
        if (!request.getRequesterId().equals(cancelParticipationRequest.getUserId())) {
            log.info("{}: attempt to cancel participationRequest by not an owner", className);
            throw new ConflictException("Request can be cancelled only by an owner",
                    "User with id=" + cancelParticipationRequest.getUserId() +
                            " is not an owner of request with id=" + cancelParticipationRequest.getRequestId());
        }

        ParticipationRequestDto result = participationRequestMapper.toDto(
                participationRequestRepository.findById(cancelParticipationRequest.getRequestId()).get());
        result.setStatus(ParticipationRequestStatus.CANCELED);
        participationRequestRepository.deleteById(cancelParticipationRequest.getRequestId());

        log.info("{}: result of cancel(): {}, which has been deleted", className, result);
        return result;
    }

    private ParticipationRequest mapEntity(NewParticipationRequest newParticipationRequest) {
        Long userId = newParticipationRequest.getUserId();
        Long eventId = newParticipationRequest.getEventId();

        validateUserExists(userId);

        ParticipationRequest entity = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .requesterId(userId)
                .event(eventRepository.findById(eventId).orElseThrow(() -> {
                    log.info("{}: attempt to find event with id:{}", className, userId);
                    return new NotFoundException("The required object was not found.",
                            "Event with id=" + eventId + " was not found");
                }))
                .status(ParticipationRequestStatus.PENDING)
                .build();

        log.trace("{}: result of mapEntity(): {}", className, entity);
        return entity;
    }

    @Override
    public ParticipationRequestDto getDto(ParticipationRequest entity) {
        return participationRequestMapper.toDto(entity);
    }

    @Override
    public void validateExists(Long id) {
        if (participationRequestRepository.findById(id).isEmpty()) {
            log.info("{}: attempt to find participationRequest with id: {}", className, id);
            throw new NotFoundException("The required object was not found.",
                    "ParticipationRequest with id=" + id + " was not found");
        }
    }

    private void validateUserExists(Long userId) {
        userFeignClient.findById(userId)
                .orElseThrow(() -> {
                            log.info("{}: attempt to find user with id: {}", className, userId);
                            return new NotFoundException(
                                    OBJECT_NOT_FOUND,
                                    String.format("User with id: %d was not found", userId));
                        }
                );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isParticipantApproved(Long userId, Long eventId) {
        return participationRequestRepository
                .existsByRequesterIdAndEventIdAndStatus(
                        userId,
                        eventId,
                        ParticipationRequestStatus.CONFIRMED
                );
    }
}
