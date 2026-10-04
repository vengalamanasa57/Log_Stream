# LogStream Architecture — Week 1 & Week 2

## 1. Components

### React Dashboard
Responsible for:
- search input
- level filter
- service filter
- response-time filters
- result table

### Spring Boot API
Responsible for:
- exposing REST search endpoints
- accepting development/test log requests
- connecting the UI to the search layer

### gRPC Ingestion Service
Responsible for:
- receiving structured `LogMessage` objects
- supporting single-log and client-streaming ingestion
- forwarding logs to the indexing layer

### Lucene Search Layer
Responsible for:
- converting each log into a Lucene `Document`
- indexing searchable fields
- executing keyword and structured filters
- returning paginated results

## 2. Data flow

```text
Producer
   |
   | gRPC LogMessage
   v
LogIngestionGrpcService
   |
   v
LuceneLogService
   |
   v
Lucene Index
   ^
   |
SearchController
   ^
   |
React Dashboard
```

## 3. Why Lucene?

Lucene provides an inverted-index-based search engine inside the Java application. This is different from scanning every row of a traditional relational table for every text search.

For this internship phase, Lucene is used directly so the indexing and query pipeline can be demonstrated without introducing Elasticsearch as another distributed service.

## 4. Searchable fields

| Field | Lucene representation | Purpose |
|---|---|---|
| id | StringField | unique document |
| level | StringField | exact filter |
| service | StringField | exact filter |
| message | TextField | keyword search |
| timestamp | StringField + LongPoint | storage/range groundwork |
| responseTimeMs | StoredField + LongPoint | numeric filtering |
| host | StringField | metadata |
| traceId | StringField | trace correlation |

## 5. Current limitations

- Single application instance
- Local filesystem Lucene index
- No Kafka
- No distributed Lucene cluster
- No authentication
- No alerting yet
- No WebSocket Live Tail yet
