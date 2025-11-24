package ru.yandex.practicum.explore.with.me.controller.compilation;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.explore.with.me.model.compilation.CompilationCreateDto;
import ru.yandex.practicum.explore.with.me.model.compilation.CompilationRequestDto;
import ru.yandex.practicum.explore.with.me.model.compilation.CompilationUpdateDto;
import ru.yandex.practicum.explore.with.me.service.compilation.CompilationService;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/admin/compilations")
@Slf4j
public class CompilationAdminController {

    private final String className = this.getClass().getSimpleName();
    private final CompilationService compilationService;

    @PostMapping
    public ResponseEntity<CompilationRequestDto> create(@RequestBody @Valid CompilationCreateDto compilationCreateDto) {
        log.trace("{}: create() call with compilationCreateDto: {}", className, compilationCreateDto);
        CompilationRequestDto compilationRequestDto = compilationService.create(compilationCreateDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(compilationRequestDto);
    }

    @PatchMapping("/{compId}")
    public ResponseEntity<CompilationRequestDto> update(@RequestBody @Valid CompilationUpdateDto compilationUpdateDto,
                                                        @PathVariable Long compId) {
        log.trace("{}: update() call with compilationUpdateDto: {}, compilationId: {}", className, compilationUpdateDto, compId);
        CompilationRequestDto compilationRequestDto = compilationService.update(compilationUpdateDto, compId);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(compilationRequestDto);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long compId) {
        log.trace("{}: delete() call with compilationId: {}", className, compId);
        compilationService.delete(compId);
    }
}
