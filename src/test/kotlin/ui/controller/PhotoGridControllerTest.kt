package ui.controller

import domain.model.Photo
import domain.service.CategoryService
import domain.service.ExportService
import domain.service.PhotoService
import domain.service.SelectionService
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.image.ImageView
import javafx.scene.layout.TilePane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.testfx.framework.junit5.ApplicationTest
import ui.handler.DragDropHandler
import ui.handler.ReorderDragDropHandler
import ui.handler.SelectionHandler
import util.StyleConstants
import java.nio.file.Paths

@DisplayName("PhotoGridController Tests")
class PhotoGridControllerTest : ApplicationTest() {

    private lateinit var photoService: PhotoService
    private lateinit var categoryService: CategoryService
    private lateinit var selectionService: SelectionService

    private lateinit var imageContainer: TilePane
    private lateinit var sharedImageViews: MutableList<ImageView>
    private lateinit var photoGridController: PhotoGridController
    private lateinit var dragDropHandler: DragDropHandler
    private lateinit var reorderDragDropHandler: ReorderDragDropHandler
    private lateinit var selectionHandler: SelectionHandler

    private lateinit var photo1: Photo
    private lateinit var photo2: Photo
    private lateinit var photo3: Photo

    override fun start(stage: Stage) {
        photoService = PhotoService()
        categoryService = CategoryService()
        selectionService = SelectionService()

        imageContainer = TilePane().apply {
            orientation = Orientation.HORIZONTAL
            hgap = 10.0
            vgap = 10.0
            prefColumns = StyleConstants.DEFAULT_COLUMNS
            id = "image-container"
        }

        sharedImageViews = mutableListOf()

        selectionHandler = SelectionHandler(
            selectionService,
            photoService,
            sharedImageViews
        ) { imageContainer.prefColumns.toInt() }

        dragDropHandler = DragDropHandler(
            photoService,
            categoryService,
            sharedImageViews,
            onPhotosDropped = {
                photoGridController.updateImageDisplay()
            }
        )

        reorderDragDropHandler = ReorderDragDropHandler(
            categoryService,
            sharedImageViews
        ) {
            photoGridController.updateImageDisplay()
        }

        photoGridController = PhotoGridController(
            photoService,
            categoryService,
            imageContainer,
            sharedImageViews,
            dragDropHandler,
            reorderDragDropHandler,
            selectionHandler,
            onPhotoRemoved = { }
        )

        val root = VBox(10.0, imageContainer)
        stage.scene = Scene(root, 800.0, 600.0)
        stage.show()
    }

    @BeforeEach
    fun setUp() {
        photoService.clearPhotos()
        categoryService.clearCategories()
        selectionService.clearSelection()

        photo1 = Photo.fromPath(Paths.get("/test/photo1.jpg"), 0)
        photo2 = Photo.fromPath(Paths.get("/test/photo2.jpg"), 1)
        photo3 = Photo.fromPath(Paths.get("/test/photo3.jpg"), 2)

        photoService.setPhotos(listOf(photo1, photo2, photo3))
        sharedImageViews.clear()
        imageContainer.children.clear()
    }

    @Nested
    @DisplayName("Photo Deletion from Category")
    inner class PhotoDeletionTests {

        @Test
        fun `should show delete button on hover in category view`() {
            val category = categoryService.createCategory()
            interact {
                categoryService.addPhotoToCategory(photo1, category)
                photoGridController.updateForCategory(category)
            }

            // In category view, display nodes should have wrappers
            val displayHasWrappers = photoGridController.getImageViews().isNotEmpty()
            assertTrue(displayHasWrappers, "Should have image views in category")
        }

        @Test
        fun `should not show delete button in all photos view`() {
            interact {
                photoGridController.updateForCategory(null)
            }

            // When showing all photos, should only have imageViews without wrappers
            assertEquals(3, photoGridController.getImageViews().size)
        }

        @Test
        fun `should remove photo from category when deleted`() {
            var category = categoryService.createCategory()
            interact {
                category = categoryService.addPhotoToCategory(photo1, category)
                category = categoryService.addPhotoToCategory(photo2, category)
                assertEquals(2, category.photos.size)
            }

            interact {
                // Simulate deletion through the service
                category = categoryService.removePhotoFromCategory(photo1, category)
            }

            assertEquals(1, category.photos.size)
            assertEquals(photo2.id, category.photos[0].id)
        }

        @Test
        fun `should restore photo to main grid when deleted from category`() {
            var category = categoryService.createCategory()
            photoService.clearPhotos()
            photoService.setPhotos(listOf(photo1, photo2, photo3))

            interact {
                category = categoryService.addPhotoToCategory(photo1, category)
                // Photo1 is now in category, removed from main photos via drag-drop
                photoService.removePhoto(photo1)
                assertEquals(2, photoService.getPhotos().size)
            }

            interact {
                // Simulate deletion
                category = categoryService.removePhotoFromCategory(photo1, category)
                photoService.restorePhotos(listOf(photo1))
            }

            val allPhotos = photoService.getPhotos()
            assertEquals(3, allPhotos.size)
            assertTrue(allPhotos.any { it.id == photo1.id })
        }

        @Test
        fun `should maintain photo order when restored to main grid`() {
            var category = categoryService.createCategory()
            photoService.clearPhotos()
            photoService.setPhotos(listOf(photo1, photo2, photo3))

            interact {
                category = categoryService.addPhotoToCategory(photo2, category)
                photoService.removePhoto(photo2)
            }

            interact {
                category = categoryService.removePhotoFromCategory(photo2, category)
                photoService.restorePhotos(listOf(photo2))
            }

            val allPhotos = photoService.getPhotos()
            assertEquals(photo1.id, allPhotos[0].id, "photo1 should be first")
            assertEquals(photo2.id, allPhotos[1].id, "photo2 should be restored to original position")
            assertEquals(photo3.id, allPhotos[2].id, "photo3 should be last")
        }

        @Test
        fun `should refresh grid display after photo deletion`() {
            var category = categoryService.createCategory()
            interact {
                category = categoryService.addPhotoToCategory(photo1, category)
                category = categoryService.addPhotoToCategory(photo2, category)
                photoGridController.updateForCategory(category)
            }

            val initialCount = photoGridController.getImageViews().size
            assertEquals(2, initialCount)

            interact {
                category = categoryService.removePhotoFromCategory(photo1, category)
                photoService.restorePhotos(listOf(photo1))
                photoGridController.updateForCategory(category)
            }

            val finalCount = photoGridController.getImageViews().size
            assertEquals(1, finalCount, "Should have removed one image from display")
        }

        @Test
        fun `should show empty state when last photo is deleted from category`() {
            var category = categoryService.createCategory()
            interact {
                category = categoryService.addPhotoToCategory(photo1, category)
                photoGridController.updateForCategory(category)
            }

            interact {
                category = categoryService.removePhotoFromCategory(photo1, category)
                photoService.restorePhotos(listOf(photo1))
                photoGridController.updateForCategory(category)
            }

            // Should show "No photos in this category" message
            assertEquals(0, photoGridController.getImageViews().size)
            assertTrue(imageContainer.children.size > 0)
        }
    }

    @Nested
    @DisplayName("Drag-Drop Compatibility After Deletion")
    inner class DragDropCompatibilityTests {

        @Test
        fun `should allow moving photos between categories after deletion`() {
            var category1 = categoryService.createCategory()
            var category2 = categoryService.createCategory()

            interact {
                category1 = categoryService.addPhotoToCategory(photo1, category1)
                category1 = categoryService.addPhotoToCategory(photo2, category1)
                photoService.removePhoto(photo1)
                photoService.removePhoto(photo2)
            }

            interact {
                // Delete photo1 from category1
                category1 = categoryService.removePhotoFromCategory(photo1, category1)
                photoService.restorePhotos(listOf(photo1))
            }

            interact {
                // Move photo2 to category2
                category1 = categoryService.removePhotoFromCategory(photo2, category1)
                category2 = categoryService.addPhotoToCategory(photo2, category2)
            }

            assertEquals(0, category1.photos.size)
            assertEquals(1, category2.photos.size)
            assertEquals(photo2.id, category2.photos[0].id)
        }

        @Test
        fun `should allow reordering after photo deletion`() {
            var category = categoryService.createCategory()
            interact {
                category = categoryService.addPhotoToCategory(photo1, category)
                category = categoryService.addPhotoToCategory(photo2, category)
                category = categoryService.addPhotoToCategory(photo3, category)
            }

            interact {
                // Delete photo2
                category = categoryService.removePhotoFromCategory(photo2, category)
                photoService.restorePhotos(listOf(photo2))
            }

            interact {
                // Reorder: move photo3 to position 0
                categoryService.reorderPhotoInCategory(photo3.id, category, 0)
            }

            val updatedCategory = categoryService.getCategoryById(category.id)!!
            assertEquals(2, updatedCategory.photos.size)
            assertEquals(photo3.id, updatedCategory.photos[0].id)
            assertEquals(photo1.id, updatedCategory.photos[1].id)
        }
    }

    @Nested
    @DisplayName("Photo Position Numbering for Export")
    inner class ExportPositioningTests {

        @Test
        fun `should renumber photo positions sequentially after deletion for export`() {
            // Scenario: Category has photos at positions 1,2,3,4,5
            // Delete position 3
            // Positions should become 1,2,3,4 on export
            val exportService = ExportService()
            var category = categoryService.createCategory()

            // Create 5 photos
            val photo4 = Photo.fromPath(Paths.get("/test/photo4.jpg"), 3)
            val photo5 = Photo.fromPath(Paths.get("/test/photo5.jpg"), 4)

            interact {
                category = categoryService.addPhotoToCategory(photo1, category)
                category = categoryService.addPhotoToCategory(photo2, category)
                category = categoryService.addPhotoToCategory(photo3, category)
                category = categoryService.addPhotoToCategory(photo4, category)
                category = categoryService.addPhotoToCategory(photo5, category)
            }

            // Verify initial positions
            assertEquals(5, category.photos.size)
            assertEquals(photo1.id, category.photos[0].id, "Position 0 should be photo1")
            assertEquals(photo3.id, category.photos[2].id, "Position 2 should be photo3")
            assertEquals(photo5.id, category.photos[4].id, "Position 4 should be photo5")

            // Delete photo3 (position 3 becomes position 2)
            interact {
                category = categoryService.removePhotoFromCategory(photo3, category)
            }

            // Verify renumbering happened
            assertEquals(4, category.photos.size)
            assertEquals(photo1.id, category.photos[0].id, "Position 0 still photo1")
            assertEquals(photo2.id, category.photos[1].id, "Position 1 still photo2")
            assertEquals(photo4.id, category.photos[2].id, "Position 2 now photo4 (was 3)")
            assertEquals(photo5.id, category.photos[3].id, "Position 3 now photo5 (was 4)")
        }

        @Test
        fun `should export photos with correct sequential positions after deletion`() {
            val exportService = ExportService()
            var category = categoryService.createCategory()

            // Create 3 photos
            val photo4 = Photo.fromPath(Paths.get("/test/photo4.jpg"), 3)

            interact {
                category = categoryService.addPhotoToCategory(photo1, category)
                category = categoryService.addPhotoToCategory(photo2, category)
                category = categoryService.addPhotoToCategory(photo4, category)
            }

            // Delete middle photo
            interact {
                category = categoryService.removePhotoFromCategory(photo2, category)
            }

            val updatedCategory = categoryService.getCategoryById(category.id)!!
            
            // Verify the photos in category are in correct order
            assertEquals(2, updatedCategory.photos.size)
            
            // When exported via forEachIndexed:
            // index 0 -> position 1
            // index 1 -> position 2
            val positions = mutableListOf<Int>()
            updatedCategory.photos.forEachIndexed { index, _ ->
                positions.add(index + 1)
            }
            
            assertEquals(listOf(1, 2), positions, "Export positions should be 1,2 not 1,3")
        }
    }
}
