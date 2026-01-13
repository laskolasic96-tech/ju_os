# Features

## Core Features

| Feature | Description |
|---------|-------------|
| HTTP Server | Undertow-based non-blocking I/O |
| Connection Pool | Configurable DB connection pool |
| REST API | Standard RESTful endpoints |
| JSON Support | Jackson-powered serialization |
| CSV Export | Export table data to CSV |
| Pagination | Configurable offset/limit |
| Health Check | Built-in monitoring endpoints |

## Database Operations

| Operation | Method | Endpoint |
|-----------|--------|----------|
| List Tables | GET | `/api/v1/tables` |
| List Views | GET | `/api/v1/views` |
| Get Data | GET | `/api/v1/table/{name}/data` |
| Export CSV | GET | `/api/v1/table/{name}/csv` |
| Get Row | GET | `/api/v1/table/{n}/row/{c}/{id}` |
| Update | PUT | `/api/v1/table/{n}/update/{c}/{id}` |
| Delete | DELETE | `/api/v1/table/{n}/delete/{c}/{id}` |

## Performance

| Metric | Value |
|--------|-------|
| Startup | < 1 second |
| Memory | ~100MB |
| Throughput | 10,000+ req/s |

## Security

- SQL injection protection
- Prepared statements
- Identifier escaping

## Extensibility

- Custom handlers
- Middleware support
- Database abstraction layer
