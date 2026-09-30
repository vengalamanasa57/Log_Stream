# LogStream — Distributed Log Analytics & Alerting Platform


LogStream is a Java-based log ingestion and search platform. It receives structured application logs through gRPC, indexes them using Apache Lucene, and exposes REST search APIs consumed by a React dashboard.

### Current scope

**Week 1**
- Java backend foundation
- Protobuf `LogMessage` schema
- gRPC log ingestion server
- React dashboard scaffolding
- Search UI and log-results layout

**Week 2**
- Apache Lucene integration
- Log parsing and indexing
- Search API
- Filters for level, service, and response time
- Pagination
- React search/filter interface
- Sample log generator
- Basic automated tests

### Architecture

```text
                    ┌──────────────────────────┐
                    │       React Dashboard    │
                    │ Search + Filters + Table │
                    └────────────┬─────────────┘
                                 │ HTTP/JSON
                                 ▼
                    ┌──────────────────────────┐
                    │     Spring Boot API      │
                    │     SearchController     │
                    └────────────┬─────────────┘
                                 │
                                 ▼
                    ┌──────────────────────────┐
                    │       LogService         │
                    │  ingestion + searching   │
                    └──────────┬───────┬───────┘
                               │       │
                         index │       │ search
                               ▼       ▼
                         ┌────────────────┐
                         │ Apache Lucene  │
                         │ Search Index   │
                         └────────────────┘
                               ▲
                               │
                         gRPC ingestion
                               │
                    ┌──────────┴───────────┐
                    │   LogIngestionService│
                    │       (gRPC)         │
                    └──────────┬───────────┘
                               ▲
                               │
                    ┌──────────┴───────────┐
                    │ Sample Log Generator │
                    └──────────────────────┘
```

## Technologies

- Java 17
- Spring Boot
- gRPC + Protocol Buffers
- Apache Lucene
- React
- Vite
- Maven

## Project structure

```text
LogStream/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/logstream/
│       │   │   ├── LogStreamApplication.java
│       │   │   ├── config/
│       │   │   ├── controller/
│       │   │   ├── grpc/
│       │   │   ├── model/
│       │   │   └── search/
│       │   ├── proto/log.proto
│       │   └── resources/application.properties
│       └── test/
│           └── java/com/logstream/search/
├── frontend/
│   ├── package.json
│   ├── vite.config.js
│   └── src/
├── docs/
│   ├── architecture.md
│   ├── week-1-progress.md
│   ├── week-2-progress.md
│   └── mid-review-script.md
└── sample/
    └── logs.json
```
