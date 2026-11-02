# Alerting API (initial prototype)

All endpoints use the backend base URL, for example `http://localhost:8080`.

## Create a rule

`POST /api/alerts/rules`

Example JSON:

```json
{
  "name": "Billing API error threshold",
  "level": "ERROR",
  "service": "billing-api",
  "threshold": 100,
  "enabled": true
}
```

## List rules

`GET /api/alerts/rules`

## Delete a rule

`DELETE /api/alerts/rules/{id}`

## List triggered events

`GET /api/alerts/events`

## Current prototype limitations

- Rules and events are held in memory and are lost when the backend restarts.
- The scheduled evaluator runs every 60 seconds.
- This first implementation evaluates the total indexed count matching level/service, not a rolling time window. The `threshold` is therefore not yet a "within the last N minutes" threshold.
- Webhook/email delivery is not implemented in this initial additive package.
- The evaluator isolates exceptions per rule; inspect backend logs when a rule does not evaluate as expected.
