# Build Guide

## Prerequisites

- Java 17+
- Maven 3.6+

## Build Commands

```bash
# Standard build
mvn clean package

# Skip tests
mvn clean package -DskipTests

# Uber JAR (self-contained)
mvn clean package

# Run tests
mvn test

# Generate coverage
mvn clean test jacoco:report
```

## Build Artifacts

| Artifact | Location |
|----------|----------|
| JAR | `target/ju-os-0.0.1-SNAPSHOT.jar` |
| Uber JAR | `target/ju-os-uber-0.0.1-SNAPSHOT.jar` |
| Dependencies | `target/lib/` |

## Profiles

| Profile | Purpose |
|---------|---------|
| dev | Debug logging (default) |
| prod | Minimal logging |
| docker | Docker image build |

## IDE Setup

### IntelliJ IDEA
- Import pom.xml
- Set SDK to Java 17
- Add run configuration

### VS Code
- Install Java extensions
- Use Java Language Support
- Maven support enabled

## Local Development

```bash
# Debug mode
mvnDebug clean package

# Connect debugger on port 5005
```
