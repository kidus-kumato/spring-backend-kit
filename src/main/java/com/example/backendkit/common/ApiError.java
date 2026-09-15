package com.example.backendkit.common;

import java.time.Instant;
import java.util.List;

public record ApiError(Instant timestamp, int status, String code, String message, String path,
                       String traceId, List<FieldError> errors) {
    public record FieldError(String field, String message) {}
}
