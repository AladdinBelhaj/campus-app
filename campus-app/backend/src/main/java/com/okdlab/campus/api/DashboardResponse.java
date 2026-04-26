package com.okdlab.campus.api;

public record DashboardResponse(
    long departments,
    long publishedOffers,
    long applications,
    long pendingApplications,
    long acceptedApplications) {
}
