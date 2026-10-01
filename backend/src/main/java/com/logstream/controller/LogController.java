package com.logstream.controller;

import com.logstream.model.LogRecord;
import com.logstream.model.LogRequest;
import com.logstream.model.SearchResponse;
import com.logstream.search.LuceneLogService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "http://localhost:5173")
public class LogController {

    private final LuceneLogService logService;

    public LogController(LuceneLogService logService) {
        this.logService = logService;
    }

    @PostMapping
    public ResponseEntity<String> indexLog(@Valid @RequestBody LogRequest request) throws Exception {
        String id = request.id() == null || request.id().isBlank()
                ? UUID.randomUUID().toString()
                : request.id();

        logService.index(new LogRecord(
                id,
                request.timestamp(),
                request.level().toUpperCase(),
                request.service(),
                request.message(),
                request.responseTimeMs(),
                request.host(),
                request.traceId()
        ));

        return ResponseEntity.ok(id);
    }

    @GetMapping("/search")
    public SearchResponse search(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String service,
            @RequestParam(required = false) Long minResponseTime,
            @RequestParam(required = false) Long maxResponseTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) throws Exception {

        return logService.search(
                q, level, service,
                minResponseTime, maxResponseTime,
                page, pageSize
        );
    }
}
