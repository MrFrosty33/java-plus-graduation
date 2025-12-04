package ru.yandex.practicum.interaction.api.model.comment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "text", length = 1000)
    private String text;

    @Column(name = "created")
    private LocalDateTime createdOn;

    @Column(name = "updated")
    private LocalDateTime updatedOn;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "author_id")
    private Long authorId;
}
