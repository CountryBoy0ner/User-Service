package com.innowise.userservice.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data

public class UserDto {

    private Long id;

    @Email(message = "Email is invalid")
    private String name;


    @NotBlank(message = "Surname is required")
    private String surname;

    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @Email(message = "Email is invalid")
    @NotBlank(message = "Email is required")
    private String email;

    @Valid
    private List<CardDto> cards;
}