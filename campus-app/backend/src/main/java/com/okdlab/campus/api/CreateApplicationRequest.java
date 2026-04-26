package com.okdlab.campus.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateApplicationRequest(
    @NotBlank @Size(max = 120) String studentName,
    @NotBlank @Email @Size(max = 160) String studentEmail,
    @NotBlank @Size(max = 120) String track,
    @NotBlank @Size(max = 500) String notes,
    @NotNull Long offerId) {
}
