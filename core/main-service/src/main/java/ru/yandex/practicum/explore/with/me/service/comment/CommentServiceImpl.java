package ru.yandex.practicum.explore.with.me.service.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.explore.with.me.mapper.CommentMapper;
import ru.yandex.practicum.explore.with.me.model.comment.Comment;
import ru.yandex.practicum.explore.with.me.model.comment.CommentDto;
import ru.yandex.practicum.explore.with.me.model.comment.CommentUpdateDto;
import ru.yandex.practicum.explore.with.me.model.comment.CommentUserDto;
import ru.yandex.practicum.explore.with.me.model.comment.CreateUpdateCommentDto;
import ru.yandex.practicum.explore.with.me.model.event.Event;
import ru.yandex.practicum.explore.with.me.model.participation.ParticipationRequestStatus;
import ru.yandex.practicum.explore.with.me.model.user.User;
import ru.yandex.practicum.explore.with.me.repository.CommentRepository;
import ru.yandex.practicum.explore.with.me.repository.EventRepository;
import ru.yandex.practicum.explore.with.me.repository.ParticipationRequestRepository;
import ru.yandex.practicum.explore.with.me.repository.UserRepository;
import ru.yandex.practicum.interaction.api.exception.BadRequestException;
import ru.yandex.practicum.interaction.api.exception.ConflictException;
import ru.yandex.practicum.interaction.api.exception.ForbiddenException;
import ru.yandex.practicum.interaction.api.exception.NotFoundException;
import ru.yandex.practicum.interaction.api.util.ExistenceValidator;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
@Slf4j
public class CommentServiceImpl implements CommentService, ExistenceValidator<Comment> {

    private static final String OBJECT_NOT_FOUND = "Required object was not found.";
    private static final String CONDITIONS_NOT_MET = "Conditions are not met.";
    private final String className = this.getClass().getSimpleName();

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestRepository requestRepository;
    private final ExistenceValidator<User> userExistenceValidator;
    private final ExistenceValidator<Event> eventExistenceValidator;
    private final CommentMapper mapper;


    // admin

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentDto getCommentById(Long id) {
        CommentDto result = mapper.toDto(getOrThrow(id));
        log.info("{}: result of getCommentById({}): {}", className, id, result);
        return result;
    }

    @Override
    public void deleteCommentByAdmin(Long id) {
        log.info("{}: comment with id: {} was deleted, if it existed", className, id);
        commentRepository.deleteById(id);
    }


    //private

    @Override
    public CommentDto createComment(Long userId, Long eventId, CreateUpdateCommentDto dto) {
        validateText(dto.getText(), 100);

        User author = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.info("{}: attempt to find user with id: {}", className, userId);
                            return new NotFoundException(
                                    OBJECT_NOT_FOUND,
                                    String.format("User with id: %d was not found", userId));
                        }
                );

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.info("{}: attempt to find event with id: {}", className, eventId);
                            return new NotFoundException(
                                    OBJECT_NOT_FOUND,
                                    String.format("Event with id: %d was not found", eventId));
                        }
                );

        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            log.info("CommentServiceImpl: attempt to comment event, which has not been happened yet");
            throw new ConflictException(CONDITIONS_NOT_MET, "Only past events can be commented on");
        }

        if (!requestRepository
                .existsByRequesterIdAndEventIdAndStatus(
                        userId,
                        eventId,
                        ParticipationRequestStatus.CONFIRMED
                )) {
            log.info("{}: attempt to comment on event with id: {}, " +
                    "in which user with id: {} did not participate", className, event, userId);
            throw new ConflictException(CONDITIONS_NOT_MET, "Only events the user participated in can be commented on");
        }

        Comment comment = mapper.toModel(dto);
        comment.setAuthor(author);
        comment.setEvent(event);

        CommentDto result = mapper.toDto(commentRepository.save(comment));
        log.info("{}: result of createComment(): {}", className, result);
        return result;
    }

    @Override
    public CommentUpdateDto updateComment(Long userId, Long commentId, CreateUpdateCommentDto dto) {
        userExistenceValidator.validateExists(userId);
        validateText(dto.getText(), 1000);

        Comment comment = getOrThrow(commentId);
        if (!comment.getAuthor().getId().equals(userId)) {
            log.info("{}: attempt to redact comment with id: {} by a user with id: {}, " +
                    "which is not an author", className, commentId, userId);
            throw new ForbiddenException(CONDITIONS_NOT_MET,
                    "Only author can redact comment");
        }

        comment.setText(dto.getText());
        comment.setUpdatedOn(LocalDateTime.now());
        CommentUpdateDto result = mapper.toUpdateDto(comment);
        log.info("{}: result of updateComment(): {}", className, result);
        return result;
    }

    @Override
    public void deleteCommentByAuthor(Long userId, Long commentId) {
        userExistenceValidator.validateExists(userId);
        Comment comment = getOrThrow(commentId);

        if (!comment.getAuthor().getId().equals(userId)) {
            log.info("{}: attempt to delete comment, but user with id: {} " +
                    "is not an author", className, userId);
            throw new ForbiddenException(CONDITIONS_NOT_MET,
                    "Only author / admin can delete comment");
        }

        commentRepository.delete(comment);
        log.info("{}: comment with id: {} was deleted", className, commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentUserDto> getCommentsByAuthor(Long userId, Pageable pageable) {
        userExistenceValidator.validateExists(userId);
        List<CommentUserDto> result = commentRepository.findByAuthorIdOrderByCreatedOnDesc(userId, pageable)
                .stream()
                .map(mapper::toUserDto)
                .toList();
        log.info("{}: result of getCommentsByAuthor(): {}", className, result);
        return result;
    }


    //public

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByEvent(Long eventId, Pageable pageable) {
        eventExistenceValidator.validateExists(eventId);
        List<CommentDto> result = commentRepository.findByEventIdOrderByCreatedOnDesc(eventId, pageable)
                .stream()
                .map(mapper::toDto)
                .toList();
        log.info("{}: result of getCommentsByEvent(): {}", className, result);
        return result;
    }


    private void validateText(String text, int max) {
        if (text == null || text.isBlank() || text.length() > max) {
            throw new BadRequestException("Text param constraint violation.",
                    String.format("Comment text has to be 1–%d symbols", max));
        }
    }

    private Comment getOrThrow(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> {
                    log.info("{}: comment with id: {} was not found", className, id);
                            return new NotFoundException(
                                    OBJECT_NOT_FOUND,
                                    String.format("Comment with id: %d was not found", id));
                        }
                );
    }

    @Override
    public void validateExists(Long id) {
        if (commentRepository.findById(id).isEmpty()) {
            log.info("{}: attempt to find comment with id: {}", className, id);
            throw new NotFoundException(OBJECT_NOT_FOUND,
                    "Comment with id=" + id + " was not found");
        }
    }
}
