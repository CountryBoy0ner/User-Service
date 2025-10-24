package com.innowise.userservice.DTO;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.lang.NonNull;

import java.time.LocalDate;
@Data
public class CardDto {

    private Long id;

    @NotBlank(message = "Card number is required")
    @Size(min = 12, max = 19, message = "Card number length must be 12..19")//todo
    private String number;

    @NotBlank(message = "Card holder is required")
    private String holder;

    @Future(message = "Expiration date must be in the future")
    private LocalDate expirationDate;

    @NotNull(message = "userId is required")
    private Long userId;
}
