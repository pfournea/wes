package domain.service

import domain.model.Category
import domain.model.Photo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import java.nio.file.Paths

@DisplayName("CategoryService - Photo Lookup Tests")
class CategoryServicePhotoLookupTest {

    private lateinit var categoryService: CategoryService
    private lateinit var category1: Category
    private lateinit var category2: Category
    private lateinit var photo1: Photo
    private lateinit var photo2: Photo
    private lateinit var photo3: Photo

    @BeforeEach
    fun setUp() {
        categoryService = CategoryService()
        category1 = categoryService.createCategory()
        category2 = categoryService.createCategory()
        
        photo1 = Photo.fromPath(Paths.get("/test/photo1.jpg"), 0)
        photo2 = Photo.fromPath(Paths.get("/test/photo2.jpg"), 1)
        photo3 = Photo.fromPath(Paths.get("/test/photo3.jpg"), 2)
        
        category1.addPhoto(photo1)
        category1.addPhoto(photo2)
        category2.addPhoto(photo3)
    }

    @Nested
    @DisplayName("Find Photo By ID")
    inner class FindPhotoByIdTests {

        @Test
        fun `should find photo in first category`() {
            val found = categoryService.findPhotoById(photo1.id)
            
            assertNotNull(found)
            assertEquals(photo1, found)
        }

        @Test
        fun `should find photo in second category`() {
            val found = categoryService.findPhotoById(photo3.id)
            
            assertNotNull(found)
            assertEquals(photo3, found)
        }

        @Test
        fun `should return null for non-existent photo ID`() {
            val found = categoryService.findPhotoById("nonexistent_id")
            
            assertNull(found)
        }

        @Test
        fun `should return null when no categories exist`() {
            val emptyService = CategoryService()
            
            val found = emptyService.findPhotoById(photo1.id)
            
            assertNull(found)
        }

        @Test
        fun `should find photo after moving between categories`() {
            // Move photo1 from category1 to category2
            category1.removePhoto(photo1)
            category2.addPhoto(photo1)
            
            val found = categoryService.findPhotoById(photo1.id)
            
            assertNotNull(found)
            assertEquals(photo1, found)
        }

        @Test
        fun `should not find photo after removal`() {
            category1.removePhoto(photo1)
            
            val found = categoryService.findPhotoById(photo1.id)
            
            assertNull(found)
        }

        @Test
        fun `should handle multiple categories with same photo IDs`() {
            // This shouldn't happen in practice, but test robustness
            val photo4 = Photo.fromPath(Paths.get("/test/photo1.jpg"), 0) // Same ID as photo1
            category2.addPhoto(photo4)
            
            val found = categoryService.findPhotoById(photo1.id)
            
            assertNotNull(found)
            // Should find the first occurrence
            assertEquals(photo1.id, found?.id)
        }
    }
}
