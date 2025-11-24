package ru.yandex.practicum.explore.with.me.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.explore.with.me.model.user.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByIdIn(List<Long> ids);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email")
    boolean isExistsEmail(@Param("email") String email);
}