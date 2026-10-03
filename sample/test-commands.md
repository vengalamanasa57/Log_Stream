# Manual Test Commands

## Index a sample log

PowerShell:

```powershell
Invoke-RestMethod -Method Post `
  -Uri http://localhost:8080/api/logs `
  -ContentType "application/json" `
  -Body '{"timestamp":"2026-09-30T10:00:00Z","level":"ERROR","service":"payment-service","message":"Database connection timeout","responseTimeMs":1500,"host":"host-1","traceId":"trace-001"}'
```

## Search

```text
http://localhost:8080/api/logs/search?q=database
```

## Filter by level

```text
http://localhost:8080/api/logs/search?level=ERROR
```

## Filter by service

```text
http://localhost:8080/api/logs/search?service=payment-service
```

## Filter by response time

```text
http://localhost:8080/api/logs/search?minResponseTime=1000
```
