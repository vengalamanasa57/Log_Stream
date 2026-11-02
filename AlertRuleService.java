package com.logstream.alerting;

import com.logstream.search.LuceneLogService;
import com.logstream.model.SearchResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class AlertRuleService {
    private final LuceneLogService logService;
    private final ConcurrentHashMap<String, AlertRule> rules = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<AlertEvent> events = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<String, Integer> lastTriggeredCounts = new ConcurrentHashMap<>();

    public AlertRuleService(LuceneLogService logService) {
        this.logService = logService;
    }

    public AlertRule create(AlertRule input) {
        if (input.name() == null || input.name().isBlank()) throw new IllegalArgumentException("Rule name is required");
        if (input.level() == null || input.level().isBlank()) throw new IllegalArgumentException("Log level is required");
        if (input.threshold() < 1) throw new IllegalArgumentException("Threshold must be greater than zero");
        String id = input.id() == null || input.id().isBlank() ? UUID.randomUUID().toString() : input.id();
        AlertRule saved = new AlertRule(id, input.name(), input.level().toUpperCase(), input.service(), input.threshold(), input.enabled());
        rules.put(id, saved);
        return saved;
    }

    public List<AlertRule> getRules() { return List.copyOf(rules.values()); }
    public List<AlertEvent> getEvents() { return List.copyOf(events); }

    public boolean delete(String id) {
        lastTriggeredCounts.remove(id);
        return rules.remove(id) != null;
    }

    @Scheduled(fixedRate = 60000)
    public void evaluateRules() {
        for (AlertRule rule : rules.values()) {
            if (!rule.enabled()) continue;
            try {
                SearchResponse response = logService.search("", rule.level(), rule.service(), null, null, 0, 100);
                int count = response.totalHits();
                int previous = lastTriggeredCounts.getOrDefault(rule.id(), 0);
                if (count >= rule.threshold() && previous < rule.threshold()) {
                    events.add(new AlertEvent(rule.id(), rule.name(), count, rule.threshold(), Instant.now()));
                }
                lastTriggeredCounts.put(rule.id(), count);
            } catch (Exception exception) {
                // A failed evaluation is isolated so other alert rules can still run.
            }
        }
    }
}
