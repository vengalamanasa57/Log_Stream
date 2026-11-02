package com.logstream.alerting;

public record AlertRule(
        String id,
        String name,
        String level,
        String service,
        int threshold,
        boolean enabled
) {}
