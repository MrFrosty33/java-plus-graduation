package ru.yandex.practicum.explore.with.me.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.explore.with.me.mapper.EventMapper;
import ru.yandex.practicum.explore.with.me.model.event.AdminEventFilter;
import ru.yandex.practicum.explore.with.me.model.event.Event;
import ru.yandex.practicum.explore.with.me.model.event.EventState;
import ru.yandex.practicum.explore.with.me.model.event.EventStatistics;
import ru.yandex.practicum.explore.with.me.model.event.dto.UpdateEventAdminRequestDto;
import ru.yandex.practicum.explore.with.me.repository.CategoryRepository;
import ru.yandex.practicum.explore.with.me.repository.EventRepository;
import ru.yandex.practicum.interaction.api.exception.ConflictException;
import ru.yandex.practicum.interaction.api.exception.NotFoundException;
import ru.yandex.practicum.interaction.api.model.event.dto.EventFullDto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventAdminService {

    private final String className = this.getClass().getSimpleName();

    private final EventRepository eventRepo;
    private final CategoryRepository categoryRepo;
    private final EventMapper mapper;
    private final EventService eventService;

    // GET /admin/events
    @Transactional(readOnly = true)
    public List<EventFullDto> search(AdminEventFilter f, Pageable page) {

        List<EventState> states = null;
        if (f.getStates() != null && !f.getStates().isEmpty()) {
            states = f.getStates().stream()
                    .map(String::toUpperCase)
                    .map(EventState::valueOf)
                    .toList();
        }

        Page<Event> events = eventRepo.searchForAdmin(
                emptyToNull(f.getUsers()),
                states,
                emptyToNull(f.getCategories()),
                f.getRangeStart(),
                f.getRangeEnd(),
                page
        );

        List<Event> eventList = events.getContent();
        if (eventList.isEmpty()) {
            return List.of();
        }

        LocalDateTime startStats = eventList.getFirst().getCreatedOn().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime endStats = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        EventStatistics stats = eventService.getEventStatistics(eventList, startStats, endStats);
        List<EventFullDto> result = events.stream()
                .map(event -> mapper.toFullDtoWithStats(event, stats))
                .toList();
        log.info("{}: result of search(): {}", className, result);
        return result;
    }

    // PATCH /admin/events/{id}
    @Transactional
    public EventFullDto update(Long id, UpdateEventAdminRequestDto dto) {
        Event event = eventRepo.findById(id)
                .orElseThrow(() -> {
                    log.info("{}: event with id: {} was not found", className, id);
                    return new NotFoundException("The required object was not found.",
                            "Event with id=" + id + " was not found");
                });

        mapper.updateFromAdmin(dto, event);

        if (dto.getCategory() != null) {
            event.setCategory(categoryRepo.findById(dto.getCategory())
                    .orElseThrow(() -> {
                        log.info("{}: category with id: {} was not found", className, dto.getCategory());
                        return new NotFoundException("The required object was not found.",
                                "Category with id=" + dto.getCategory() + " was not found");
                    }));
        }

        if (dto.getStateAction() != null) {
            switch (dto.getStateAction()) {
                case PUBLISH_EVENT -> publish(event);
                case REJECT_EVENT -> reject(event);
            }
        }
        EventFullDto result = mapper.toFullDto(eventRepo.save(event));
        log.info("{}: result of update(): {}", className, result);
        return result;
    }

    private <T> List<T> emptyToNull(List<T> list) {
        return (list == null || list.isEmpty()) ? null : list;
    }

    private void publish(Event e) {
        if (e.getState() != EventState.PENDING) {
            log.info("{}: attempt to publish event, but eventState: {} is not PENDING", className, e.getState());
            throw new ConflictException("For the requested operation the conditions are not met.",
                    "Cannot publish the event because it's not in the right state: " + e.getState());
        }
        if (e.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            log.info("{}: attempt to publish event, but eventDate: {} is not at least 1 hour from publicationTime: {}",
                    className, e.getEventDate(), LocalDateTime.now().plusHours(1));
            throw new ConflictException("For the requested operation the conditions are not met.",
                    "Event start time must be at least 1 hour from publication time");
        }
        e.setState(EventState.PUBLISHED);
        e.setPublishedOn(LocalDateTime.now());
        log.info("{}: event with id: {} was published", className, e.getId());
    }

    private void reject(Event e) {
        if (e.getState() == EventState.PUBLISHED) {
            log.info("{}: attempt to reject alreadz published event with id: {}", className, e.getId());
            throw new ConflictException("For the requested operation the conditions are not met.",
                    "Cannot reject the event because it's already published");
        }
        e.setState(EventState.CANCELED);
        log.info("{}: event with id: {} was rejected", className, e.getId());
    }
}