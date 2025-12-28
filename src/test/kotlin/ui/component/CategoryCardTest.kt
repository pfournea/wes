package ui.component

import domain.model.Category
import domain.model.Photo
import domain.service.CategoryService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import java.nio.file.Paths

@DisplayName("CategoryCard State Tests")
class CategoryCardTest {

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
    @DisplayName("Category Reference Updates")
    inner class CategoryReferenceTests {

        @Test
        fun `getCategory should return updated category after updateCategory is called`() {
            val initialCategory = categoryService.createCategory()
            
            val updatedCategory = categoryService.addPhotoToCategory(photo1, initialCategory)
            
            assertNotSame(initialCategory, updatedCategory)
            assertEquals(0, initialCategory.photos.size)
            assertEquals(1, updatedCategory.photos.size)
            
            val latestCategory = categoryService.getCategoryById(initialCategory.id)!!
            assertEquals(1, latestCategory.photos.size)
        }

        @Test
        fun `category photos should reflect additions made through CategoryService`() {
            var category = categoryService.createCategory()
            
            category = categoryService.addPhotoToCategory(photo1, category)
            category = categoryService.addPhotoToCategory(photo2, category)
            category = categoryService.addPhotoToCategory(photo3, category)
            
            val latestCategory = categoryService.getCategoryById(category.id)!!
            assertEquals(3, latestCategory.photos.size)
            assertEquals(photo1, latestCategory.photos[0])
            assertEquals(photo2, latestCategory.photos[1])
            assertEquals(photo3, latestCategory.photos[2])
        }

        @Test
        fun `stale category reference does not reflect service updates`() {
            val staleCategory = categoryService.createCategory()
            
            categoryService.addPhotoToCategory(photo1, staleCategory)
            categoryService.addPhotoToCategory(photo2, staleCategory)
            
            assertEquals(0, staleCategory.photos.size)
            
            val freshCategory = categoryService.getCategoryById(staleCategory.id)!!
            assertEquals(2, freshCategory.photos.size)
        }

        @Test
        fun `getCategoryById returns latest state after modifications`() {
            val category = categoryService.createCategory()
            val categoryId = category.id
            
            categoryService.addPhotoToCategory(photo1, category)
            val afterAdd = categoryService.getCategoryById(categoryId)!!
            
            categoryService.addPhotoToCategory(photo2, afterAdd)
            val afterSecondAdd = categoryService.getCategoryById(categoryId)!!
            
            categoryService.removePhotoFromCategory(photo1, afterSecondAdd)
            val afterRemove = categoryService.getCategoryById(categoryId)!!
            
            assertEquals(1, afterAdd.photos.size)
            assertEquals(2, afterSecondAdd.photos.size)
            assertEquals(1, afterRemove.photos.size)
            assertEquals(photo2, afterRemove.photos[0])
        }
    }
}
