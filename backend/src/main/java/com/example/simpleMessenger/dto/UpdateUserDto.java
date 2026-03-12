package com.example.simpleMessenger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserDto {
    @NotBlank(message = "Повне ім'я є обов'язковим")
    @Size(min = 2, max = 50, message = "Повне ім'я має бути від 2 до 50 символів")
    @Pattern(
            regexp = "^[A-Za-z]+(?:[ '-][A-Za-z]+)*$",
            message = "Повне ім'я має містити тільки англійські літери"
    )
    private String fullName;
    private String avatarUrl;

    @NotBlank(message = "Ім'я користувача є обов'язковим")
    @Size(min = 3, max = 20, message = "Ім'я користувача має бути від 3 до 20 символів")
    @Pattern(
            regexp = "^[A-Za-z]+$",
            message = "Ім'я користувача має містити тільки англійські літери"
    )
    private String username;
}
