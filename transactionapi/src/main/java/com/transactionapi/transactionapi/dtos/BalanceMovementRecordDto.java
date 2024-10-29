package com.transactionapi.transactionapi.dtos;

import jakarta.validation.constraints.NotNull;

public record BalanceMovementRecordDto(@NotNull double value,
                                       String email) {
    
}
