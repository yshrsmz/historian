# Task Completion Checklist

When completing a task in this project, ensure the following steps are performed:

## Before Committing

### 1. Build Verification
```bash
./gradlew clean build
```
Ensure the build completes without errors.

### 2. Run Tests
```bash
./gradlew test
```
All tests must pass.

### 3. Code Review Checklist
- [ ] New code follows existing Java conventions (see `code_style_and_conventions.md`)
- [ ] Public API changes are minimal and backward-compatible when possible
- [ ] Internal implementation classes remain in the `internal` package
- [ ] No unused imports or code
- [ ] AndroidX libraries used (not support libraries)

## For Version Changes

### Update Version
- Edit `VERSION_NAME` in `gradle.properties`
- Update `CHANGELOG.md` if applicable

## For Dependency Updates

### Dependencies Location
- All dependencies centralized in `gradle/dependencies.gradle`
- Update there first, then reference via `deps.xxx` in module build files

## Coverage Report (Optional)
```bash
./gradlew :historian-core:jacocoTestReportDebug
```
Check coverage report at `historian-core/build/reports/jacoco/test/jacocoTestReportDebug/index.html`
