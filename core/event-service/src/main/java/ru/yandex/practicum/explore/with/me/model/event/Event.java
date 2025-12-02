package ru.yandex.practicum.explore.with.me.model.event;


import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.explore.with.me.model.category.Category;
import ru.yandex.practicum.interaction.api.model.event.EventState;
import ru.yandex.practicum.interaction.api.model.event.Location;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "annotation", length = 2000)
    private String annotation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "description", length = 7000)
    private String description;

    @Column(name = "event_date")
    private LocalDateTime eventDate;
    @Column(name = "created")
    private LocalDateTime createdOn;
    @Column(name = "published")
    private LocalDateTime publishedOn;

    @Embedded
    private Location location;

    @Column(name = "paid")
    private boolean paid;
    @Column(name = "participant_limit")
    private int participantLimit;
    @Column(name = "request_moderation")
    private boolean requestModeration;
    @Column(name = "title", length = 120)
    private String title;

    @Column(name = "initiator_id")
    private Long initiatorId;

    @Convert(converter = EventStateConverter.class)
    private EventState state;
}
