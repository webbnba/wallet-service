package ru.bezborodov.walletservice.controller.payload;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import ru.bezborodov.walletservice.entity.OperationType;

import java.math.BigDecimal;
import java.util.UUID;

public record RequestPayload(
        @NotNull(message = "Id must not be null")
        UUID id,
        @NotNull(message = "Operation type must not be null")
        OperationType type,
        @NotNull(message = "Amount must not be null")
        @DecimalMin(value = "0.0", message = "Amount must be greater than or equal to 0.0")
        BigDecimal amount) {
}
