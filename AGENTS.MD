# AGENTS.MD - Coding Agent Instructions

## Build/Test Commands
- Build: `./gradlew build`
- Run app: `./gradlew run`
- Run all tests: `./gradlew test`
- Run single test: `./gradlew test --tests "ClassName"` (e.g., `--tests "PhotoServiceTest"`)
- Clean: `./gradlew clean`

## Language & Framework
- Kotlin 2.2.21 with JVM target 21
- JavaFX 21.0.2 for UI (modules: `javafx.controls`, `javafx.fxml`)
- JUnit 5 for testing
- Gradle with Kotlin DSL (`build.gradle.kts`)

## Project Overview
**Photo Categorizer** - A JavaFX desktop app for organizing photos into categories via drag-and-drop.

### Core Functionality
1. Load photos from ZIP file
2. Display photos in a resizable grid
3. Create categories and drag-drop photos into them
4. Multi-select photos (click, Ctrl+click, Shift+click)
5. Reorder photos within categories via drag-and-drop
6. Move photos between categories from category view
7. Export categorized photos to directories

## Architecture

### Package Structure
```
src/main/kotlin/
├── Main.kt                          # Entry point, launches JavaFX
├── domain/
│   ├── model/                       # Pure data classes
│   │   ├── Photo.kt                 # Photo with id, path, fileName, originalIndex
│   │   ├── Category.kt              # Category with id, number, name, photos list
│   │   └── Selection.kt             # Selection state (anchor, selectedPhotoIds)
│   └── service/                     # Business logic (no UI deps)
│       ├── PhotoService.kt          # Manages main photo collection
│       ├── CategoryService.kt       # Manages categories and photo assignments
│       ├── SelectionService.kt      # Manages selection state
│       ├── FileService.kt           # ZIP extraction, file I/O
│       └── ExportService.kt         # Export photos to directories
├── ui/
│   ├── PhotoCategorizerApp.kt       # Main Application class, orchestrates everything
│   ├── component/
│   │   └── CategoryCard.kt          # UI component for category display
│   └── handler/
│       ├── DragDropHandler.kt       # Drag-and-drop to categories
│       ├── ReorderDragDropHandler.kt # Reorder photos within category view
│       └── SelectionHandler.kt      # Click/selection logic
└── util/
    ├── ImageCache.kt                # LRU cache for loaded images
    ├── ImageUtils.kt                # Image loading/resizing helpers
    └── StyleConstants.kt            # UI dimension constants
```

### Key Design Patterns
- **Services hold state**: `PhotoService` owns uncategorized photos, `CategoryService` owns categories
- **Handlers delegate UI events**: `SelectionHandler`, `DragDropHandler`, and `ReorderDragDropHandler` encapsulate complex UI logic
- **Models are mutable internally**: `Category.photos` is `MutableList`, but services return copies
- **Original order preserved**: `Photo.originalIndex` maintains original sequence for restoration when categories are deleted

### Data Flow
1. ZIP loaded → `FileService.extractPhotosFromZip()` → `PhotoService.setPhotos()`
2. Photo dragged to category → `DragDropHandler` → `CategoryService.addPhotoToCategory()` + `PhotoService.removePhoto()`
3. Photo reordered in category view → `ReorderDragDropHandler` → `CategoryService.reorderPhotoInCategory()`
4. Category deleted → `PhotoService.restorePhotos()` sorts by `originalIndex`
5. Export → `ExportService.exportCategories()` copies files to category subdirectories

## Code Style

### Imports & Formatting
- Group imports by: domain, javafx, util, java stdlib
- No wildcard imports
- Organize: package > imports > blank line > KDoc > class

### Naming & Structure
- Classes: PascalCase (e.g., `PhotoService`, `CategoryCard`)
- Functions: camelCase with descriptive verb+noun (e.g., `getPhotoById`, `handleDragDetected`)
- Properties: camelCase for mutable, UPPER_SNAKE_CASE for constants in `object`
- Packages: lowercase, organized as `domain/model`, `domain/service`, `ui/component`, `ui/handler`, `util`

### Types & Nullability
- Use explicit types for public APIs; infer for local variables
- Prefer immutability: `val` over `var`, `List` over `MutableList` in return types
- Use `?` for nullable types, handle with `?.`, `?:`, or explicit null checks
- Return copies from services (e.g., `photos.toList()`) to protect internal state

### Documentation & Testing
- KDoc on all public classes and functions with `@param`, `@return` where applicable
- Test structure: `@Nested` inner classes with `@DisplayName`, backtick test names
- Test pattern: arrange-act-assert with descriptive assertions
- Test file naming: `<ClassName>Test.kt` in matching package under `src/test/kotlin/`

### Domain-Driven Design
- Pure domain models in `domain/model` (no UI dependencies)
- Business logic in `domain/service`
- UI concerns strictly in `ui` package
- Use dependency injection via constructor parameters

## Common Tasks

### Adding a New Service
1. Create `domain/service/NewService.kt` with KDoc
2. Add constructor injection in `PhotoCategorizerApp`
3. Create `domain/service/NewServiceTest.kt` with `@Nested` test groups

### Adding a New Model
1. Create `domain/model/NewModel.kt` as `data class`
2. Add factory method in `companion object` if needed
3. Keep free of UI imports

### Adding UI Features
1. For new components: create in `ui/component/`
2. For event handling: create handler in `ui/handler/`
3. Wire up in `PhotoCategorizerApp`

## Testing

### Test Structure Example
```kotlin
@DisplayName("ServiceName Tests")
class ServiceNameTest {
    @Nested
    @DisplayName("Feature Group")
    inner class FeatureGroupTests {
        @Test
        fun `should do something when condition`() {
            // arrange
            // act
            // assert
        }
    }
}
```

### Running Specific Tests
```bash
./gradlew test --tests "PhotoServiceTest"
./gradlew test --tests "CategoryServiceTest.CategoryCreationTests"
```
