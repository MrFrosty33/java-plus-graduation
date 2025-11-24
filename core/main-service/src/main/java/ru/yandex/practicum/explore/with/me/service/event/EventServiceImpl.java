package ru.yandex.practicum.explore.with.me.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.explore.with.me.exception.BadRequestException;
import ru.yandex.practicum.explore.with.me.exception.ConflictException;
import ru.yandex.practicum.explore.with.me.exception.NotFoundException;
import ru.yandex.practicum.explore.with.me.mapper.EventMapper;
import ru.yandex.practicum.explore.with.me.mapper.ParticipationRequestMapper;
import ru.yandex.practicum.explore.with.me.model.category.Category;
import ru.yandex.practicum.explore.with.me.model.event.Event;
import ru.yandex.practicum.explore.with.me.model.event.EventPublicSort;
import ru.yandex.practicum.explore.with.me.model.event.EventState;
import ru.yandex.practicum.explore.with.me.model.event.EventStatistics;
import ru.yandex.practicum.explore.with.me.model.event.PublicEventParam;
import ru.yandex.practicum.explore.with.me.model.event.dto.EventFullDto;
import ru.yandex.practicum.explore.with.me.model.event.dto.EventRequestCount;
import ru.yandex.practicum.explore.with.me.model.event.dto.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.explore.with.me.model.event.dto.EventRequestStatusUpdateResult;
import ru.yandex.practicum.explore.with.me.model.event.dto.EventShortDto;
import ru.yandex.practicum.explore.with.me.model.event.dto.EventViewsParameters;
import ru.yandex.practicum.explore.with.me.model.event.dto.NewEventDto;
import ru.yandex.practicum.explore.with.me.model.event.dto.StatusUpdateRequest;
import ru.yandex.practicum.explore.with.me.model.event.dto.UpdateEventUserAction;
import ru.yandex.practicum.explore.with.me.model.event.dto.UpdateEventUserRequest;
import ru.yandex.practicum.explore.with.me.model.participation.ParticipationRequest;
import ru.yandex.practicum.explore.with.me.model.participation.ParticipationRequestDto;
import ru.yandex.practicum.explore.with.me.model.participation.ParticipationRequestStatus;
import ru.yandex.practicum.explore.with.me.model.user.User;
import ru.yandex.practicum.explore.with.me.repository.CategoryRepository;
import ru.yandex.practicum.explore.with.me.repository.EventRepository;
import ru.yandex.practicum.explore.with.me.repository.ParticipationRequestRepository;
import ru.yandex.practicum.explore.with.me.repository.UserRepository;
import ru.yandex.practicum.explore.with.me.util.ExistenceValidator;
import ru.yandex.practicum.explore.with.me.util.StatsGetter;
import ru.yandex.practicum.stat.dto.ViewStats;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements ExistenceValidator<Event>, EventService {
    private final String className = this.getClass().getSimpleName();

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final StatsGetter statsGetter;
    private final ParticipationRequestRepository requestRepository;
    private final ParticipationRequestMapper requestMapper;

    @Transactional
    @Override
    public EventFullDto createEvent(long userId, NewEventDto eventDto) {
        User user = findUserByIdOrElseThrow(userId);

        long categoryId = eventDto.getCategory();
        Category category = findCategoryByIdOrElseThrow(categoryId);

        Event event = eventMapper.toModel(eventDto);
        event.setInitiator(user);
        event.setCategory(category);
        event.setState(EventState.PENDING);
        Event eventSaved = eventRepository.save(event);
        EventFullDto eventFullDto = eventMapper.toFullDto(eventSaved);
        eventFullDto.setViews(0L);

        log.info("{}: result of createEvent(): {}", className, eventFullDto);
        return eventFullDto;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getPrivateEventById(long userId, long eventId) {
        Event event = getEventIfInitiatedByUser(userId, eventId);
        List<Event> events = List.of(event);
        LocalDateTime startStats = event.getCreatedOn().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime endStats = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        EventStatistics stats = getEventStatistics(events, startStats, endStats);
        EventFullDto result = eventMapper.toFullDtoWithStats(event, stats);
        log.info("{}: result of getPrivateEventById(): {}", className, result);
        return result;
    }

    @Transactional
    @Override
    public EventFullDto updateEvent(long userId, long eventId, UpdateEventUserRequest updateEvent) {
        Event event = getEventIfInitiatedByUser(userId, eventId);

        if (event.getState() == EventState.PUBLISHED) {
            log.info("User {} cannot change an event {} with state PUBLISHED", userId, eventId);
            throw new ConflictException("For the requested operation the conditions are not met.",
                    "Only pending or canceled events can be changed");
        }

        if (updateEvent.getCategory() != null) {
            long categoryId = updateEvent.getCategory();
            Category category = findCategoryByIdOrElseThrow(categoryId);
            event.setCategory(category);
        }

        if (updateEvent.getStateAction() != null) {
            UpdateEventUserAction action = updateEvent.getStateAction();
            switch (action) {
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
                case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
            }
        }

        if (updateEvent.getAnnotation() != null) {
            event.setAnnotation(updateEvent.getAnnotation());
        }
        if (updateEvent.getDescription() != null) {
            event.setDescription(updateEvent.getDescription());
        }
        if (updateEvent.getTitle() != null) {
            event.setTitle(updateEvent.getTitle());
        }
        if (updateEvent.getEventDate() != null) {
            event.setEventDate(updateEvent.getEventDate());
        }
        if (updateEvent.getLocation() != null) {
            event.setLocation(updateEvent.getLocation());
        }
        if (updateEvent.getPaid() != null) {
            event.setPaid(updateEvent.getPaid());
        }
        if (updateEvent.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEvent.getParticipantLimit());
        }
        if (updateEvent.getRequestModeration() != null) {
            event.setRequestModeration(updateEvent.getRequestModeration());
        }
        eventRepository.save(event);
        List<Event> events = List.of(event);
        LocalDateTime startStats = event.getCreatedOn().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime endStats = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        EventStatistics stats = getEventStatistics(events, startStats, endStats);
        EventFullDto result = eventMapper.toFullDtoWithStats(event, stats);
        log.info("{}: result of updateEvent(): {}", className, result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsByUser(long userId, int from, int count) {
        User user = findUserByIdOrElseThrow(userId);
        Pageable pageable = PageRequest.of(from, count, Sort.by("createdOn").ascending());
        List<Event> events = eventRepository.findEventsByUser(user, pageable).getContent();
        if (events.isEmpty()) {
            return List.of();
        }
        LocalDateTime startStats = events.getFirst().getCreatedOn().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime endStats = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        EventStatistics stats = getEventStatistics(events, startStats, endStats);
        List<EventShortDto> result = events.stream()
                .map(event -> eventMapper.toShortDtoWithStats(event, stats))
                .toList();
        log.info("{}: result of getEventsByUser(): {}", className, result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventParticipationRequestsByUser(long userId, long eventId) {
        getEventIfInitiatedByUser(userId, eventId);
        List<ParticipationRequest> requestsByEventId = requestRepository.findAllByEventId(eventId);
        return requestsByEventId.stream().map(requestMapper::toDto).toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequestStatus(long userId, long eventId,
                                                                   EventRequestStatusUpdateRequest updateRequest) {
        Event event = getEventIfInitiatedByUser(userId, eventId);
        List<ParticipationRequest> requestsByEventId = requestRepository.findAllById(updateRequest.getRequestIds());

        if (event.getParticipantLimit() == 0 || !event.isRequestModeration()) {
            return new EventRequestStatusUpdateResult(
                    requestsByEventId.stream().map(requestMapper::toDto).toList(), List.of());
        }

        List<ParticipationRequest> alreadyConfirmed = requestRepository
                .findAllByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
        AtomicInteger remainingSpots = new AtomicInteger(event.getParticipantLimit() - alreadyConfirmed.size());

        if (remainingSpots.get() <= 0) {
            throw new ConflictException("For the requested operation the conditions are not met.",
                    "The participant limit has been reached");
        }

        requestsByEventId.forEach(request -> {
            if (request.getStatus() != ParticipationRequestStatus.PENDING) {
                throw new ConflictException("For the requested operation the conditions are not met.",
                        "It's not allowed to change the status of the request");
            }
        });

        List<ParticipationRequestDto> confirmedDto = new ArrayList<>();
        List<ParticipationRequestDto> rejectedDto = new ArrayList<>();

        requestsByEventId.forEach(request -> {
            if (remainingSpots.get() > 0 && updateRequest.getStatus() == StatusUpdateRequest.CONFIRMED) {
                request.setStatus(ParticipationRequestStatus.CONFIRMED);
                confirmedDto.add(requestMapper.toDto(request));
                remainingSpots.getAndDecrement();
            } else {
                request.setStatus(ParticipationRequestStatus.REJECTED);
                rejectedDto.add(requestMapper.toDto(request));
            }
        });

        if (!confirmedDto.isEmpty()) {
            requestRepository.updateStatus(
                    confirmedDto.stream().map(ParticipationRequestDto::getId).toList(),
                    ParticipationRequestStatus.CONFIRMED);
        }
        if (!rejectedDto.isEmpty()) {
            requestRepository.updateStatus(
                    rejectedDto.stream().map(ParticipationRequestDto::getId).toList(),
                    ParticipationRequestStatus.REJECTED);
        }
        if (remainingSpots.get() == 0) {
            List<Long> pendingIds = requestRepository
                    .findAllByEventIdAndStatus(eventId, ParticipationRequestStatus.PENDING)
                    .stream().map(ParticipationRequest::getId).toList();
            if (!pendingIds.isEmpty()) {
                requestRepository.updateStatus(pendingIds, ParticipationRequestStatus.REJECTED);
            }
        }
        return new EventRequestStatusUpdateResult(confirmedDto, rejectedDto);
    }

    @Transactional(readOnly = true)
    public EventFullDto getPublicEventById(long eventId) {
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> {
                    log.info("{}: attempt to find event with id: {}", className, eventId);
                    return new NotFoundException("The required object was not found.",
                            "Event with id=" + eventId + " was not found");
                });
        List<Event> events = List.of(event);
        LocalDateTime startStats = event.getCreatedOn().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime endStats = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        EventStatistics stats = getEventStatistics(events, startStats, endStats);
        EventFullDto result = eventMapper.toFullDtoWithStats(event, stats);
        log.info("{}: result of getPublicEventById(): {}", className, result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getPublicEvents(PublicEventParam params) {
        if (params.getRangeStart() != null && params.getRangeEnd() != null
                && params.getRangeStart().isAfter(params.getRangeEnd())) {
            log.info("{}: getPublicEvents() call, where rangeStart:{} is not before rangeEnd: {}",
                    className, params.getRangeStart(), params.getRangeEnd());
            throw new BadRequestException("Start date must be before end date",
                    "Start: " + params.getRangeStart() + " End: " + params.getRangeEnd());
        }

        Pageable pageable = PageRequest.of(
                params.getFrom() / params.getSize(),
                params.getSize(),
                getSort(params.getSort())
        );

        // Get events from a repository
        Page<Event> page = eventRepository.findPublicEvents(
                params.getText(),
                params.getCategories(),
                params.getPaid(),
                params.getRangeStart(),
                params.getRangeEnd(),
                pageable);

        List<Event> events = page.getContent();

        if (events.isEmpty()) {
            return List.of();
        }

        LocalDateTime startStats = params.getRangeStart() != null ? params.getRangeStart().truncatedTo(ChronoUnit.SECONDS)
                : events.getFirst().getCreatedOn().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime endStats = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        EventStatistics stats = getEventStatistics(events, startStats, endStats);
        List<EventShortDto> result = events.stream()
                .map(event -> eventMapper.toShortDtoWithStats(event, stats))
                .toList();
        log.info("{}: result of getPublicEvents(): {}", className, result);
        return result;
    }

    @Override
    public Map<Long, Long> getEventViews(EventViewsParameters params) {
        List<ViewStats> stats = statsGetter.getEventViewStats(params);
        Map<Long, Long> views = new HashMap<>();
        if (stats != null) {
            for (ViewStats stat : stats) {
                Long eventId = extractId(stat.getUri());
                if (eventId != null) {
                    views.put(eventId, stat.getHits());
                }
            }
        }
        log.info("{}: result of getEventViews: {}", className, views);
        return views;
    }

    @Override
    public Map<Long, Integer> getConfirmedRequests(List<Long> eventIds) {
        List<EventRequestCount> confirmedRequests = requestRepository.countGroupByEventId(eventIds);
        Map<Long, Integer> result = confirmedRequests.stream().collect(
                Collectors.toMap(
                        EventRequestCount::eventId,
                        r -> r.count().intValue()
                )
        );
        log.info("{}: result of getConfirmedRequests: {}", className, result);
        return result;
    }

    private Event getEventIfInitiatedByUser(long userId, long eventId) {
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("The required object was not found.", "User with id=" + userId + " was not found"));
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("The required object was not found.", "Event with id=" + eventId + " was not found"));

        if (event.getInitiator().getId() != userId) {
            log.info("User {} cannot manipulate with the event with id {}", userId, eventId);
            throw new ConflictException("For the requested operation the conditions are not met.",
                    "Only initiator of event can can manipulate with it");
        }
        log.info("{}: result of getEventIfInitiatedByUser(): {}", className, event);
        return event;
    }

    private User findUserByIdOrElseThrow(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> {
            log.info("{}: user with id: {} was not found", className, userId);
            return new NotFoundException("The required object was not found.", "User with id=" + userId + " was not found");
        });
    }

    private Category findCategoryByIdOrElseThrow(long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() -> {
            log.info("{}: category with id: {} was not found", className, categoryId);
            return new NotFoundException("The required object was not found.", "Category with id=" + categoryId + " was not found");
        });
    }

    private Long extractId(String uri) {
        try {
            String[] parts = uri.split("/");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) {
            return null;
        }
    }

    private Sort getSort(EventPublicSort sort) {
        if (sort == null) return Sort.unsorted();
        return switch (sort) {
            case EVENT_DATE -> Sort.by("eventDate").ascending();
            case VIEWS -> Sort.by("views").descending();
        };
    }

    @Override
    public void validateExists(Long id) {
        if (eventRepository.findById(id).isEmpty()) {
            log.info("attempt to find event with id: {}", id);
            throw new NotFoundException("The required object was not found.",
                    "Event with id=" + id + " was not found");
        }
    }

    @Override
    public EventStatistics getEventStatistics(List<Event> events, LocalDateTime start, LocalDateTime end) {
        if (events.isEmpty()) {
            return new EventStatistics(Map.of(), Map.of());
        }

        List<Long> eventIds = events.stream().map(Event::getId).toList();
        EventViewsParameters params = EventViewsParameters.builder()
                .start(start)
                .end(end)
                .eventIds(eventIds).unique(true).build();
        Map<Long, Long> viewStats = getEventViews(params);
        Map<Long, Integer> confirmedRequests = getConfirmedRequests(eventIds);
        EventStatistics result = new EventStatistics(viewStats, confirmedRequests);
        log.info("{}: result of getEventStatistics(): {}", className, result);
        return result;
    }

}
