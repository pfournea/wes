package domain.service

import domain.model.Category
import domain.model.Photo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import java.nio.file.Paths

@DisplayName("CopyService Tests")
class CopyServiceTest {

    private lateinit var categoryService: CategoryService
    private lateinit var copyService: CopyService
    private lateinit var photo1: Photo
    private lateinit var photo2: Photo
    private lateinit var photo3: Photo

    @BeforeEach
    fun setUp() {
        categoryService = CategoryService()
        copyService = CopyService(categoryService)
        photo1 = Photo.fromPath(Paths.get("/test/photo1.jpg"), 0)
        photo2 = Photo.fromPath(Paths.get("/test/photo2.jpg"), 1)
        photo3 = Photo.fromPath(Paths.get("/test/photo3.jpg"), 2)
    }

    @Nested
    @DisplayName("Copying Photos to Single Category")
    inner class CopyToSingleCategoryTests {

        @Test
        fun `should copy photos to single target category`() {
            val sourceCategory = categoryService.createCategory()
            val targetCategory = categoryService.createCategory()

            var updatedSource = categoryService.addPhotoToCategory(photo1, sourceCategory)
            updatedSource = categoryService.addPhotoToCategory(photo2, updatedSource)

            val result = copyService.copyPhotosToCategories(updatedSource, listOf(targetCategory))

            assertTrue(result.success)
            assertEquals(2, result.photosCopied)
            assertEquals(1, result.categoriesUpdated)

            val updatedTarget = categoryService.getCategoryById(targetCategory.id)!!
            assertEquals(2, updatedTarget.photos.size)
        }

        @Test
        fun `should preserve photo order when copying`() {
            val sourceCategory = categoryService.createCategory()
            val targetCategory = categoryService.createCategory()

            var updatedSource = categoryService.addPhotoToCategory(photo1, sourceCategory)
            updatedSource = categoryService.addPhotoToCategory(photo2, updatedSource)
            updatedSource = categoryService.addPhotoToCategory(photo3, updatedSource)

            val result = copyService.copyPhotosToCategories(updatedSource, listOf(targetCategory))

            assertTrue(result.success)

            val updatedTarget = categoryService.getCategoryById(targetCategory.id)!!
            assertEquals(3, updatedTarget.photos.size)
            assertEquals(photo1.originalIndex, updatedTarget.photos[0].originalIndex)
            assertEquals(photo2.originalIndex, updatedTarget.photos[1].originalIndex)
            assertEquals(photo3.originalIndex, updatedTarget.photos[2].originalIndex)
        }

        @Test
        fun `should create unique photo IDs when copying`() {
            val sourceCategory = categoryService.createCategory()
            val targetCategory = categoryService.createCategory()

            val updatedSource = categoryService.addPhotoToCategory(photo1, sourceCategory)

            val result = copyService.copyPhotosToCategories(updatedSource, listOf(targetCategory))

            assertTrue(result.success)

            val updatedTarget = categoryService.getCategoryById(targetCategory.id)!!
            val copiedPhoto = updatedTarget.photos[0]
            assertNotEquals(photo1.id, copiedPhoto.id)
            assertTrue(copiedPhoto.id.contains("_${targetCategory.number}_"))
        }

        @Test
        fun `should not modify source category when copying`() {
            val sourceCategory = categoryService.createCategory()
            val targetCategory = categoryService.createCategory()

            var updatedSource = categoryService.addPhotoToCategory(photo1, sourceCategory)
            updatedSource = categoryService.addPhotoToCategory(photo2, updatedSource)

            copyService.copyPhotosToCategories(updatedSource, listOf(targetCategory))

            val source = categoryService.getCategoryById(sourceCategory.id)!!
            assertEquals(2, source.photos.size)
        }
    }

    @Nested
    @DisplayName("Copying Photos to Multiple Categories")
    inner class CopyToMultipleCategoriesTests {

        @Test
        fun `should copy photos to multiple target categories`() {
            val sourceCategory = categoryService.createCategory()
            val targetCategory1 = categoryService.createCategory()
            val targetCategory2 = categoryService.createCategory()
            val targetCategory3 = categoryService.createCategory()

            var updatedSource = categoryService.addPhotoToCategory(photo1, sourceCategory)
            updatedSource = categoryService.addPhotoToCategory(photo2, updatedSource)

            val result = copyService.copyPhotosToCategories(
                updatedSource,
                listOf(targetCategory1, targetCategory2, targetCategory3)
            )

            assertTrue(result.success)
            assertEquals(6, result.photosCopied)
            assertEquals(3, result.categoriesUpdated)

            for (cat in listOf(targetCategory1, targetCategory2, targetCategory3)) {
                val updated = categoryService.getCategoryById(cat.id)!!
                assertEquals(2, updated.photos.size)
            }
        }

        @Test
        fun `should skip copying to source category`() {
            var sourceCategory = categoryService.createCategory()
            var otherCategory = categoryService.createCategory()

            sourceCategory = categoryService.addPhotoToCategory(photo1, sourceCategory)

            val result = copyService.copyPhotosToCategories(
                sourceCategory,
                listOf(sourceCategory, otherCategory)
            )

            assertTrue(result.success)
            assertEquals(1, result.photosCopied)
            assertEquals(1, result.categoriesUpdated)

            val other = categoryService.getCategoryById(otherCategory.id)!!
            assertEquals(1, other.photos.size)
        }

        @Test
        fun `should handle empty source category`() {
            val sourceCategory = categoryService.createCategory()
            val targetCategory = categoryService.createCategory()

            val result = copyService.copyPhotosToCategories(sourceCategory, listOf(targetCategory))

            assertFalse(result.success)
            assertEquals(0, result.photosCopied)
            assertTrue(result.errors.any { it.contains("no photos") })
        }

        @Test
        fun `should handle empty target categories list`() {
            var sourceCategory = categoryService.createCategory()
            sourceCategory = categoryService.addPhotoToCategory(photo1, sourceCategory)

            val result = copyService.copyPhotosToCategories(sourceCategory, emptyList())

            assertFalse(result.success)
            assertEquals(0, result.photosCopied)
            assertTrue(result.errors.any { it.contains("No target") })
        }

        @Test
        fun `should append to existing photos in target category`() {
            var sourceCategory = categoryService.createCategory()
            var targetCategory = categoryService.createCategory()

            targetCategory = categoryService.addPhotoToCategory(photo1, targetCategory)
            sourceCategory = categoryService.addPhotoToCategory(photo2, sourceCategory)

            val result = copyService.copyPhotosToCategories(sourceCategory, listOf(targetCategory))

            assertTrue(result.success)
            assertEquals(0, result.errors.size)

            val finalTarget = categoryService.getCategoryById(targetCategory.id)!!
            assertEquals(2, finalTarget.photos.size)
            assertEquals(photo1.originalIndex, finalTarget.photos[0].originalIndex)
            assertEquals(photo2.originalIndex, finalTarget.photos[1].originalIndex)
        }
    }

    @Nested
    @DisplayName("Category Range Parsing")
    inner class CategoryRangeParsingTests {

        @Test
        fun `should parse simple range`() {
            val result = copyService.parseCategoryRange("10-16", emptyList())

            assertEquals(7, result.size)
            assertEquals(listOf(10, 11, 12, 13, 14, 15, 16), result)
        }

        @Test
        fun `should parse single numbers`() {
            val result = copyService.parseCategoryRange("1,3,5", emptyList())

            assertEquals(3, result.size)
            assertEquals(listOf(1, 3, 5), result)
        }

        @Test
        fun `should parse mixed range and single numbers`() {
            val result = copyService.parseCategoryRange("2-4,8,10-12", emptyList())

            assertEquals(7, result.size)
            assertEquals(listOf(2, 3, 4, 8, 10, 11, 12), result)
        }

        @Test
        fun `should handle reversed range`() {
            val result = copyService.parseCategoryRange("16-10", emptyList())

            assertEquals(7, result.size)
            assertEquals(listOf(10, 11, 12, 13, 14, 15, 16), result)
        }

        @Test
        fun `should handle single number as range`() {
            val result = copyService.parseCategoryRange("5-5", emptyList())

            assertEquals(1, result.size)
            assertEquals(5, result[0])
        }

        @Test
        fun `should handle whitespace in input`() {
            val result = copyService.parseCategoryRange(" 1 - 3 , 5 , 7 - 9 ", emptyList())

            assertEquals(7, result.size)
            assertEquals(listOf(1, 2, 3, 5, 7, 8, 9), result)
        }

        @Test
        fun `should return empty list for invalid input`() {
            val result = copyService.parseCategoryRange("abc", emptyList())

            assertTrue(result.isEmpty())
        }

        @Test
        fun `should return empty list for empty input`() {
            val result = copyService.parseCategoryRange("", emptyList())

            assertTrue(result.isEmpty())
        }

        @Test
        fun `should parse range without filtering by existing categories`() {
            val existingCategories = listOf(
                Category.create(1),
                Category.create(3),
                Category.create(5)
            )

            val result = copyService.parseCategoryRange("1-10", existingCategories)

            assertEquals(10, result.size)
            assertEquals(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), result)
        }

        @Test
        fun `should return sorted unique numbers`() {
            val result = copyService.parseCategoryRange("5,1,3,2,4", emptyList())

            assertEquals(5, result.size)
            assertEquals(listOf(1, 2, 3, 4, 5), result)
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    inner class EdgeCasesTests {

        @Test
        fun `should handle copying large number of photos`() {
            val sourceCategory = categoryService.createCategory()
            val targetCategory = categoryService.createCategory()

            var updatedSource = sourceCategory
            for (i in 0 until 100) {
                val photo = Photo.fromPath(Paths.get("/test/photo$i.jpg"), i)
                updatedSource = categoryService.addPhotoToCategory(photo, updatedSource)
            }

            val result = copyService.copyPhotosToCategories(updatedSource, listOf(targetCategory))

            assertTrue(result.success)
            assertEquals(100, result.photosCopied)

            val updatedTarget = categoryService.getCategoryById(targetCategory.id)!!
            assertEquals(100, updatedTarget.photos.size)
        }

        @Test
        fun `should preserve original photo data except ID`() {
            val sourceCategory = categoryService.createCategory()
            val targetCategory = categoryService.createCategory()

            val sourcePhoto = Photo.fromPath(Paths.get("/test/photo1.jpg"), 0).copy(rotationDegrees = 90)
            val updatedSource = categoryService.addPhotoToCategory(sourcePhoto, sourceCategory)

            val result = copyService.copyPhotosToCategories(updatedSource, listOf(targetCategory))

            assertTrue(result.success)

            val updatedTarget = categoryService.getCategoryById(targetCategory.id)!!
            val copiedPhoto = updatedTarget.photos[0]
            assertEquals(90, copiedPhoto.rotationDegrees)
            assertEquals(sourcePhoto.originalIndex, copiedPhoto.originalIndex)
            assertEquals(sourcePhoto.fileName, copiedPhoto.fileName)
        }
    }
}
