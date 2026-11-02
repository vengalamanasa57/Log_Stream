package com.logstream.alerting;

import java.time.Instant;

public record AlertEvent(
        String ruleId,
        String ruleName,
        int observedCount,
        int threshold,
        Instant triggeredAt
) {}
