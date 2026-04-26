package com.okdlab.campus.api;

import com.okdlab.campus.domain.ApplicationStatus;
import java.time.OffsetDateTime;

public record ApplicationResponse(
    Long id,
    String studentName,
    String studentEmail,
    String track,
    String notes,
    ApplicationStatus status,
    Long offerId,
    String offerTitle,
    OffsetDateTime createdAt) {
}
