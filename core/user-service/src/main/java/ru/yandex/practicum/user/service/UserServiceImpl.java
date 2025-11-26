package ru.yandex.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interaction.api.exception.ConflictException;
import ru.yandex.practicum.interaction.api.mapper.UserMapper;
import ru.yandex.practicum.interaction.api.model.user.AdminUserFindParam;
import ru.yandex.practicum.interaction.api.model.user.NewUserRequest;
import ru.yandex.practicum.interaction.api.model.user.User;
import ru.yandex.practicum.interaction.api.model.user.UserDto;
import ru.yandex.practicum.interaction.api.model.user.UserShortDto;
import ru.yandex.practicum.interaction.api.util.DataProvider;
import ru.yandex.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImpl implements UserService, DataProvider<UserShortDto, User> {
    private final String className = this.getClass().getSimpleName();
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> find(AdminUserFindParam param) {
        List<UserDto> result;

        if (param.getIds() != null && !param.getIds().isEmpty()) {
            result = userRepository.findByIdIn(param.getIds()).stream()
                    .map(this::mapUserDto)
                    .toList();
        } else {
            PageRequest pageRequest = PageRequest.of(param.getFrom(), param.getSize());
            result = userRepository.findAll(pageRequest).get()
                    .map(this::mapUserDto)
                    .toList();
        }

        log.info("{}: result of find(): {}", className, result);
        return result;
    }

    @Override
    public Optional<UserDto> findById(Long id) {
        Optional<User> user = userRepository.findById(id);
        Optional<UserDto> result = Optional.empty();

        if (user.isPresent()) {
            result = Optional.of(userMapper.toDto(user.get()));
        }

        log.info("{}: result of findById: {}", className, result);
        return result;
    }

    @Transactional
    @Override
    public UserDto create(NewUserRequest newUserRequest) {
        validateEmailUnique(newUserRequest.getEmail());
        UserDto result = mapUserDto(userRepository.save(mapEntity(newUserRequest)));
        log.info("{}: result of create():: {}", className, result);
        return result;
    }

    @Transactional
    @Override
    public void delete(Long userId) {
        userRepository.deleteById(userId);
        log.info("{}: user with id: {} has been deleted ", className, userId);
    }

    private User mapEntity(NewUserRequest newUserRequest) {
        return userMapper.toEntity(newUserRequest);
    }

    private UserDto mapUserDto(User user) {
        return userMapper.toDto(user);
    }

    @Override
    public UserShortDto getDto(User entity) {
        return userMapper.toShortDto(entity);
    }

    private void validateEmailUnique(String email) {
        if (userRepository.isExistsEmail(email)) {
            log.info("{}: user with email {} already exists", className, email);
            throw new ConflictException("The email of user should be unique.",
                    "User with email=" + email + " is already exist");
        }
    }
}
