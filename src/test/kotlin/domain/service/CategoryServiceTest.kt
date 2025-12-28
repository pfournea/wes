package domain.service

import domain.model.Category
import domain.model.Photo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import java.nio.file.Paths

@DisplayName("CategoryService Tests")
class CategoryServiceTest {

    private lateinit var categoryService: CategoryService
    private lateinit var photo1: Photo
    private lateinit var photo2: Photo
    private lateinit var photo3: Photo

    @BeforeEach
    fun setUp() {
        categoryService = CategoryService()
        photo1 = Photo.fromPath(Paths.get("/test/photo1.jpg"), 0)
        photo2 = Photo.fromPath(Paths.get("/test/photo2.jpg"), 1)
        photo3 = Photo.fromPath(Paths.get("/test/photo3.jpg"), 2)
    }

    @Nested
    @DisplayName("Category Creation")
    inner class CategoryCreationTests {

        @Test
        fun `should create category with auto-incremented number`() {
            val category1 = categoryService.createCategory()
            
            assertEquals("category_1", category1.id)
            assertEquals("Category 1", category1.name)
        }

        @Test
        fun `should create multiple categories with sequential numbers`() {
            val category1 = categoryService.createCategory()
            val category2 = categoryService.createCategory()
            val category3 = categoryService.createCategory()
            
            assertEquals("category_1", category1.id)
            assertEquals("category_2", category2.id)
            assertEquals("category_3", category3.id)
        }

        @Test
        fun `should track created categories`() {
            categoryService.createCategory()
            categoryService.createCategory()
            
            assertEquals(2, categoryService.getCategoryCount())
            assertEquals(2, categoryService.getCategories().size)
        }
    }

    @Nested
    @DisplayName("Photo Management")
    inner class PhotoManagementTests {

        @Test
        fun `should add photo to category`() {
            var category = categoryService.createCategory()
            
            category = categoryService.addPhotoToCategory(photo1, category)
            
            assertTrue(category.containsPhoto(photo1))
            assertEquals(1, category.photos.size)
        }

        @Test
        fun `should add photo to category at specific position`() {
            var category = categoryService.createCategory()
            category = categoryService.addPhotoToCategory(photo1, category)
            category = categoryService.addPhotoToCategory(photo2, category)
            
            category = categoryService.addPhotoToCategory(photo3, category, position = 1)
            
            assertEquals(3, category.photos.size)
            assertEquals(photo1, category.photos[0])
            assertEquals(photo3, category.photos[1])
            assertEquals(photo2, category.photos[2])
        }

        @Test
        fun `should add photo at end when position is null`() {
            var category = categoryService.createCategory()
            category = categoryService.addPhotoToCategory(photo1, category)
            category = categoryService.addPhotoToCategory(photo2, category)
            
            category = categoryService.addPhotoToCategory(photo3, category, position = null)
            
            assertEquals(3, category.photos.size)
            assertEquals(photo3, category.photos[2])
        }

        @Test
        fun `should remove photo from category`() {
            var category = categoryService.createCategory()
            category = categoryService.addPhotoToCategory(photo1, category)
            category = categoryService.addPhotoToCategory(photo2, category)
            
            category = categoryService.removePhotoFromCategory(photo1, category)
            
            assertFalse(category.containsPhoto(photo1))
            assertTrue(category.containsPhoto(photo2))
            assertEquals(1, category.photos.size)
        }

        @Test
        fun `should handle removing non-existent photo`() {
            var category = categoryService.createCategory()
            category = categoryService.addPhotoToCategory(photo1, category)
            
            category = categoryService.removePhotoFromCategory(photo2, category)
            
            assertEquals(1, category.photos.size)
        }
    }

    @Nested
    @DisplayName("Category Lookup")
    inner class CategoryLookupTests {

        @Test
        fun `should find category containing photo`() {
            var category1 = categoryService.createCategory()
            var category2 = categoryService.createCategory()
            
            category1 = categoryService.addPhotoToCategory(photo1, category1)
            category2 = categoryService.addPhotoToCategory(photo2, category2)
            
            val foundCategory = categoryService.findCategoryContainingPhoto(photo2)
            
            assertNotNull(foundCategory)
            assertEquals(category2.id, foundCategory?.id)
        }

        @Test
        fun `should return null when photo is not in any category`() {
            var category = categoryService.createCategory()
            category = categoryService.addPhotoToCategory(photo1, category)
            
            val foundCategory = categoryService.findCategoryContainingPhoto(photo2)
            
            assertNull(foundCategory)
        }

        @Test
        fun `should get category by id`() {
            categoryService.createCategory()
            val category2 = categoryService.createCategory()
            
            val found = categoryService.getCategoryById("category_2")
            
            assertNotNull(found)
            assertEquals(category2.id, found?.id)
        }

        @Test
        fun `should return null when category id does not exist`() {
            categoryService.createCategory()
            
            val found = categoryService.getCategoryById("category_99")
            
            assertNull(found)
        }

        @Test
        fun `should get all categories`() {
            val category1 = categoryService.createCategory()
            val category2 = categoryService.createCategory()
            val category3 = categoryService.createCategory()
            
            val allCategories = categoryService.getCategories()
            
            assertEquals(3, allCategories.size)
            assertTrue(allCategories.any { it.id == category1.id })
            assertTrue(allCategories.any { it.id == category2.id })
            assertTrue(allCategories.any { it.id == category3.id })
        }
    }

    @Nested
    @DisplayName("Category State Management")
    inner class CategoryStateTests {

        @Test
        fun `should clear all categories`() {
            categoryService.createCategory()
            categoryService.createCategory()
            
            categoryService.clearCategories()
            
            assertEquals(0, categoryService.getCategoryCount())
            assertTrue(categoryService.getCategories().isEmpty())
        }

        @Test
        fun `should return correct category count`() {
            assertEquals(0, categoryService.getCategoryCount())
            
            categoryService.createCategory()
            assertEquals(1, categoryService.getCategoryCount())
            
            categoryService.createCategory()
            assertEquals(2, categoryService.getCategoryCount())
        }

        @Test
        fun `should reset numbering after clearing categories`() {
            categoryService.createCategory()
            categoryService.createCategory()
            categoryService.clearCategories()
            
            val newCategory = categoryService.createCategory()
            
            assertEquals("category_1", newCategory.id)
        }
    }

    @Nested
    @DisplayName("Complex Scenarios")
    inner class ComplexScenarioTests {

        @Test
        fun `should move photo between categories`() {
            var category1 = categoryService.createCategory()
            var category2 = categoryService.createCategory()
            
            category1 = categoryService.addPhotoToCategory(photo1, category1)
            assertTrue(category1.containsPhoto(photo1))
            
            category1 = categoryService.removePhotoFromCategory(photo1, category1)
            category2 = categoryService.addPhotoToCategory(photo1, category2)
            
            assertFalse(category1.containsPhoto(photo1))
            assertTrue(category2.containsPhoto(photo1))
        }

        @Test
        fun `should handle multiple photos in multiple categories`() {
            var category1 = categoryService.createCategory()
            var category2 = categoryService.createCategory()
            
            category1 = categoryService.addPhotoToCategory(photo1, category1)
            category1 = categoryService.addPhotoToCategory(photo2, category1)
            category2 = categoryService.addPhotoToCategory(photo3, category2)
            
            assertEquals(2, category1.photos.size)
            assertEquals(1, category2.photos.size)
            
            assertEquals(category1.id, categoryService.findCategoryContainingPhoto(photo1)?.id)
            assertEquals(category1.id, categoryService.findCategoryContainingPhoto(photo2)?.id)
            assertEquals(category2.id, categoryService.findCategoryContainingPhoto(photo3)?.id)
        }

        @Test
        fun `should maintain photo order in category`() {
            var category = categoryService.createCategory()
            
            category = categoryService.addPhotoToCategory(photo1, category)
            category = categoryService.addPhotoToCategory(photo2, category)
            category = categoryService.addPhotoToCategory(photo3, category)
            
            assertEquals(photo1, category.photos[0])
            assertEquals(photo2, category.photos[1])
            assertEquals(photo3, category.photos[2])
        }

        @Test
        fun `should handle adding same photo to category multiple times`() {
            var category = categoryService.createCategory()
            
            category = categoryService.addPhotoToCategory(photo1, category)
            category = categoryService.addPhotoToCategory(photo1, category)
            
            assertEquals(2, category.photos.size)
        }
    }

    @Nested
    @DisplayName("Photo Reordering")
    inner class PhotoReorderingTests {

        @Test
        fun `should reorder photo to new position`() {
            var category = categoryService.createCategory()
            category = categoryService.addPhotoToCategory(photo1, category)
            category = categoryService.addPhotoToCategory(photo2, category)
            category = categoryService.addPhotoToCategory(photo3, category)

            val result = categoryService.reorderPhotoInCategory(photo3.id, category, 0)
            category = categoryService.getCategoryById(category.id)!!

            assertTrue(result)
            assertEquals(photo3, category.photos[0])
            assertEquals(photo1, category.photos[1])
            assertEquals(photo2, category.photos[2])
        }

        @Test
        fun `should reorder photo from first to last position`() {
            var category = categoryService.createCategory()
            category = categoryService.addPhotoToCategory(photo1, category)
            category = categoryService.addPhotoToCategory(photo2, category)
            category = categoryService.addPhotoToCategory(photo3, category)

            val result = categoryService.reorderPhotoInCategory(photo1.id, category, 3)
            category = categoryService.getCategoryById(category.id)!!

            assertTrue(result)
            assertEquals(photo2, category.photos[0])
            assertEquals(photo3, category.photos[1])
            assertEquals(photo1, category.photos[2])
        }

        @Test
        fun `should return false for photo not in category`() {
            var category = categoryService.createCategory()
            category = categoryService.addPhotoToCategory(photo1, category)

            val result = categoryService.reorderPhotoInCategory(photo2.id, category, 0)

            assertFalse(result)
        }

        @Test
        fun `should return false for invalid position`() {
            var category = categoryService.createCategory()
            category = categoryService.addPhotoToCategory(photo1, category)

            val result = categoryService.reorderPhotoInCategory(photo1.id, category, -1)

            assertFalse(result)
        }

        @Test
        fun `should return true when photo already at target position`() {
            var category = categoryService.createCategory()
            category = categoryService.addPhotoToCategory(photo1, category)
            category = categoryService.addPhotoToCategory(photo2, category)

            val result = categoryService.reorderPhotoInCategory(photo1.id, category, 0)
            category = categoryService.getCategoryById(category.id)!!

            assertTrue(result)
            assertEquals(photo1, category.photos[0])
            assertEquals(photo2, category.photos[1])
        }

        @Test
        fun `should preserve originalIndex after reordering`() {
            var category = categoryService.createCategory()
            category = categoryService.addPhotoToCategory(photo1, category)
            category = categoryService.addPhotoToCategory(photo2, category)
            category = categoryService.addPhotoToCategory(photo3, category)

            categoryService.reorderPhotoInCategory(photo3.id, category, 0)
            category = categoryService.getCategoryById(category.id)!!

            assertEquals(2, category.photos[0].originalIndex)
            assertEquals(0, category.photos[1].originalIndex)
            assertEquals(1, category.photos[2].originalIndex)
        }
    }

    @Nested
    @DisplayName("Cross-Category Photo Movement")
    inner class CrossCategoryMovementTests {

        @Test
        fun `should move photo from one category to another`() {
            var category1 = categoryService.createCategory()
            var category2 = categoryService.createCategory()
            
            category1 = categoryService.addPhotoToCategory(photo1, category1)
            category1 = categoryService.addPhotoToCategory(photo2, category1)
            
            category1 = categoryService.removePhotoFromCategory(photo1, category1)
            category2 = categoryService.addPhotoToCategory(photo1, category2)
            
            assertFalse(category1.containsPhoto(photo1))
            assertTrue(category1.containsPhoto(photo2))
            assertTrue(category2.containsPhoto(photo1))
            assertEquals(1, category1.photos.size)
            assertEquals(1, category2.photos.size)
        }

        @Test
        fun `should move multiple photos between categories`() {
            var category1 = categoryService.createCategory()
            var category2 = categoryService.createCategory()
            
            category1 = categoryService.addPhotoToCategory(photo1, category1)
            category1 = categoryService.addPhotoToCategory(photo2, category1)
            category1 = categoryService.addPhotoToCategory(photo3, category1)
            
            category1 = categoryService.removePhotoFromCategory(photo1, category1)
            category1 = categoryService.removePhotoFromCategory(photo3, category1)
            category2 = categoryService.addPhotoToCategory(photo1, category2)
            category2 = categoryService.addPhotoToCategory(photo3, category2)
            
            assertEquals(1, category1.photos.size)
            assertEquals(2, category2.photos.size)
            assertTrue(category1.containsPhoto(photo2))
            assertTrue(category2.containsPhoto(photo1))
            assertTrue(category2.containsPhoto(photo3))
        }

        @Test
        fun `should preserve photo originalIndex after cross-category move`() {
            var category1 = categoryService.createCategory()
            var category2 = categoryService.createCategory()
            
            category1 = categoryService.addPhotoToCategory(photo1, category1)
            
            category1 = categoryService.removePhotoFromCategory(photo1, category1)
            category2 = categoryService.addPhotoToCategory(photo1, category2)
            
            assertEquals(0, category2.photos[0].originalIndex)
        }

        @Test
        fun `should find photo in new category after move`() {
            var category1 = categoryService.createCategory()
            var category2 = categoryService.createCategory()
            
            category1 = categoryService.addPhotoToCategory(photo1, category1)
            assertEquals(category1.id, categoryService.findCategoryContainingPhoto(photo1)?.id)
            
            categoryService.removePhotoFromCategory(photo1, category1)
            categoryService.addPhotoToCategory(photo1, category2)
            
            assertEquals(category2.id, categoryService.findCategoryContainingPhoto(photo1)?.id)
        }
    }

    @Nested
    @DisplayName("Category Removal")
    inner class CategoryRemovalTests {

        @Test
        fun `should remove category by id`() {
            val category1 = categoryService.createCategory()
            val category2 = categoryService.createCategory()
            
            val removed = categoryService.removeCategory(category1.id)
            
            assertTrue(removed)
            assertEquals(1, categoryService.getCategoryCount())
            assertNull(categoryService.getCategoryById(category1.id))
            assertNotNull(categoryService.getCategoryById(category2.id))
        }

        @Test
        fun `should return false when removing non-existent category`() {
            categoryService.createCategory()
            
            val removed = categoryService.removeCategory("nonexistent_id")
            
            assertFalse(removed)
            assertEquals(1, categoryService.getCategoryCount())
        }

        @Test
        fun `should not include removed category in getCategories`() {
            val category1 = categoryService.createCategory()
            val category2 = categoryService.createCategory()
            val category3 = categoryService.createCategory()
            
            categoryService.removeCategory(category2.id)
            
            val categories = categoryService.getCategories()
            assertEquals(2, categories.size)
            assertTrue(categories.any { it.id == category1.id })
            assertFalse(categories.any { it.id == category2.id })
            assertTrue(categories.any { it.id == category3.id })
        }

        @Test
        fun `should return photos from removed category`() {
            var category = categoryService.createCategory()
            category = categoryService.addPhotoToCategory(photo1, category)
            category = categoryService.addPhotoToCategory(photo2, category)
            
            val returnedPhotos = categoryService.removeCategoryAndReturnPhotos(category.id)
            
            assertEquals(2, returnedPhotos.size)
            assertTrue(returnedPhotos.contains(photo1))
            assertTrue(returnedPhotos.contains(photo2))
            assertNull(categoryService.getCategoryById(category.id))
        }

        @Test
        fun `should return empty list when removing empty category`() {
            val category = categoryService.createCategory()
            
            val returnedPhotos = categoryService.removeCategoryAndReturnPhotos(category.id)
            
            assertTrue(returnedPhotos.isEmpty())
            assertNull(categoryService.getCategoryById(category.id))
        }

        @Test
        fun `should return empty list when removing non-existent category`() {
            val returnedPhotos = categoryService.removeCategoryAndReturnPhotos("nonexistent_id")
            
            assertTrue(returnedPhotos.isEmpty())
        }
    }

    @Nested
    @DisplayName("Category Numbering After Deletion")
    inner class CategoryNumberingTests {

        @Test
        fun `should not create duplicate category numbers after deletion`() {
            val category1 = categoryService.createCategory()
            val category2 = categoryService.createCategory()
            val category3 = categoryService.createCategory()
            
            categoryService.removeCategory(category2.id)
            
            val category4 = categoryService.createCategory()
            
            val allNames = categoryService.getCategories().map { it.name }
            val uniqueNames = allNames.toSet()
            assertEquals(allNames.size, uniqueNames.size)
        }

        @Test
        fun `should assign unique number to new category after deletion`() {
            categoryService.createCategory()
            val category2 = categoryService.createCategory()
            categoryService.createCategory()
            
            categoryService.removeCategory(category2.id)
            val newCategory = categoryService.createCategory()
            
            assertNotEquals("category_2", newCategory.id)
            assertNotEquals("category_3", newCategory.id)
            assertEquals("category_4", newCategory.id)
        }

        @Test
        fun `should handle multiple deletions and creations`() {
            val cat1 = categoryService.createCategory()
            val cat2 = categoryService.createCategory()
            val cat3 = categoryService.createCategory()
            
            categoryService.removeCategory(cat1.id)
            categoryService.removeCategory(cat3.id)
            
            val cat4 = categoryService.createCategory()
            val cat5 = categoryService.createCategory()
            
            assertEquals(3, categoryService.getCategoryCount())
            
            val allIds = categoryService.getCategories().map { it.id }
            val uniqueIds = allIds.toSet()
            assertEquals(allIds.size, uniqueIds.size)
        }
    }
}
