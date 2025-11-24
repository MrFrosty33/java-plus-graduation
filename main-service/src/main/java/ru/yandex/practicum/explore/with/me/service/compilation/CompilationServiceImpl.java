package ru.yandex.practicum.explore.with.me.service.compilation;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.explore.with.me.exception.NotFoundException;
import ru.yandex.practicum.explore.with.me.mapper.CompilationMapper;
import ru.yandex.practicum.explore.with.me.model.compilation.Compilation;
import ru.yandex.practicum.explore.with.me.model.compilation.CompilationCreateDto;
import ru.yandex.practicum.explore.with.me.model.compilation.CompilationRequestDto;
import ru.yandex.practicum.explore.with.me.model.compilation.CompilationUpdateDto;
import ru.yandex.practicum.explore.with.me.model.event.Event;
import ru.yandex.practicum.explore.with.me.repository.CompilationRepository;
import ru.yandex.practicum.explore.with.me.repository.EventRepository;
import ru.yandex.practicum.explore.with.me.util.ExistenceValidator;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService, ExistenceValidator<Compilation> {

    private final String className = this.getClass().getSimpleName();
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;


    @Override
    @Transactional
    public CompilationRequestDto create(CompilationCreateDto compilationCreateDto) {
        if (compilationCreateDto.getPinned() == null) {
            compilationCreateDto.setPinned(false);
        }

        List<Long> eventIds = compilationCreateDto.getEvents();
        if (eventIds == null) {
            eventIds = Collections.emptyList();
        } else {
            eventIds = eventIds.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        List<Event> events = eventRepository.findAllById(eventIds);

        Compilation compilation = compilationMapper.toEntity(compilationCreateDto, events);

        CompilationRequestDto result = compilationMapper.toRequestDto(compilationRepository.save(compilation));
        log.info("{}: result of create(): {}", className, result);
        return result;
    }

    @Override
    @Transactional
    public CompilationRequestDto update(CompilationUpdateDto compilationUpdateDto, Long compId) {
        Compilation compilation = findByIdOrElseThrow(compId);

        if (compilationUpdateDto.getTitle() != null) {
            compilation.setTitle(compilationUpdateDto.getTitle());
        }

        if (compilationUpdateDto.getPinned() != null) {
            compilation.setPinned(compilationUpdateDto.getPinned());
        }

        if (compilationUpdateDto.getEvents() != null) {
            List<Event> events = eventRepository.findAllById(compilationUpdateDto.getEvents());

            compilation.setEvents(events);
        }

        CompilationRequestDto result = compilationMapper.toRequestDto(
                compilationRepository.save(compilation)
        );
        log.info("{}: result of update(): {}", className, result);
        return result;
    }

    @Override
    @Transactional
    public void delete(Long compId) {
        validateExists(compId);
        compilationRepository.deleteById(compId);
        log.info("{}: compilation with id: {} was deleted", className, compId);
    }

    @Override
    public List<CompilationRequestDto> get(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        Page<Compilation> page;

        if (pinned == null) {
            page = compilationRepository.findAll(pageable);
        } else {
            page = compilationRepository.findAllByPinned(pinned, pageable);
        }

        List<CompilationRequestDto> result = page.stream()
                .map(compilationMapper::toRequestDto)
                .collect(Collectors.toList());
        log.info("{}: result of get(): {}", className, result);
        return result;
    }

    @Override
    public CompilationRequestDto getById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("The required object was not found.", "Compilation with id=" + compId + " was not found"));

        CompilationRequestDto result = compilationMapper.toRequestDto(compilation);
        log.info("{}: result of getById(): {}", className, result);
        return result;
    }

    private Compilation findByIdOrElseThrow(long compilationId) {
        return compilationRepository.findById(compilationId)
                .orElseThrow(() -> {
                    log.info("{}: attempt to find compilation with id:{}", className, compilationId);
                    return new NotFoundException("The required object was not found.",
                            "Compilation with id=" + compilationId + " was not found");
                });
    }

    @Override
    public void validateExists(Long id) {
        if (compilationRepository.findById(id).isEmpty()) {
            log.info("{}: attempt to find user with id: {}", className, id);
            throw new NotFoundException("The required object was not found.",
                    "Compilation with id=" + id + " was not found");
        }
    }
}
