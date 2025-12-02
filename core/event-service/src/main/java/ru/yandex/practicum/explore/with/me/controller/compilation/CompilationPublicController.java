package ru.yandex.practicum.explore.with.me.controller.compilation;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.explore.with.me.model.compilation.CompilationRequestDto;
import ru.yandex.practicum.explore.with.me.service.compilation.CompilationService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/compilations")
@Slf4j
public class CompilationPublicController {
    private final String className = this.getClass().getSimpleName();
    private final CompilationService compilationService;

    @GetMapping
    public ResponseEntity<List<CompilationRequestDto>> get(
            @RequestParam(defaultValue = "false") Boolean pinned,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        log.trace("{}: get() call with pinned: {}, from: {}, size: {}", className, pinned, from, size);
        List<CompilationRequestDto> compilations = compilationService.get(pinned, from, size);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(compilations);
    }

    @GetMapping("/{compId}")
    public ResponseEntity<CompilationRequestDto> getById(@PathVariable Long compId) {
        log.trace("{}: getById() call with compilationId {}", className, compId);
        CompilationRequestDto compilationRequestDto = compilationService.getById(compId);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(compilationRequestDto);
    }
}
