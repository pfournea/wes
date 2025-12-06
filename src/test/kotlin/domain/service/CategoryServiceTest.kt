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
            val category = categoryService.createCategory()
            
            categoryService.addPhotoToCategory(photo1, category)
            
            assertTrue(category.containsPhoto(photo1))
            assertEquals(1, category.photos.size)
        }

        @Test
        fun `should add photo to category at specific position`() {
            val category = categoryService.createCategory()
            categoryService.addPhotoToCategory(photo1, category)
            categoryService.addPhotoToCategory(photo2, category)
            
            // Insert photo3 at position 1 (between photo1 and photo2)
            categoryService.addPhotoToCategory(photo3, category, position = 1)
            
            assertEquals(3, category.photos.size)
            assertEquals(photo1, category.photos[0])
            assertEquals(photo3, category.photos[1])
            assertEquals(photo2, category.photos[2])
        }

        @Test
        fun `should add photo at end when position is null`() {
            val category = categoryService.createCategory()
            categoryService.addPhotoToCategory(photo1, category)
            categoryService.addPhotoToCategory(photo2, category)
            
            categoryService.addPhotoToCategory(photo3, category, position = null)
            
            assertEquals(3, category.photos.size)
            assertEquals(photo3, category.photos[2])
        }

        @Test
        fun `should remove photo from category`() {
            val category = categoryService.createCategory()
            categoryService.addPhotoToCategory(photo1, category)
            categoryService.addPhotoToCategory(photo2, category)
            
            categoryService.removePhotoFromCategory(photo1, category)
            
            assertFalse(category.containsPhoto(photo1))
            assertTrue(category.containsPhoto(photo2))
            assertEquals(1, category.photos.size)
        }

        @Test
        fun `should handle removing non-existent photo`() {
            val category = categoryService.createCategory()
            categoryService.addPhotoToCategory(photo1, category)
            
            // Should not throw exception
            categoryService.removePhotoFromCategory(photo2, category)
            
            assertEquals(1, category.photos.size)
        }
    }

    @Nested
    @DisplayName("Category Lookup")
    inner class CategoryLookupTests {

        @Test
        fun `should find category containing photo`() {
            val category1 = categoryService.createCategory()
            val category2 = categoryService.createCategory()
            
            categoryService.addPhotoToCategory(photo1, category1)
            categoryService.addPhotoToCategory(photo2, category2)
            
            val foundCategory = categoryService.findCategoryContainingPhoto(photo2)
            
            assertNotNull(foundCategory)
            assertEquals(category2.id, foundCategory?.id)
        }

        @Test
        fun `should return null when photo is not in any category`() {
            val category = categoryService.createCategory()
            categoryService.addPhotoToCategory(photo1, category)
            
            val foundCategory = categoryService.findCategoryContainingPhoto(photo2)
            
            assertNull(foundCategory)
        }

        @Test
        fun `should get category by id`() {
            val category1 = categoryService.createCategory()
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
            assertTrue(allCategories.contains(category1))
            assertTrue(allCategories.contains(category2))
            assertTrue(allCategories.contains(category3))
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
            val category1 = categoryService.createCategory()
            val category2 = categoryService.createCategory()
            
            // Add photo to category1
            categoryService.addPhotoToCategory(photo1, category1)
            assertTrue(category1.containsPhoto(photo1))
            
            // Move to category2
            categoryService.removePhotoFromCategory(photo1, category1)
            categoryService.addPhotoToCategory(photo1, category2)
            
            assertFalse(category1.containsPhoto(photo1))
            assertTrue(category2.containsPhoto(photo1))
        }

        @Test
        fun `should handle multiple photos in multiple categories`() {
            val category1 = categoryService.createCategory()
            val category2 = categoryService.createCategory()
            
            categoryService.addPhotoToCategory(photo1, category1)
            categoryService.addPhotoToCategory(photo2, category1)
            categoryService.addPhotoToCategory(photo3, category2)
            
            assertEquals(2, category1.photos.size)
            assertEquals(1, category2.photos.size)
            
            assertEquals(category1, categoryService.findCategoryContainingPhoto(photo1))
            assertEquals(category1, categoryService.findCategoryContainingPhoto(photo2))
            assertEquals(category2, categoryService.findCategoryContainingPhoto(photo3))
        }

        @Test
        fun `should maintain photo order in category`() {
            val category = categoryService.createCategory()
            
            categoryService.addPhotoToCategory(photo1, category)
            categoryService.addPhotoToCategory(photo2, category)
            categoryService.addPhotoToCategory(photo3, category)
            
            assertEquals(photo1, category.photos[0])
            assertEquals(photo2, category.photos[1])
            assertEquals(photo3, category.photos[2])
        }

        @Test
        fun `should handle adding same photo to category multiple times`() {
            val category = categoryService.createCategory()
            
            categoryService.addPhotoToCategory(photo1, category)
            categoryService.addPhotoToCategory(photo1, category)
            
            // Should allow duplicates (list behavior)
            assertEquals(2, category.photos.size)
        }
    }
}
