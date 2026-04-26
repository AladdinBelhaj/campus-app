package com.okdlab.campus.api;

import com.okdlab.campus.domain.OfferStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOfferStatusRequest(@NotNull OfferStatus status) {
}
