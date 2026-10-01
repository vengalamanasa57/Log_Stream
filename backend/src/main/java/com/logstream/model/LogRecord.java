package com.logstream.model;

public record LogRecord(
        String id,
        String timestamp,
        String level,
        String service,
        String message,
        long responseTimeMs,
        String host,
        String traceId
) {}
