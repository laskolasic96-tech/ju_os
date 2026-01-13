# ju-os

Lightweight Java HTTP Server with Oracle Database Integration - Built with Undertow

## Quick Start

```bash
mvn clean package
java -jar target/ju-os-0.0.1-SNAPSHOT.jar
curl http://localhost:8080/api/v1/tables
```

## Features

- REST API for Oracle DB operations
- JSON and CSV output
- Connection pooling
- Health checks
- Pagination support

## Documentation

- [README.md](README.md) - Main docs
- [FEATURES.md](FEATURES.md) - Features list
- [PLAN.md](PLAN.md) - Development roadmap
- [INSTALL.md](INSTALL.md) - Installation guide
- [BUILD.md](BUILD.md) - Build instructions
- [DEPLOY.md](DEPLOY.md) - Deployment guide

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/tables` | List all tables |
| GET | `/api/v1/views` | List all views |
| GET | `/api/v1/all` | List tables & views |
| GET | `/api/v1/table/{name}/data` | Get data (JSON) |
| GET | `/api/v1/table/{name}/csv` | Export CSV |
| GET | `/api/v1/table/{n}/row/{c}/{id}` | Get row by ID |
| PUT | `/api/v1/table/{n}/update/{c}/{id}` | Update row |
| DELETE | `/api/v1/table/{n}/delete/{c}/{id}` | Delete row |
| GET | `/api/v1/health` | Health check |

## License

MIT
