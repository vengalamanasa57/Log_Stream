package com.logstream.model;

import java.util.List;

public record SearchResponse(
        List<LogRecord> results,
        int totalHits,
        int page,
        int pageSize
) {}
