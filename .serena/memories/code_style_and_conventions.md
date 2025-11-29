# Code Style and Conventions

## Language
- Pure Java codebase (Java 11 source/target compatibility)
- No Kotlin usage in the library modules

## Package Structure
- Root package: `net.yslibrary.historian`
- Internal classes under: `net.yslibrary.historian.internal`
- Timber integration: `net.yslibrary.historian.tree`

## Code Conventions

### Class Naming
- Main public API classes: Simple names like `Historian`, `HistorianTree`
- Internal implementation classes: Descriptive names like `LogWriter`, `LogWritingTask`, `DbOpenHelper`
- Exception classes: Suffix with `Exception` (e.g., `HistorianFileException`)

### API Design Patterns
- **Builder Pattern**: Used for `Historian` configuration (`Historian.Builder`)
- **Callback Interface**: `Historian.Callbacks` for lifecycle events
- **Static Factory Method**: `Historian.builder(context)` as entry point

### Field Naming
- Constants: `UPPER_SNAKE_CASE` (e.g., `DB_NAME`, `LOG_LEVEL`)
- Instance fields: `camelCase` without prefix (e.g., `dbOpenHelper`, `logWriter`)

### Documentation
- Minimal Javadoc - focus on public API
- Author annotations in test classes (e.g., `Created by yshrsmz on 2017/01/22`)

### Testing
- Test classes: `*Test.java` suffix
- Uses Robolectric for Android unit tests
- `@RunWith(RobolectricTestRunner.class)` annotation
- `ApplicationProvider.getApplicationContext()` for test context

### Android Specific
- Uses AndroidX libraries exclusively
- Non-transitive R class enabled
- BuildConfig generation disabled
