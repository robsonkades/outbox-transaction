# Outbox Transactions Worker

An example Spring Boot 3 project that demonstrates the Outbox Pattern with transactional writes and asynchronous processing. It stores domain events in an outbox table within the same database transaction as your domain changes and processes them with a background worker to achieve reliable, at-least-once delivery and decoupled workflows.

This repository is intentionally small, focused on the outbox flow and high-performance processing using keyset pagination and batched writes.


## Key Features
- Outbox Pattern using JPA/Hibernate and a relational database
- Reliable, at-least-once processing via scheduled worker
- Keyset pagination to process large backlogs efficiently
- Batched updates and inserts for throughput
- Spring TransactionTemplate wrapper to ensure atomic updates
- HTTP endpoints to insert and read outbox events (demo purposes)
- Micrometer + Spring Boot Actuator for health and metrics
- Undertow web server and Virtual Threads enabled for scalability
- Ready to run with SQL Server by default; easy to switch to PostgreSQL


## Architecture Overview
1. Your application writes domain data and an Outbox event inside the same database transaction.
2. The Outbox Worker scans the outbox_event table using keyset pagination in small windows, updates processed events, and optionally chains next events to simulate a pipeline.
3. External systems (e.g., message brokers) could be integrated where the worker publishes events; in this demo, we focus on DB-based transitions to keep the sample minimal.

Relevant code:
- br.com.robsonkades.worker.outbox.OutboxJpaEntity – JPA entity for outbox_event
- br.com.robsonkades.worker.outbox.OutboxWorker – Scheduled worker that processes events in batches
- br.com.robsonkades.worker.outbox.OutboxController – Demo endpoints to create and page outbox events
- br.com.robsonkades.worker.transaction.Transaction – Abstraction over transactional execution


## Technology Stack
- Java 25+ (project sets Java 25 target; buildpacks set JVM 21 — use JDK 21 or newer)
- Spring Boot 3.5.x
- Spring Data JPA (Hibernate)
- Undertow
- Micrometer + Actuator (Prometheus registry at runtime)
- Database: SQL Server (default) or PostgreSQL


## Getting Started
### Prerequisites
- JDK 21+ installed (JAVA_HOME configured)
- Maven 3.9+
- A running database:
  - SQL Server (default configuration), or
  - PostgreSQL (instructions below)

### Configuration
Application properties are in src/main/resources/application.yml. Key settings:
- spring.datasource.url (env override: SQL_JDBC)
- spring.datasource.username
- spring.datasource.password (env override: SQL_PASSWORD)
- spring.jpa.properties.hibernate.dialect

By default, SQL Server is enabled. Virtual threads and Undertow are enabled. Actuator endpoints are exposed under /.

Example environment overrides (PowerShell):

$env:SQL_JDBC = "jdbc:sqlserver://localhost:1433;encrypt=false;databaseName=master;encrypt=true;trustServerCertificate=true;sendStringParametersAsUnicode=false;ApplicationName=App"
$env:SQL_PASSWORD = "YourStrong!Passw0rd"

Then run the application as shown below.

### Database Schema
Create the outbox_event table before running. DDL examples:

SQL Server:

```sql
CREATE TABLE outbox_event (
    id BIGINT IDENTITY(1,1) NOT NULL,
    aggregate_id varchar(255) NOT NULL,
    aggregate_type varchar(50) NOT NULL,
    event_type tinyint NOT NULL,
    payload nvarchar(2000) NOT NULL,
    status tinyint NOT NULL,
    created_at DATETIME2(3) NOT NULL,
    processed_at DATETIME2(3),
    retry_count tinyint NOT NULL DEFAULT 0
)

CREATE NONCLUSTERED INDEX IX_Outbox_Status_Event_Type_Covering
    ON outbox_event (status ASC, event_type ASC, id ASC)
    INCLUDE (aggregate_id, aggregate_type, payload, created_at, processed_at, retry_count)
    WITH (FILLFACTOR = 85, ONLINE = ON);
```

PostgreSQL:

```sql
CREATE TABLE outbox_event (
    id BIGSERIAL PRIMARY KEY,
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    event_type SMALLINT NOT NULL,
    payload TEXT NOT NULL,
    status SMALLINT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITHOUT TIME ZONE,
    retry_count SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_outbox_processed_event_type_status_covering
    ON outbox_event (event_type ASC, status ASC, id ASC)
    INCLUDE (aggregate_id, aggregate_type, payload, created_at, processed_at, retry_count)
    WITH (FILLFACTOR = 85);
```

Note: event_type and status are stored as small numeric codes (enums in code).

### Switch to PostgreSQL
In application.yml, change the following (and re-run):
- spring.datasource.driver-class-name: org.postgresql.Driver
- spring.datasource.url: jdbc:postgresql://localhost:5432/postgres
- spring.datasource.username / password
- spring.jpa.properties.hibernate.dialect: org.hibernate.dialect.PostgreSQLDialect

You can also comment/uncomment the already-present lines in the YAML that hint at Postgres.


## Running the Application
From the project root:

mvn spring-boot:run

The server starts on port 8080 with context-path /api.

- Health: GET http://localhost:8080/health
- Metrics: GET http://localhost:8080/actuator/metrics


## Demo Endpoints
Base path: http://localhost:8080/api

1) Create a demo outbox event

POST /outbox

PowerShell:

Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/outbox"

2) Page through outbox events (keyset pagination)

GET /outbox

Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/outbox" | ConvertTo-Json -Depth 5

The response contains content, hasNext, and next (a map of keyset values). To get the next page, pass the next map as query parameters.

Example:
- First page: GET /outbox
- Suppose response.next = {"id": 123}
- Next page: GET /outbox?id=123

Note: The actual key names come from Spring Data’s keyset serialization; use the values from the previous response’s next field.


## Worker Behavior
The OutboxWorker runs on a fast fixedDelay schedule (100 ms after an initial 5s delay) using a custom scheduler bean (fastScheduler). It processes events in the following simulated stages:
- ORDER_CREATED -> ORDER_SHIPPED -> PAYMENT_CONFIRMED -> INVOICE_GENERATED

Each stage:
- Reads up to 100 PENDING events of a given event type ordered by id ASC using keyset pagination
- Marks them as PROCESSED and sets processedAt
- Optionally creates the next stage event as PENDING with the same aggregate id and payload
- Uses batched saveAll for efficiency

The worker loops for a limited number of rounds per tick to avoid monopolizing threads while still draining the backlog quickly.


## Build and Packaging
- Run tests and package:

mvn -DskipTests package

- Build container image with Spring Boot Buildpacks (configured for native image by default):

mvn spring-boot:build-image

Notes:
- Buildpacks env in the POM sets BP_JVM_VERSION=21 and BP_NATIVE_IMAGE=true; you can disable native by overriding env or adjusting the POM.
- GraalVM Native configuration is included via the native-maven-plugin.


## Observability
- Actuator endpoints are exposed under base-path "/" and context-path "/api"
  - Health: /api/health
  - Metrics: /api/actuator/metrics
- Micrometer registry for Prometheus is on the classpath; scrape /api/actuator/prometheus if enabled in your environment.


## Troubleshooting
- Connection timeouts: Hikari pool timeouts are intentionally low to fail fast; check DB connectivity and credentials.
- Table not found: Ensure you created the outbox_event table (see DDL above). DDL auto is disabled (ddl-auto: none).
- Pagination keys: Always pass the next map exactly as returned by the previous response.
- Switching databases: Make sure both driver and dialect are consistent with your DB vendor.


## License
This sample is provided as-is for educational purposes. Use at your own risk.