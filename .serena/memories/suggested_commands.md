# Suggested Commands

## Build Commands

```bash
# Build the entire project
./gradlew build

# Clean build
./gradlew clean build
```

## Testing Commands

```bash
# Run all tests
./gradlew test

# Run tests for specific modules
./gradlew :historian-core:test
./gradlew :historian-tree:test

# Generate coverage report (historian-core only)
./gradlew :historian-core:jacocoTestReportDebug
```

## Dependency Management

```bash
# Check for dependency updates
./gradlew dependencyUpdates
```

## Publishing

```bash
# Publish to Maven Central (requires credentials)
./gradlew clean publish --no-daemon
```

## Gradle Utilities

```bash
# List all available tasks
./gradlew tasks

# Show project dependencies
./gradlew :historian-core:dependencies
./gradlew :historian-tree:dependencies
```
