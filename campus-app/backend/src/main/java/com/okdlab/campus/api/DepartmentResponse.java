package com.okdlab.campus.api;

public record DepartmentResponse(
    Long id,
    String code,
    String name,
    String campus,
    String contactEmail) {
}
