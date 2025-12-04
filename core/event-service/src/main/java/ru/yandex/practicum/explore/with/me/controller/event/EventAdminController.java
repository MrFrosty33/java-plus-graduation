package ru.yandex.practicum.explore.with.me.controller.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.explore.with.me.model.event.AdminEventFilter;
import ru.yandex.practicum.explore.with.me.model.event.dto.AdminEventSearchRequestDto;
import ru.yandex.practicum.explore.with.me.model.event.dto.UpdateEventAdminRequestDto;
import ru.yandex.practicum.explore.with.me.service.event.EventAdminService;
import ru.yandex.practicum.interaction.api.model.event.dto.EventFullDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
@Validated
@Slf4j
public class EventAdminController {

    private final String className = this.getClass().getSimpleName();
    private final EventAdminService service;

    // GET /admin/events
    @GetMapping
    public List<EventFullDto> searchEvents(@ModelAttribute AdminEventSearchRequestDto req) {
        log.trace("{}: searchEvents() call with dto: {}", className, req);
        Pageable page = PageRequest.of(req.getFrom() / req.getSize(), req.getSize());
        AdminEventFilter f = new AdminEventFilter(
                req.getUsers(), req.getStates(), req.getCategories(),
                req.getRangeStart(), req.getRangeEnd());
        return service.search(f, page);
    }

    // PATCH /admin/events/{id}
    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable @PositiveOrZero @NotNull Long eventId,
                                    @RequestBody @Valid UpdateEventAdminRequestDto dto) {
        log.trace("{}: updateEvent() call with eventId: {}, dto: {}", className, eventId, dto);
        return service.update(eventId, dto);
    }
}
