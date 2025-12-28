# PROJECT KNOWLEDGE BASE

**Generated:** 2025-12-28
**Commit:** b481407
**Branch:** main

## OVERVIEW

Kotlin/JavaFX desktop app for organizing photos into categories via drag-drop. Load ZIP → grid display → categorize → export to directories.

## STRUCTURE

```
wes/
├── src/main/kotlin/
│   ├── Main.kt                    # Entry: Application.launch()
│   ├── domain/
│   │   ├── model/                 # Photo, Category, Selection (data classes)
│   │   └── service/               # Business logic (no UI deps)
│   ├── ui/
│   │   ├── PhotoCategorizerApp.kt # Main app, orchestrates all components
│   │   ├── component/             # CategoryCard, ButtonFactory
│   │   ├── controller/            # View-model coordination
│   │   └── handler/               # Event handling (drag-drop, selection)
│   └── util/                      # ImageCache, ImageUtils, StyleConstants
├── src/test/kotlin/               # Mirrors main structure
├── build.gradle.kts               # Kotlin 2.2.21, JavaFX 21.0.2, JUnit 5
└── gradle/                        # Wrapper (9.2.1)
```

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| Add domain model | `domain/model/` | Data class, no UI imports |
| Add business logic | `domain/service/` | Constructor inject deps |
| Add UI component | `ui/component/` | Extend JavaFX node |
| Add event handling | `ui/handler/` | Separate from controllers |
| Add controller | `ui/controller/` | Wire in PhotoCategorizerApp |
| Modify constants | `util/StyleConstants.kt` | All UI dimensions here |
| Add test | `src/test/kotlin/` mirror path | Use @Nested, backtick names |

## CONVENTIONS

- **Services hold state**: PhotoService owns photos, CategoryService owns categories
- **Handlers isolate events**: DragDropHandler, SelectionHandler, ReorderDragDropHandler
- **Models are mutable internally**: Category.photos is MutableList, services return copies
- **Original order preserved**: Photo.originalIndex for restoration after category deletion
- **Grid-aware selection**: Shift+click calculates rectangular regions, not linear

## ANTI-PATTERNS

- **UI imports in domain/**: Domain must be pure, no JavaFX deps
- **Direct state mutation**: Services return copies, never expose internal lists
- **Wildcard imports**: Use explicit imports only
- **Type suppression**: No `as Any`, `@Suppress` for type issues

## UNIQUE STYLES

- **Test naming**: Backtick natural language (`should do X when Y`)
- **Test grouping**: @Nested inner classes with @DisplayName
- **KDoc required**: All public classes/functions documented
- **Import order**: domain → javafx → util → java stdlib

## DATA FLOW

```
ZIP → FileService.extractPhotosFromZip() → PhotoService.setPhotos()
Drag to category → DragDropHandler → CategoryService.addPhotoToCategory() + PhotoService.removePhoto()
Reorder in category → ReorderDragDropHandler → CategoryService.reorderPhotoInCategory()
Delete category → PhotoService.restorePhotos() (sorted by originalIndex)
Export → ExportService.exportCategories() → copies to category subdirs
```

## COMMANDS

```bash
./gradlew build          # Build project
./gradlew run            # Run application
./gradlew test           # Run all tests (headless via Monocle)
./gradlew test --tests "ClassName"           # Single test class
./gradlew test --tests "ClassName.InnerClass" # Nested test group
./gradlew clean          # Clean build artifacts
```

## NOTES

- **Headless testing**: TestFX + Monocle for UI tests without display
- **JVM 21 required**: Toolchain enforced in build.gradle.kts
- **Module exports**: Extensive --add-exports for JavaFX internals in tests
- **Dual photo existence**: Photos in PhotoService (grid) AND CategoryService (organization)
- **Destructive export**: ExportService clears target dirs before writing
- **Photo ID format**: `${originalIndex}_${filename}` for uniqueness
