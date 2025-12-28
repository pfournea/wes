package ui.handler

import javafx.scene.input.MouseButton
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("DragDropHandler UI Tests")
class DragDropHandlerUITest : DragDropTestBase() {

    @Nested
    @DisplayName("Single Photo Drag-Drop")
    inner class SinglePhotoDragDropTests {

        @Test
        fun `should drag single photo from grid to category`() {
            val photo = createTestPhoto(0)
            val imageView = addPhotoToGrid(photo)
            val categoryCard = createCategory()

            performDragDrop(imageView, categoryCard)

            val category = categoryService.getCategoryById("category_1")!!
            assertEquals(1, category.photos.size)
            assertEquals(photo.id, category.photos[0].id)
        }

        @Test
        fun `should remove photo from PhotoService after drop to category`() {
            val photo = createTestPhoto(0)
            val imageView = addPhotoToGrid(photo)
            val categoryCard = createCategory()
            assertEquals(1, photoService.getPhotos().size)

            performDragDrop(imageView, categoryCard)

            assertEquals(0, photoService.getPhotos().size)
        }

        @Test
        fun `should not accept drop if photo already in category`() {
            val photo = createTestPhoto(0)
            val imageView = addPhotoToGrid(photo)
            val categoryCard = createCategory()

            performDragDrop(imageView, categoryCard)

            val category = categoryService.getCategoryById("category_1")!!
            assertEquals(1, category.photos.size)

            interact {
                sharedImageViews.clear()
                imageContainer.children.clear()
            }
            
            val newImageView = addPhotoToGrid(photo)
            performDragDrop(newImageView, categoryCard)

            val updatedCategory = categoryService.getCategoryById("category_1")!!
            assertEquals(1, updatedCategory.photos.size)
        }
    }

    @Nested
    @DisplayName("Multi-Select Drag-Drop")
    inner class MultiSelectDragDropTests {

        @Test
        fun `should drag multiple selected photos to category`() {
            val photo1 = createTestPhoto(0)
            val photo2 = createTestPhoto(1)
            val photo3 = createTestPhoto(2)

            val imageView1 = addPhotoToGrid(photo1)
            val imageView2 = addPhotoToGrid(photo2)
            val imageView3 = addPhotoToGrid(photo3)

            val categoryCard = createCategory()

            selectPhotos(photo1.id, photo2.id)

            performDragDrop(imageView1, categoryCard)

            val category = categoryService.getCategoryById("category_1")!!
            assertEquals(2, category.photos.size)
            assertTrue(category.photos.any { it.id == photo1.id })
            assertTrue(category.photos.any { it.id == photo2.id })
        }

        @Test
        fun `should remove all dragged photos from PhotoService`() {
            val photo1 = createTestPhoto(0)
            val photo2 = createTestPhoto(1)

            val imageView1 = addPhotoToGrid(photo1)
            val imageView2 = addPhotoToGrid(photo2)

            val categoryCard = createCategory()
            assertEquals(2, photoService.getPhotos().size)

            selectPhotos(photo1.id, photo2.id)

            performDragDrop(imageView1, categoryCard)

            assertEquals(0, photoService.getPhotos().size)
            val category = categoryService.getCategoryById("category_1")!!
            assertEquals(2, category.photos.size)
        }

        @Test
        fun `should only drag clicked photo when not in selection`() {
            val photo1 = createTestPhoto(0)
            val photo2 = createTestPhoto(1)
            val photo3 = createTestPhoto(2)

            val imageView1 = addPhotoToGrid(photo1)
            val imageView2 = addPhotoToGrid(photo2)
            val imageView3 = addPhotoToGrid(photo3)

            val categoryCard = createCategory()

            selectPhotos(photo1.id, photo2.id)

            performDragDrop(imageView3, categoryCard)

            val category = categoryService.getCategoryById("category_1")!!
            assertEquals(1, category.photos.size)
            assertEquals(photo3.id, category.photos[0].id)
        }
    }
}
