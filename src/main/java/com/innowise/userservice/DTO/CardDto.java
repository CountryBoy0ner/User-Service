package com.innowise.userservice.DTO;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;


import java.time.LocalDate;
@Data
public class CardDto {
    private Long id;

    @NotBlank(message = "Card number is required", groups = ValidationGroups.OnCreate.class)
    @Size(min = 12, max = 19, message = "Card number length must be 12..19", groups = ValidationGroups.OnCreate.class)
    private String number;

    @NotBlank(message = "Card holder is required",
            groups = {ValidationGroups.OnCreate.class, ValidationGroups.OnUpdateBasicInfo.class})
    private String holder;

    @Future(message = "Expiration date must be in the future",
            groups = {ValidationGroups.OnCreate.class, ValidationGroups.OnUpdateBasicInfo.class})
    private LocalDate expirationDate;

    @NotNull(message = "userId is required", groups = ValidationGroups.OnCreate.class)
    private Long userId;
}
