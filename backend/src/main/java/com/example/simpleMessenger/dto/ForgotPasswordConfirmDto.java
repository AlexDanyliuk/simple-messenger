package com.example.simpleMessenger.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ForgotPasswordConfirmDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$",
            message = "Некоректний формат пошти. Використовуйте gmail.com адресу"
    )
    private String email;

    @NotBlank(message = "Reset code is required")
    @Pattern(regexp = "^\\d{6}$", message = "Reset code must contain 6 digits")
    private String code;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)\\S+$",
            message = "Password must contain uppercase, lowercase, digit and no spaces"
    )
    private String newPassword;
}
