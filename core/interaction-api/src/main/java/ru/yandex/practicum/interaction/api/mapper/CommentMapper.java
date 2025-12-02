package ru.yandex.practicum.interaction.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.interaction.api.model.comment.Comment;
import ru.yandex.practicum.interaction.api.model.comment.dto.CommentDto;
import ru.yandex.practicum.interaction.api.model.comment.dto.CommentUpdateDto;
import ru.yandex.practicum.interaction.api.model.comment.dto.CommentUserDto;
import ru.yandex.practicum.interaction.api.model.comment.dto.CreateUpdateCommentDto;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CommentMapper {
    @Mapping(target = "eventDto", ignore = true)
    @Mapping(target = "authorDto", ignore = true)
    CommentDto toDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedOn", ignore = true)
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    Comment toModel(CreateUpdateCommentDto createUpdateCommentDto);

    @Mapping(target = "eventDto", ignore = true)
    CommentUserDto toUserDto(Comment comment);

    @Mapping(target = "eventDto", ignore = true)
    @Mapping(target = "authorDto", ignore = true)
    CommentUpdateDto toUpdateDto(Comment comment);
}
