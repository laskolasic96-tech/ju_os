# Installation Guide

## Requirements

- Java 17+
- Maven 3.6+
- Oracle Database 11g+

## Quick Install

```bash
# Download release
wget https://github.com/example/ju-os/releases/download/v0.0.1/ju-os-0.0.1.tar.gz
tar -xzf ju-os-0.0.1.tar.gz
cd ju-os-0.0.1

# Configure
cp config.example.yaml config.yaml
vim config.yaml

# Run
java -jar ju-os.jar
```

## Docker

```bash
docker build -t ju-os .
docker run -p 8080:8080 -v $(pwd)/config.yaml:/app/config.yaml ju-os
```

## Verification

```bash
curl http://localhost:8080/api/v1/health
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Java not found | Set JAVA_HOME |
| Port in use | Change port or kill process |
| DB connection failed | Check credentials |
