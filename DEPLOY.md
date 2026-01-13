# Deployment Guide

## Deployment Options

| Option | Best For | Complexity |
|--------|----------|------------|
| JAR | Single server | Low |
| Docker | Containerized | Medium |
| Kubernetes | Orchestrated | High |
| Cloud | Managed | Medium |

## Pre-Deployment Checklist

- [ ] Java 17+ installed
- [ ] Database accessible
- [ ] Firewall configured
- [ ] Config updated for production
- [ ] Tests passing
- [ ] Backup completed

## JAR Deployment

```bash
# Copy files
scp ju-os.jar user@server:/opt/ju-os/
scp config.yaml user@server:/opt/ju-os/

# Run
java -Xms128m -Xmx256m -jar /opt/ju-os/ju-os.jar
```

## systemd Service

```ini
[Unit]
Description=ju-os HTTP Server

[Service]
Type=simple
User=ju-os
WorkingDirectory=/opt/ju-os
ExecStart=/usr/bin/java -Xms128m -Xmx256m -jar /opt/ju-os/ju-os.jar
Restart=always

[Install]
WantedBy=multi-user.target
```

## Docker Deployment

```bash
# Build
docker build -t ju-os .

# Run
docker run -d \
  --name ju-os \
  -p 8080:8080 \
  -v $(pwd)/config.yaml:/app/config.yaml \
  ju-os
```

## Docker Compose

```yaml
version: '3.8'
services:
  ju-os:
    image: ju-os:latest
    ports:
      - "8080:8080"
    volumes:
      - ./config.yaml:/app/config.yaml
    restart: unless-stopped
```

## Kubernetes

```bash
# Apply configs
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml

# Scale
kubectl scale deployment ju-os --replicas=3

# Check status
kubectl get pods -l app=ju-os
```

## Monitoring

```bash
# Health check
curl http://localhost:8080/api/v1/health

# Response
# {"status":"UP","database":{"status":"UP"}}
```

## Security

### SSL/TLS
```yaml
server:
  port: 8443
  ssl:
    enabled: true
    keyStore: /path/to/keystore.jks
```

### Reverse Proxy (Nginx)
```nginx
upstream ju-os {
    server 127.0.0.1:8080;
}

server {
    listen 443 ssl;
    location / {
        proxy_pass http://ju-os;
    }
}
```

## Rollback

```bash
# Stop current
sudo systemctl stop ju-os

# Restore previous version
cp /backup/ju-os-old.jar /opt/ju-os/ju-os.jar

# Start
sudo systemctl start ju-os
```
