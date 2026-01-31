# Development Guide

## Prerequisites

- **JDK 21** or later
- **Gradle 8.x** (wrapper included)

### Verify Java Version

```bash
java -version
# Should show: openjdk version "21.x.x" or similar
```

## Quick Start

```bash
# Clone and enter project
cd wes

# Build the project
./gradlew build

# Run the application
./gradlew run

# Run tests
./gradlew test
```

## Build Commands

| Command | Description |
|---------|-------------|
| `./gradlew build` | Compile and run tests |
| `./gradlew run` | Launch the application |
| `./gradlew test` | Run all tests |
| `./gradlew test --tests "ClassName"` | Run specific test class |
| `./gradlew test --tests "ClassName.NestedClass"` | Run nested test class |
| `./gradlew clean` | Delete build artifacts |
| `./gradlew clean build` | Full rebuild |

### Test Examples

```bash
# Run all PhotoService tests
./gradlew test --tests "PhotoServiceTest"

# Run specific nested test group
./gradlew test --tests "CategoryServiceTest.CategoryCreationTests"

# Run with verbose output
./gradlew test --info
```

## Project Structure

```
wes/
├── build.gradle.kts          # Build configuration
├── settings.gradle.kts       # Project settings
├── gradlew                   # Gradle wrapper (Unix)
├── gradlew.bat               # Gradle wrapper (Windows)
├── AGENTS.MD                 # AI agent instructions
├── docs/                     # Documentation
│   ├── ARCHITECTURE.md
│   ├── API.md
│   └── DEVELOPMENT.md
└── src/
    ├── main/kotlin/          # Source code
    │   ├── Main.kt
    │   ├── domain/
    │   ├── ui/
    │   └── util/
    └── test/kotlin/          # Tests
        └── domain/service/
```

## IDE Setup

### IntelliJ IDEA (Recommended)

1. Open project folder (`wes/`)
2. IntelliJ will detect Gradle project automatically
3. Wait for indexing to complete
4. Run configurations are auto-detected

### VS Code

1. Install "Kotlin" extension
2. Install "Gradle for Java" extension
3. Open project folder
4. Run tasks via Gradle sidebar

## Code Style

### File Organization

```kotlin
package domain.service          // 1. Package declaration

import domain.model.Photo       // 2. Imports (grouped)
import java.nio.file.Path

/**                             // 3. KDoc
 * Service description.
 */
class MyService {               // 4. Class
    // ...
}
```

### Import Grouping Order

1. `domain.*` (project domain)
2. `javafx.*` (JavaFX)
3. `util.*` (project utilities)
4. `java.*` (Java stdlib)

### Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `PhotoService`, `CategoryCard` |
| Functions | camelCase, verb+noun | `getPhotoById`, `handleDragDetected` |
| Properties | camelCase | `selectedPhotoIds` |
| Constants | UPPER_SNAKE_CASE | `MAX_CACHE_SIZE` |
| Packages | lowercase | `domain.model`, `ui.handler` |

### Test Structure

```kotlin
@DisplayName("ServiceName Tests")
class ServiceNameTest {

    private lateinit var service: ServiceName

    @BeforeEach
    fun setUp() {
        service = ServiceName()
    }

    @Nested
    @DisplayName("Feature Group")
    inner class FeatureGroupTests {

        @Test
        fun `should do X when Y`() {
            // Arrange
            val input = ...

            // Act
            val result = service.doSomething(input)

            // Assert
            assertEquals(expected, result)
        }
    }
}
```

## Adding New Features

### New Domain Model

1. Create `src/main/kotlin/domain/model/NewModel.kt`
2. Use `data class` with immutable properties where possible
3. Add factory method in `companion object` if needed
4. No UI imports allowed

```kotlin
package domain.model

data class NewModel(
    val id: String,
    val name: String
) {
    companion object {
        fun create(name: String): NewModel {
            return NewModel(
                id = "model_${System.currentTimeMillis()}",
                name = name
            )
        }
    }
}
```

### New Service

1. Create `src/main/kotlin/domain/service/NewService.kt`
2. Add KDoc documentation
3. Return copies of internal collections
4. Create test file

```kotlin
package domain.service

/**
 * Service for managing X.
 */
class NewService {
    private val items = mutableListOf<Item>()

    fun getItems(): List<Item> = items.toList()

    fun addItem(item: Item) {
        items.add(item)
    }
}
```

### New UI Component

1. Create `src/main/kotlin/ui/component/NewComponent.kt`
2. Extend appropriate JavaFX class
3. Wire up in `PhotoCategorizerApp`

```kotlin
package ui.component

import javafx.scene.layout.VBox

class NewComponent(
    private val onAction: () -> Unit
) : VBox() {
    init {
        // Setup UI
    }
}
```

### New Handler

1. Create `src/main/kotlin/ui/handler/NewHandler.kt`
2. Accept services via constructor
3. Provide methods for each event type

```kotlin
package ui.handler

import domain.service.SomeService

class NewHandler(
    private val someService: SomeService
) {
    fun handleEvent(event: Event) {
        // Handle event
    }
}
```

## Testing

### Run Tests with Coverage

```bash
./gradlew test jacocoTestReport
# Report at: build/reports/jacoco/test/html/index.html
```

### Test Patterns

**Arrange-Act-Assert**:
```kotlin
@Test
fun `should return null for invalid index`() {
    // Arrange
    photoService.setPhotos(listOf(photo1, photo2))

    // Act
    val result = photoService.getPhotoByIndex(-1)

    // Assert
    assertNull(result)
}
```

**Edge Cases to Test**:
- Empty collections
- Null/invalid inputs
- Boundary conditions
- Large datasets

## Debugging

### JavaFX Application

1. Set breakpoints in IntelliJ
2. Run with Debug (`Shift+F9`)
3. Use "Evaluate Expression" for JavaFX properties

### Common Issues

| Issue | Solution |
|-------|----------|
| "JavaFX runtime components are missing" | Ensure JDK 21 with JavaFX modules |
| Tests fail with NPE | Check `@BeforeEach` initialization |
| Images not loading | Check file paths, clear `ImageCache` |
| Drag-drop not working | Verify dragboard content format |

## Performance Considerations

- **ImageCache**: Limited to 100 images (LRU eviction)
- **Large ZIPs**: Extracted to temp directory, cleaned on exit
- **UI Thread**: Keep heavy operations off JavaFX thread
- **Export**: Uses background `Task` for file operations

## Useful Gradle Tasks

```bash
# List all available tasks
./gradlew tasks

# Check for dependency updates
./gradlew dependencyUpdates

# Generate dependency tree
./gradlew dependencies

# Run with debug logging
./gradlew run --debug
```

## Distribution & Packaging

The application can be packaged as native installers or portable distributions that include a bundled JRE, so users don't need to install Java.

### Build Commands

| Command | Output | Size | Description |
|---------|--------|------|-------------|
| `./gradlew runtimeZip` | `build/image.zip` | ~50 MB | Portable zip with bundled JRE |
| `./gradlew jpackageImage` | `build/jpackage/wes-photo-categorizer/` | ~77 MB | Standalone app folder |
| `./gradlew jpackage` | `build/jpackage/*.deb\|.msi\|.dmg` | ~47 MB | Platform-specific installer |

### Portable Distribution (runtimeZip)

Creates a zip archive containing the application and a bundled JRE. Users can extract and run without installing Java.

```bash
./gradlew runtimeZip
```

**Output:** `build/image.zip`

**Contents:**
```
wes-photo-categorizer/
├── bin/
│   ├── java                        # Bundled Java runtime
│   ├── wes-photo-categorizer       # Linux/macOS launcher
│   └── wes-photo-categorizer.bat   # Windows launcher
├── lib/                            # Application JARs & native libraries
├── conf/                           # JRE configuration
└── legal/                          # License files
```

**Running the portable distribution:**

| Platform | Command |
|----------|---------|
| Linux/macOS | `./bin/wes-photo-categorizer` |
| Windows | `bin\wes-photo-categorizer.bat` |

### Native Installers (jpackage)

Creates platform-specific installers with bundled JRE.

```bash
./gradlew jpackage
```

| Platform | Output | Install Method |
|----------|--------|----------------|
| **Linux** | `.deb` | `sudo dpkg -i wes-photo-categorizer_1.0.0_amd64.deb` |
| **Windows** | `.msi` | Double-click installer |
| **macOS** | `.dmg` | Double-click, drag to Applications |

> **Note:** `runtimeZip` and `jpackage` create distributions for the **current platform only**. To build for Windows, run the command on a Windows machine. To build for Linux, run on Linux.
