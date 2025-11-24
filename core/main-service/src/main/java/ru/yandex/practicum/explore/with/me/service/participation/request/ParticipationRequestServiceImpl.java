package ru.yandex.practicum.explore.with.me.service.participation.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.explore.with.me.exception.ConflictException;
import ru.yandex.practicum.explore.with.me.exception.NotFoundException;
import ru.yandex.practicum.explore.with.me.mapper.ParticipationRequestMapper;
import ru.yandex.practicum.explore.with.me.model.event.Event;
import ru.yandex.practicum.explore.with.me.model.participation.CancelParticipationRequest;
import ru.yandex.practicum.explore.with.me.model.participation.NewParticipationRequest;
import ru.yandex.practicum.explore.with.me.model.participation.ParticipationRequest;
import ru.yandex.practicum.explore.with.me.model.participation.ParticipationRequestDto;
import ru.yandex.practicum.explore.with.me.model.participation.ParticipationRequestStatus;
import ru.yandex.practicum.explore.with.me.model.user.User;
import ru.yandex.practicum.explore.with.me.repository.EventRepository;
import ru.yandex.practicum.explore.with.me.repository.ParticipationRequestRepository;
import ru.yandex.practicum.explore.with.me.repository.UserRepository;
import ru.yandex.practicum.explore.with.me.util.DataProvider;
import ru.yandex.practicum.explore.with.me.util.ExistenceValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ParticipationRequestServiceImpl implements ParticipationRequestService,
        ExistenceValidator<ParticipationRequest>, DataProvider<ParticipationRequestDto, ParticipationRequest> {

    private final String className = this.getClass().getSimpleName();
    private final ParticipationRequestRepository participationRequestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ExistenceValidator<User> userExistenceValidator;
    private final ExistenceValidator<Event> eventExistenceValidator;
    private final ParticipationRequestMapper participationRequestMapper;


    @Override
    public List<ParticipationRequestDto> find(Long userId) {
        userExistenceValidator.validateExists(userId);

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
        userExistenceValidator.validateExists(requesterId);

        Event event = eventRepository.findById(eventId).get();

        if (event.getInitiator().getId().equals(requesterId)) {
            log.info("{}: attempt to create participationRequest by an event initiator with requesterId: {}, eventId: {}, " +
                    "initiatorId: {}", className, requesterId, eventId, event.getInitiator().getId());
            throw new ConflictException("Initiator can't create participation request.", "requesterId: "
                    + requesterId + " equals to initiatorId: " + event.getInitiator().getId());
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
        userExistenceValidator.validateExists(cancelParticipationRequest.getUserId());
        if (!request.getRequester().getId().equals(cancelParticipationRequest.getUserId())) {
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

        ParticipationRequest entity = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .requester(userRepository.findById(userId).orElseThrow(() -> {
                    log.info("{}: attempt to find user with id:{}", className, userId);
                    return new NotFoundException("The required object was not found.",
                            "User with id=" + userId + " was not found");
                }))
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
