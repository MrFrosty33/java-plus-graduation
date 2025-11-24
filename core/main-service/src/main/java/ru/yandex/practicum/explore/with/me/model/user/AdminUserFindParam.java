package ru.yandex.practicum.explore.with.me.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class AdminUserFindParam {
    private List<Long> ids;
    private int from;
    private int size;
}