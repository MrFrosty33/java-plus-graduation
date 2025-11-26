package ru.yandex.practicum.interaction.api.model.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewUserRequest {
    @Size(min = 6, max = 254, message = "email size must be between 6 and 254")
    @NotBlank(message = "must not be blank")
    @Email(message = "must be a valid email")
    private String email;
    @Size(min = 2, max = 250, message = "name size must be between 2 and 250")
    @NotBlank(message = "must not be blank")
    private String name;
}