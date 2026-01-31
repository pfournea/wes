# API Reference

## Domain Models

### Photo

Represents a photo with its metadata.

```kotlin
data class Photo(
    val id: String,
    val path: Path,
    val fileName: String,
    val originalIndex: Int,
    val rotationDegrees: Int = 0  // Rotation in degrees: 0, 90, 180, 270
)
```

| Property | Type | Description |
|----------|------|-------------|
| `id` | `String` | Unique identifier (`<index>_<filename>`) |
| `path` | `Path` | Absolute path to the image file |
| `fileName` | `String` | Original filename |
| `originalIndex` | `Int` | Position in original ZIP order |
| `rotationDegrees` | `Int` | Rotation angle (0, 90, 180, 270), default 0 |

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

### RotationService

Manages photo rotation state.

```kotlin
class RotationService
```

#### Methods

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `rotateClockwise` | `photo: Photo` | `Photo` | Rotate 90° clockwise, returns new Photo |
| `rotateCounterClockwise` | `photo: Photo` | `Photo` | Rotate 90° counter-clockwise, returns new Photo |
| `resetRotation` | `photo: Photo` | `Photo` | Reset rotation to 0°, returns new Photo |

---

## Utility Classes

### StyleConstants

Enterprise design system constants inspired by modern UI frameworks (Linear, Notion, Figma).

```kotlin
object StyleConstants
```

#### Color Palette

| Category | Constants | Description |
|----------|-----------|-------------|
| Primary Blue | `PRIMARY_50` - `PRIMARY_700` | Primary actions and focus states |
| Neutral Gray | `NEUTRAL_50` - `NEUTRAL_900` | Text, borders, backgrounds |
| Success | `SUCCESS_50`, `SUCCESS_500`, `SUCCESS_600` | Success states |
| Warning | `WARNING_50`, `WARNING_500`, `WARNING_600` | Warning states |
| Danger | `DANGER_50`, `DANGER_500`, `DANGER_600` | Error/danger states |

#### Semantic Colors

| Constant | Value | Description |
|----------|-------|-------------|
| `BACKGROUND_PRIMARY` | `#ffffff` | Main background |
| `BACKGROUND_SECONDARY` | `NEUTRAL_50` | Secondary background |
| `TEXT_PRIMARY` | `NEUTRAL_800` | Primary text color |
| `TEXT_SECONDARY` | `NEUTRAL_500` | Secondary text color |
| `BORDER_DEFAULT` | `NEUTRAL_200` | Default border color |
| `BORDER_FOCUS` | `PRIMARY_500` | Focus state border |

#### Typography

| Constant | Value | Description |
|----------|-------|-------------|
| `FONT_FAMILY` | SF Pro/Segoe UI/Roboto | System font stack |
| `FONT_SIZE_XS` - `FONT_SIZE_3XL` | 11.0 - 24.0 | Font size scale |

#### Spacing Scale

| Constant | Value | Description |
|----------|-------|-------------|
| `SPACING_XS` | 4.0 | Extra small spacing |
| `SPACING_SM` | 8.0 | Small spacing |
| `SPACING_MD` | 12.0 | Medium spacing |
| `SPACING_BASE` | 16.0 | Base spacing |
| `SPACING_LG` - `SPACING_3XL` | 20.0 - 40.0 | Large spacing |

#### Shadows/Elevation

| Constant | Description |
|----------|-------------|
| `SHADOW_XS` - `SHADOW_XL` | Subtle drop shadows |
| `ELEVATION_1` - `ELEVATION_4` | Legacy shadow aliases |

#### Layout Dimensions

| Constant | Value | Description |
|----------|-------|-------------|
| `PHOTO_GRID_WIDTH` | 180.0 | Photo thumbnail size |
| `PHOTO_CATEGORY_WIDTH` | 200.0 | Photo size in category view |
| `DEFAULT_COLUMNS` | 4 | Default grid columns |
| `GRID_GAP` | 16.0 | Gap between grid items |
| `DEFAULT_DIVIDER_POSITION` | 0.72 | Split pane divider (72/28) |
| `CATEGORY_CARD_WIDTH` | 260.0 | Category card width |

#### Interaction

| Constant | Value | Description |
|----------|-------|-------------|
| `DRAG_OPACITY` | 0.7 | Opacity during drag |
| `CONTROL_BUTTON_SIZE` | 32.0 | Control button dimensions |
| `ANIMATION_FAST/NORMAL/SLOW` | 100/200/300 ms | Animation durations |

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

---

### Icons

Unicode icon constants for consistent UI without external dependencies.

```kotlin
object Icons
```

| Category | Icons |
|----------|-------|
| Actions | `UPLOAD`, `DOWNLOAD`, `SAVE`, `ADD`, `REMOVE`, `CLOSE`, `CHECK`, `SEARCH` |
| View | `VIEW`, `HIDE`, `EXPAND`, `COLLAPSE`, `GRID`, `LIST` |
| Media | `FOLDER`, `FILE`, `IMAGE`, `PHOTO_STACK` |
| Rotation | `ROTATE_LEFT` (↺), `ROTATE_RIGHT` (↻) |
| Status | `INFO`, `WARNING`, `ERROR`, `SUCCESS` |
| Arrows | `ARROW_LEFT`, `ARROW_RIGHT`, `ARROW_UP`, `ARROW_DOWN` |
| Category | `CATEGORY`, `TAG`, `ORGANIZE` |
| Misc | `SETTINGS`, `MENU`, `MORE`, `DRAG` |
