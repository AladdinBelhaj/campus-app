package com.okdlab.campus.api;

import com.okdlab.campus.domain.OfferStatus;
import java.time.OffsetDateTime;

public record InternshipOfferResponse(
    Long id,
    String title,
    String domain,
    String summary,
    int seats,
    int filledSeats,
    String location,
    OfferStatus status,
    String department,
    OffsetDateTime updatedAt) {
}
