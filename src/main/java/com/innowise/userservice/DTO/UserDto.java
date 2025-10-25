package com.innowise.userservice.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UserDto {

    private Long id;

    @NotBlank(message = "Name is required", groups = ValidationGroups.OnCreate.class)
    @Size(max = 100, message = "Name max length is 100",
            groups = {ValidationGroups.OnCreate.class, ValidationGroups.OnPatch.class})
    private String name;

    @NotBlank(message = "Surname is required", groups = ValidationGroups.OnCreate.class)
    @Size(max = 100, message = "Surname max length is 100",
            groups = {ValidationGroups.OnCreate.class, ValidationGroups.OnPatch.class})
    private String surname;

    @Past(message = "Birth date must be in the past",
            groups = {ValidationGroups.OnCreate.class, ValidationGroups.OnPatch.class})

    private LocalDate birthDate;

    @Email(message = "Email is invalid",
            groups = {ValidationGroups.OnCreate.class, ValidationGroups.OnPatch.class})
    @NotBlank(message = "Email is required", groups = ValidationGroups.OnCreate.class)
    private String email;

    @Valid
    private List<CardDto> cards;
}
