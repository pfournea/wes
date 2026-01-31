# API Reference

## Domain Models

### Photo

Represents a photo with its metadata.

```kotlin
data class Photo(
    val id: String,
    val path: Path,
    val fileName: String,
    val originalIndex: Int
)
```

| Property | Type | Description |
|----------|------|-------------|
| `id` | `String` | Unique identifier (`<index>_<filename>`) |
| `path` | `Path` | Absolute path to the image file |
| `fileName` | `String` | Original filename |
| `originalIndex` | `Int` | Position in original ZIP order |

#### Factory Method

```kotlin
Photo.fromPath(path: Path, index: Int): Photo
```

Creates a Photo from a file path with auto-generated ID.

---

### Category

Represents a category that can contain photos.

```kotlin
data class Category(
    val id: String,
    val number: Int,
    val name: String,
    val photos: MutableList<Photo> = mutableListOf()
)
```

| Property | Type | Description |
|----------|------|-------------|
| `id` | `String` | Unique identifier (`category_<number>`) |
| `number` | `Int` | Category number (1-based) |
| `name` | `String` | Display name |
| `photos` | `MutableList<Photo>` | Photos in this category |

#### Methods

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `addPhoto` | `photo: Photo, position: Int?` | `Unit` | Add photo at position (or end) |
| `removePhoto` | `photo: Photo` | `Unit` | Remove photo from category |
| `containsPhoto` | `photo: Photo` | `Boolean` | Check if photo exists |
| `reorderPhoto` | `photo: Photo, newPosition: Int` | `Boolean` | Move photo to new position within category |
| `getPhotoPosition` | `photo: Photo` | `Int` | Get index of photo (-1 if not found) |

#### Factory Method

```kotlin
Category.create(number: Int): Category
```

Creates a Category with auto-generated ID and name.

---

### Selection

Manages selection state in the photo grid.

```kotlin
data class Selection(
    var anchorIndex: Int? = null,
    val selectedPhotoIds: MutableSet<String> = mutableSetOf()
)
```

| Property | Type | Description |
|----------|------|-------------|
| `anchorIndex` | `Int?` | Anchor point for Shift+Click |
| `selectedPhotoIds` | `MutableSet<String>` | Currently selected photo IDs |

#### Methods

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `setAnchor` | `index: Int` | `Unit` | Set anchor index |
| `clearAnchor` | - | `Unit` | Clear anchor |
| `addSelection` | `photoId: String` | `Unit` | Add to selection |
| `removeSelection` | `photoId: String` | `Unit` | Remove from selection |
| `toggleSelection` | `photoId: String` | `Unit` | Toggle selection state |
| `clearSelection` | - | `Unit` | Clear all selections |
| `isSelected` | `photoId: String` | `Boolean` | Check if selected |
| `hasSelection` | - | `Boolean` | Check if any selected |
| `getSelectionCount` | - | `Int` | Count of selected photos |

---

## Services

### PhotoService

Manages the main photo collection (uncategorized photos).

```kotlin
class PhotoService
```

#### Methods

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `setPhotos` | `photoList: List<Photo>` | `Unit` | Replace photo collection |
| `getPhotos` | - | `List<Photo>` | Get all photos (copy) |
| `getPhotoByIndex` | `index: Int` | `Photo?` | Get photo at index |
| `getPhotoById` | `photoId: String` | `Photo?` | Find photo by ID |
| `getIndexOfPhoto` | `photoId: String` | `Int` | Get index (-1 if not found) |
| `removePhoto` | `photo: Photo` | `Unit` | Remove single photo |
| `removePhotosByIds` | `photoIds: Collection<String>` | `Unit` | Remove multiple photos |
| `getPhotoCount` | - | `Int` | Total photo count |
| `clearPhotos` | - | `Unit` | Remove all photos |
| `getPhotosByIndices` | `indices: List<Int>` | `List<Photo>` | Get photos at indices |

---

### CategoryService

Manages categories and photo assignments.

```kotlin
class CategoryService
```

#### Methods

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `createCategory` | - | `Category` | Create new category (auto-numbered) |
| `getCategories` | - | `List<Category>` | Get all categories (copy) |
| `addPhotoToCategory` | `photo: Photo, category: Category, position: Int?` | `Unit` | Add photo to category |
| `removePhotoFromCategory` | `photo: Photo, category: Category` | `Unit` | Remove photo from category |
| `reorderPhotoInCategory` | `photo: Photo, category: Category, newPosition: Int` | `Boolean` | Move photo to new position within category |
| `findCategoryContainingPhoto` | `photo: Photo` | `Category?` | Find which category has photo |
| `findPhotoById` | `photoId: String` | `Photo?` | Find photo across all categories |
| `getCategoryById` | `categoryId: String` | `Category?` | Find category by ID |
| `clearCategories` | - | `Unit` | Remove all categories |
| `getCategoryCount` | - | `Int` | Total category count |

---

### SelectionService

Manages photo selection logic.

```kotlin
class SelectionService
```

#### Methods

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `handleSingleClick` | `photoIndex: Int, photoId: String` | `SelectionResult` | Process single click |
| `handleCtrlClick` | `photoIndex: Int, photoId: String` | `SelectionResult` | Process Ctrl+Click |
| `handleShiftClick` | `clickedIndex: Int, totalPhotos: Int, columns: Int` | `RangeSelectionResult` | Process Shift+Click |
| `clearSelection` | - | `Unit` | Clear all selection |
| `getSelection` | - | `Selection` | Get selection state |
| `isSelected` | `photoId: String` | `Boolean` | Check if photo selected |
| `getSelectedPhotoIds` | - | `Set<String>` | Get all selected IDs |

#### Result Types

```kotlin
data class SelectionResult(
    val selectedPhotoIds: Set<String>,
    val anchorIndex: Int?
)

data class RangeSelectionResult(
    val selectedIndices: List<Int>
)
```

---

### FileService

Handles file operations and ZIP extraction.

```kotlin
class FileService
```

#### Methods

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `extractPhotosFromZip` | `zipFile: File` | `List<Photo>` | Extract images from ZIP |
| `isImageFile` | `filename: String` | `Boolean` | Check if file is image |
| `getSupportedExtensions` | - | `Set<String>` | Get supported extensions |

#### Supported Extensions

`jpg`, `jpeg`, `png`, `gif`, `bmp`

---

### ExportService

Exports categorized photos to a directory.

```kotlin
class ExportService
```

#### Methods

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `exportCategories` | `categories: List<Category>, targetDirectory: Path` | `ExportResult` | Export all categories |
| `countFilesInDirectory` | `directory: Path` | `Int` | Count files in directory |

#### Result Type

```kotlin
data class ExportResult(
    val success: Boolean,
    val photosCopied: Int,
    val filesDeleted: Int,
    val errors: List<String> = emptyList()
)
```

#### Export Filename Format

`<category_number>_<position_5digits>.<extension>`

Examples:
- `1_00001.jpg` (Category 1, position 1)
- `3_00042.png` (Category 3, position 42)

---

## Utility Classes

### StyleConstants

UI styling constants.

```kotlin
object StyleConstants
```

| Constant | Type | Value | Description |
|----------|------|-------|-------------|
| `SELECTED_BORDER_COLOR` | `String` | `#0077ff` | Selection highlight color |
| `SELECTED_BORDER_WIDTH` | `Int` | `8` | Selection border width |
| `PHOTO_GRID_WIDTH` | `Double` | `200.0` | Photo thumbnail size in grid |
| `PHOTO_CATEGORY_WIDTH` | `Double` | `220.0` | Photo size in category |
| `DEFAULT_COLUMNS` | `Int` | `3` | Default grid columns |
| `DEFAULT_DIVIDER_POSITION` | `Double` | `0.7` | Split pane divider (70/30) |
| `CATEGORY_CARD_WIDTH` | `Double` | `220.0` | Category card width |
| `DRAG_OPACITY` | `Double` | `0.5` | Opacity during drag |

---

### ImageCache

LRU cache for loaded images.

```kotlin
object ImageCache
```

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `get` | `path: String` | `Image?` | Get cached image |
| `put` | `path: String, image: Image` | `Unit` | Cache image |
| `clear` | - | `Unit` | Clear all cached images |

**Cache Size**: 100 images (LRU eviction)

---

### ImageUtils

Image loading and processing utilities.

```kotlin
object ImageUtils
```

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `createImageView` | `photo: Photo, width: Double` | `ImageView` | Create ImageView for photo |
| `loadImage` | `path: Path, width: Double` | `Image` | Load and resize image |
