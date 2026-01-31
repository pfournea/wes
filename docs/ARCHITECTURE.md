# Architecture Documentation

## Overview

Photo Categorizer is a JavaFX desktop application for organizing photos into categories via drag-and-drop. Built with Kotlin and following Domain-Driven Design principles.

## System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                               UI Layer                                       │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                   PhotoCategorizerApp (Orchestrator)                   │  │
│  │  - Initializes all services, controllers, and handlers                │  │
│  │  - Manages JavaFX Stage/Scene lifecycle                               │  │
│  │  - Wires up event handlers to UI components                           │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
│                                    │                                         │
│  ┌─────────────────────────────────┼─────────────────────────────────────┐  │
│  │                          Controllers                                   │  │
│  │  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐          │  │
│  │  │ Layout     │ │ PhotoGrid  │ │ Category   │ │ Upload     │          │  │
│  │  │ Controller │ │ Controller │ │ Controller │ │ Controller │          │  │
│  │  └────────────┘ └────────────┘ └────────────┘ └────────────┘          │  │
│  │                               ┌────────────┐                           │  │
│  │                               │ Export     │                           │  │
│  │                               │ Controller │                           │  │
│  │                               └────────────┘                           │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
│                                    │                                         │
│  ┌─────────────────────────────────┼─────────────────────────────────────┐  │
│  │                           Components                                   │  │
│  │  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐          │  │
│  │  │ Category   │ │ PhotoCard  │ │ Button     │ │ AddCategory│          │  │
│  │  │ Card       │ │            │ │ Factory    │ │ Dialog     │          │  │
│  │  └────────────┘ └────────────┘ └────────────┘ └────────────┘          │  │
│  │                               ┌────────────┐                           │  │
│  │                               │ HelpDialog │                           │  │
│  │                               └────────────┘                           │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
│                                    │                                         │
│  ┌─────────────────────────────────┼─────────────────────────────────────┐  │
│  │                            Handlers                                    │  │
│  │  ┌────────────────┐  ┌────────────────┐  ┌────────────────────────┐   │  │
│  │  │SelectionHandler│  │DragDropHandler │  │ReorderDragDropHandler │   │  │
│  │  └────────────────┘  └────────────────┘  └────────────────────────┘   │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Domain Layer                                    │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                            Services                                    │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐                 │  │
│  │  │ PhotoService │  │CategoryService│  │SelectionSvc  │                 │  │
│  │  │              │  │              │  │              │                 │  │
│  │  │ Owns:        │  │ Owns:        │  │ Owns:        │                 │  │
│  │  │ - Uncateg.   │  │ - Categories │  │ - Selection  │                 │  │
│  │  │   photos     │  │ - Photo→Cat  │  │   state      │                 │  │
│  │  │              │  │   mappings   │  │ - Anchor     │                 │  │
│  │  └──────────────┘  └──────────────┘  └──────────────┘                 │  │
│  │                                                                        │  │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐                 │  │
│  │  │ FileService  │  │ExportService │  │RotationSvc   │                 │  │
│  │  │              │  │              │  │              │                 │  │
│  │  │ - ZIP        │  │ - File copy  │  │ - Rotate CW  │                 │  │
│  │  │   extraction │  │ - Renaming   │  │ - Rotate CCW │                 │  │
│  │  └──────────────┘  └──────────────┘  └──────────────┘                 │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
│                                    │                                         │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                             Models                                     │  │
│  │  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐             │  │
│  │  │    Photo     │    │   Category   │    │  Selection   │             │  │
│  │  │  (data)      │    │    (data)    │    │   (data)     │             │  │
│  │  └──────────────┘    └──────────────┘    └──────────────┘             │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                               Util Layer                                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │  ImageCache  │  │  ImageUtils  │  │StyleConstants│  │    Icons     │     │
│  │  (LRU cache) │  │  (loading)   │  │(design system)│  │  (unicode)   │     │
│  └──────────────┘  └──────────────┘  └──────────────┘  └──────────────┘     │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Package Structure

```
src/main/kotlin/
├── Main.kt                     # Entry point
├── domain/
│   ├── model/                  # Pure data classes (no dependencies)
│   │   ├── Photo.kt
│   │   ├── Category.kt
│   │   └── Selection.kt
│   └── service/                # Business logic (no UI deps)
│       ├── PhotoService.kt
│       ├── CategoryService.kt
│       ├── SelectionService.kt
│       ├── FileService.kt
│       ├── ExportService.kt
│       └── RotationService.kt  # Photo rotation logic
├── ui/
│   ├── PhotoCategorizerApp.kt  # Main Application
│   ├── component/              # Reusable UI components
│   │   ├── CategoryCard.kt
│   │   ├── PhotoCard.kt        # Photo display with controls
│   │   ├── ButtonFactory.kt    # Consistent button creation
│   │   ├── AddCategoryDialog.kt
│   │   └── HelpDialog.kt
│   ├── controller/             # UI region controllers
│   │   ├── LayoutController.kt
│   │   ├── PhotoGridController.kt
│   │   ├── CategoryController.kt
│   │   ├── UploadController.kt
│   │   └── ExportController.kt
│   └── handler/                # Event handlers
│       ├── DragDropHandler.kt
│       ├── ReorderDragDropHandler.kt
│       └── SelectionHandler.kt
└── util/
    ├── ImageCache.kt
    ├── ImageUtils.kt
    ├── Icons.kt                # Unicode icon constants
    └── StyleConstants.kt       # Enterprise design system
```

## Design Decisions

### 1. Service-Owned State

Each service owns its domain state:

| Service | Owns | Responsibility |
|---------|------|----------------|
| `PhotoService` | `List<Photo>` | Uncategorized photos (main pool) |
| `CategoryService` | `List<Category>` | Categories and their photos |
| `SelectionService` | `Selection` | Current selection state |
| `FileService` | - | ZIP extraction (stateless) |
| `ExportService` | - | File export (stateless) |
| `RotationService` | - | Photo rotation (stateless) |

**Rationale**: Clear ownership prevents state synchronization bugs. When a photo moves from main pool to a category, `PhotoService.removePhoto()` and `CategoryService.addPhotoToCategory()` are called atomically.

### 2. Handler Pattern for UI Events

Complex UI event logic is encapsulated in handler classes:

- **SelectionHandler**: Click, Ctrl+Click, Shift+Click logic
- **DragDropHandler**: Drag detection, drop handling for moving photos to categories
- **ReorderDragDropHandler**: Reordering photos within category view via drag-drop

**Rationale**: Keeps `PhotoCategorizerApp` focused on orchestration. Handlers are reusable and testable.

### 3. Mutable Internals, Immutable Returns

```kotlin
// Category.kt
data class Category(
    val photos: MutableList<Photo> = mutableListOf()  // Mutable internally
)

// PhotoService.kt
fun getPhotos(): List<Photo> = photos.toList()  // Returns copy
```

**Rationale**: Services can mutate efficiently, but callers can't accidentally corrupt state.

### 4. Factory Methods over Constructors

```kotlin
// Photo.kt
companion object {
    fun fromPath(path: Path, index: Int): Photo { ... }
}

// Category.kt
companion object {
    fun create(number: Int): Category { ... }
}
```

**Rationale**: Encapsulates ID generation logic. Constructor stays simple for testing.

## Data Flow

### Loading Photos

```
User selects ZIP
      │
      ▼
FileService.extractPhotosFromZip()
      │
      ├── Extract to temp directory
      ├── Filter image files
      └── Create Photo objects
      │
      ▼
PhotoService.setPhotos()
      │
      ▼
UI creates ImageViews → Display in grid
```

### Drag-and-Drop to Category

```
User drags photo(s)
      │
      ▼
DragDropHandler.handleDragDetected()
      │
      ├── Capture selected photos
      └── Set dragboard content
      │
      ▼
User drops on CategoryCard
      │
      ▼
DragDropHandler.handleDragDropped()
      │
      ├── CategoryService.addPhotoToCategory()
      ├── PhotoService.removePhoto()  (if from main pool)
      └── Refresh UI
```

### Reorder Photos Within Category

```
User views category (clicks eye icon)
      │
      ▼
Photos displayed in left pane
      │
      ▼
User drags photo to new position
      │
      ▼
ReorderDragDropHandler.handleDragOver()
      │
      └── Show drop indicator
      │
      ▼
User drops photo
      │
      ▼
ReorderDragDropHandler.handleDragDropped()
      │
      ├── CategoryService.reorderPhotoInCategory()
      └── Refresh category view
```

Note: When viewing a category, photos can also be dragged to other category cards (cross-category move), which uses DragDropHandler.

### Delete Category (Restore Original Order)

```
User deletes category
      │
      ▼
PhotoService.restorePhotos(category.photos)
      │
      ├── Add photos back to main pool
      └── Sort by Photo.originalIndex
      │
      ▼
Photos appear in original order
```

### Export

```
User clicks "Save Images"
      │
      ▼
ExportService.exportCategories()
      │
      ├── Clean target directory
      ├── For each category:
      │     For each photo:
      │       └── Copy with renamed filename
      └── Return ExportResult
      │
      ▼
Show success/error dialog
```

## File Naming Convention (Export)

Format: `<category_number>_<position_5digits>.<extension>`

Examples:
- Category 1, position 1: `1_00001.jpg`
- Category 3, position 42: `3_00042.png`

## Selection Model

### Selection States

| Action | Result |
|--------|--------|
| Click | Clear selection, select clicked, set anchor |
| Ctrl+Click | Toggle clicked photo, update anchor |
| Shift+Click | Select rectangular range from anchor |

### Shift+Click Range Selection

The selection is **row-aware** (not just index range):

```
Grid (5 columns):
[0] [1] [2] [3] [4]
[5] [6] [7] [8] [9]

Anchor at 2, Shift+Click at 7:
Selected: [2] [3] [4] [5] [6] [7]  (columns 2-4 in row 0, columns 0-2 in row 1)
```

## Caching Strategy

`ImageCache` uses LRU eviction:

```kotlin
object ImageCache {
    private const val MAX_CACHE_SIZE = 100
    private val cache = LinkedHashMap<String, Image>(16, 0.75f, true)
}
```

- Key: Photo path
- Value: JavaFX `Image`
- Eviction: Oldest accessed when > 100 entries
- Cleared on new ZIP load

## Threading Model

- **UI Thread**: All JavaFX operations
- **Background Thread**: Export operation (using `javafx.concurrent.Task`)

```kotlin
val exportTask = object : Task<ExportResult>() {
    override fun call(): ExportResult { ... }
}
// Progress bound to UI
progressBar.progressProperty().bind(exportTask.progressProperty())
```

## Error Handling

- **File I/O**: Caught and collected in result objects (e.g., `ExportResult.errors`)
- **User Feedback**: Alert dialogs for warnings/errors
- **Graceful Degradation**: Invalid indices return `null`, not exceptions
