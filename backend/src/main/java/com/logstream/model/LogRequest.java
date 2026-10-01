package com.logstream.model;

import jakarta.validation.constraints.NotBlank;

public record LogRequest(
        String id,
        @NotBlank String timestamp,
        @NotBlank String level,
        @NotBlank String service,
        @NotBlank String message,
        long responseTimeMs,
        String host,
        String traceId
) {}
