package com.transactionapi.transactionapi.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record LoginRecordDto(@NotNull @NotEmpty @Email String email,
                             @NotNull @NotEmpty String password) {
    
}
