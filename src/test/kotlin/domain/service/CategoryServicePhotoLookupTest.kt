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
        
        category1 = categoryService.addPhotoToCategory(photo1, category1)
        category1 = categoryService.addPhotoToCategory(photo2, category1)
        category2 = categoryService.addPhotoToCategory(photo3, category2)
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
            category1 = categoryService.removePhotoFromCategory(photo1, category1)
            category2 = categoryService.addPhotoToCategory(photo1, category2)
            
            val found = categoryService.findPhotoById(photo1.id)
            
            assertNotNull(found)
            assertEquals(photo1, found)
        }

        @Test
        fun `should not find photo after removal`() {
            category1 = categoryService.removePhotoFromCategory(photo1, category1)
            
            val found = categoryService.findPhotoById(photo1.id)
            
            assertNull(found)
        }

        @Test
        fun `should handle multiple categories with same photo IDs`() {
            val photo4 = Photo.fromPath(Paths.get("/test/photo1.jpg"), 0)
            category2 = categoryService.addPhotoToCategory(photo4, category2)
            
            val found = categoryService.findPhotoById(photo1.id)
            
            assertNotNull(found)
            assertEquals(photo1.id, found?.id)
        }
    }
}
