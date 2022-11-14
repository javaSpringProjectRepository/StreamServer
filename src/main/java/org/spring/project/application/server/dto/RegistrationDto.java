package org.spring.project.application.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
public class RegistrationDto {

    @NotBlank(message = "Имя пользователя не должно быть пустым")
    @Size(min = 5, message = "Имя пользователя должно содержать более четырех символов")
    @Size(max = 20, message = "Имя пользователя должно содержать менее двадцати символов")
    @NonNull
    private String username;
    @NotBlank(message = "Пароль пользователя не должен быть пустым")
    @Size(min = 5, message = "Пароль пользователя должен содержать более четырех символов")
    @Size(max = 20, message = "Пароль пользователя должен содержать менее двадцати символов")
    @NonNull
    private String password;
    @Email(message = "Электронный адрес введен некорректно")
    @NonNull
    private String email;
}