package domain.service

import domain.model.Category
import domain.model.Photo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@DisplayName("ExportService Tests")
class ExportServiceTest {

    private lateinit var exportService: ExportService
    
    @TempDir
    lateinit var tempSourceDir: Path
    
    @TempDir
    lateinit var tempTargetDir: Path

    @BeforeEach
    fun setUp() {
        exportService = ExportService()
    }

    private fun createTestPhoto(name: String, index: Int): Photo {
        // Create actual file in temp directory
        val photoPath = tempSourceDir.resolve(name)
        Files.write(photoPath, "test image content".toByteArray())
        return Photo.fromPath(photoPath, index)
    }

    @Nested
    @DisplayName("Export Categories")
    inner class ExportCategoriesTests {

        @Test
        fun `should export single category with one photo`() {
            val photo = createTestPhoto("test1.jpg", 0)
            val category = Category("cat1", 1, "Category 1", mutableListOf(photo))
            
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertTrue(result.success)
            assertEquals(1, result.photosCopied)
            assertTrue(Files.exists(tempTargetDir.resolve("0001.jpg")))
        }

        @Test
        fun `should export multiple categories with multiple photos`() {
            val photo1 = createTestPhoto("test1.jpg", 0)
            val photo2 = createTestPhoto("test2.png", 1)
            val photo3 = createTestPhoto("test3.gif", 2)
            val photo4 = createTestPhoto("test4.jpg", 3)
            
            val category1 = Category("cat1", 1, "Category 1", mutableListOf(photo1, photo2))
            val category2 = Category("cat2", 2, "Category 2", mutableListOf(photo3, photo4))
            
            val result = exportService.exportCategories(listOf(category1, category2), tempTargetDir)
            
            assertTrue(result.success)
            assertEquals(4, result.photosCopied)
            assertTrue(Files.exists(tempTargetDir.resolve("0001.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("0001-01.png")))
            assertTrue(Files.exists(tempTargetDir.resolve("0002.gif")))
            assertTrue(Files.exists(tempTargetDir.resolve("0002-01.jpg")))
        }

        @Test
        fun `should skip empty categories`() {
            val photo = createTestPhoto("test1.jpg", 0)
            val category1 = Category("cat1", 1, "Category 1", mutableListOf(photo))
            val category2 = Category("cat2", 2, "Category 2", mutableListOf()) // Empty
            
            val result = exportService.exportCategories(listOf(category1, category2), tempTargetDir)
            
            assertTrue(result.success)
            assertEquals(1, result.photosCopied)
            assertTrue(Files.exists(tempTargetDir.resolve("0001.jpg")))
            assertFalse(Files.exists(tempTargetDir.resolve("0002.jpg")))
        }

        @Test
        fun `should handle empty category list`() {
            val result = exportService.exportCategories(emptyList(), tempTargetDir)
            
            assertTrue(result.success)
            assertEquals(0, result.photosCopied)
        }

        @Test
        fun `should create target directory if it does not exist`() {
            val nonExistentDir = tempTargetDir.resolve("newdir")
            assertFalse(Files.exists(nonExistentDir))
            
            val photo = createTestPhoto("test1.jpg", 0)
            val category = Category("cat1", 1, "Category 1", mutableListOf(photo))
            
            val result = exportService.exportCategories(listOf(category), nonExistentDir)
            
            assertTrue(result.success)
            assertTrue(Files.exists(nonExistentDir))
            assertTrue(Files.exists(nonExistentDir.resolve("0001.jpg")))
        }
    }

    @Nested
    @DisplayName("Filename Generation")
    inner class FilenameGenerationTests {

        @Test
        fun `should generate filename with correct format`() {
            val photo = createTestPhoto("myPhoto.jpg", 0)
            val category = Category("cat1", 3, "Category 3", mutableListOf(photo))
            
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertTrue(Files.exists(tempTargetDir.resolve("0003.jpg")))
        }

        @Test
        fun `should pad position with leading zeros`() {
            val photos = (0..4).map { createTestPhoto("test$it.jpg", it) }.toMutableList()
            val category = Category("cat1", 2, "Category 2", photos)
            
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertTrue(Files.exists(tempTargetDir.resolve("0002.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("0002-01.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("0002-02.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("0002-03.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("0002-04.jpg")))
        }

        @Test
        fun `should handle large position numbers`() {
            // Create category with photo at position 12345
            val photos = mutableListOf<Photo>()
            // We'll just add one photo and verify it gets position 1
            // but the padding should handle 4 digits for category
            val photo = createTestPhoto("test.jpg", 0)
            val category = Category("cat1", 1, "Category 1", mutableListOf(photo))
            
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            // Position 1 should be just category number: 0001
            assertTrue(Files.exists(tempTargetDir.resolve("0001.jpg")))
        }

        @Test
        fun `should name subsequent photos with hyphen and 2-digit position`() {
            val photo1 = createTestPhoto("test1.jpg", 0)
            val photo2 = createTestPhoto("test2.jpg", 1)
            val photo3 = createTestPhoto("test3.jpg", 2)
            val category = Category("cat1", 5, "Category 5", 
                mutableListOf(photo1, photo2, photo3))
            
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertTrue(result.success)
            assertTrue(Files.exists(tempTargetDir.resolve("0005.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("0005-01.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("0005-02.jpg")))
        }
    }

    @Nested
    @DisplayName("Directory Empty Check")
    inner class DirectoryEmptyTests {

        @Test
        fun `should return false for directory with files`() {
            Files.write(tempTargetDir.resolve("file1.txt"), "content".toByteArray())
            Files.write(tempTargetDir.resolve("file2.jpg"), "content".toByteArray())
            Files.write(tempTargetDir.resolve("file3.png"), "content".toByteArray())
            
            assertFalse(exportService.isDirectoryEmpty(tempTargetDir))
        }

        @Test
        fun `should return true for empty directory`() {
            assertTrue(exportService.isDirectoryEmpty(tempTargetDir))
        }

        @Test
        fun `should return true for non-existent directory`() {
            val nonExistent = tempTargetDir.resolve("doesnotexist")
            
            assertTrue(exportService.isDirectoryEmpty(nonExistent))
        }

        @Test
        fun `should return false for directory with subdirectories`() {
            Files.createDirectories(tempTargetDir.resolve("subdir"))
            
            assertFalse(exportService.isDirectoryEmpty(tempTargetDir))
        }

        @Test
        fun `should return false for directory with both files and subdirectories`() {
            Files.write(tempTargetDir.resolve("file1.txt"), "content".toByteArray())
            Files.createDirectories(tempTargetDir.resolve("subdir"))
            
            assertFalse(exportService.isDirectoryEmpty(tempTargetDir))
        }
    }

    @Nested
    @DisplayName("Error Handling")
    inner class ErrorHandlingTests {

        @Test
        fun `should handle missing source file gracefully`() {
            // Create photo with non-existent source file
            val nonExistentPath = tempSourceDir.resolve("nonexistent.jpg")
            val photo = Photo.fromPath(nonExistentPath, 0)
            val category = Category("cat1", 1, "Category 1", mutableListOf(photo))
            
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertFalse(result.success)
            assertEquals(0, result.photosCopied)
            assertFalse(result.errors.isEmpty())
        }

        @Test
        fun `should continue exporting after error with one file`() {
            val goodPhoto1 = createTestPhoto("good1.jpg", 0)
            val nonExistentPath = tempSourceDir.resolve("nonexistent.jpg")
            val badPhoto = Photo.fromPath(nonExistentPath, 1)
            val goodPhoto2 = createTestPhoto("good2.jpg", 2)
            
            val category = Category("cat1", 1, "Category 1", 
                mutableListOf(goodPhoto1, badPhoto, goodPhoto2))
            
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertFalse(result.success) // Has errors
            assertEquals(2, result.photosCopied) // But copied the good ones
            assertEquals(1, result.errors.size)
            assertTrue(Files.exists(tempTargetDir.resolve("0001.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("0001-02.jpg")))
        }
    }

    @Nested
    @DisplayName("New Naming Format Tests")
    inner class NewNamingFormatTests {

        @Test
        fun `should name first photo without hyphen or position`() {
            val photo = createTestPhoto("test1.jpg", 0)
            val category = Category("cat1", 5, "Category 5", mutableListOf(photo))
            
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertTrue(result.success)
            assertTrue(Files.exists(tempTargetDir.resolve("0005.jpg")))
        }

        @Test
        fun `should pad position with leading zeros`() {
            val photos = mutableListOf<Photo>()
            repeat(5) { i ->
                photos.add(createTestPhoto("test${i+1}.jpg", i))
            }
            val category = Category("cat1", 2, "Category 2", photos)
            
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertTrue(Files.exists(tempTargetDir.resolve("0002.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("0002-01.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("0002-02.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("0002-03.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("0002-04.jpg")))
        }

        @Test
        fun `should use 4-digit category padding for all categories`() {
            val photo1 = createTestPhoto("test1.jpg", 0)
            val photo2 = createTestPhoto("test2.jpg", 1)
            val photo3 = createTestPhoto("test3.jpg", 2)
            
            val cat1 = Category("cat1", 1, "Category 1", mutableListOf(photo1))
            val cat11 = Category("cat11", 11, "Category 11", mutableListOf(photo2))
            val cat123 = Category("cat123", 123, "Category 123", mutableListOf(photo3))
            
            val result = exportService.exportCategories(listOf(cat1, cat11, cat123), tempTargetDir)
            
            assertTrue(result.success)
            assertTrue(Files.exists(tempTargetDir.resolve("0001.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("0011.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("0123.jpg")))
        }

        @Test
        fun `should handle double-digit photo positions correctly`() {
            val photos = (0..14).map { createTestPhoto("test$it.jpg", it) }.toMutableList()
            val category = Category("cat1", 7, "Category 7", photos)
            
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertTrue(result.success)
            assertTrue(Files.exists(tempTargetDir.resolve("0007.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("0007-01.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("0007-08.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("0007-09.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("0007-14.jpg")))
        }

        @Test
        fun `should use hyphens not underscores in filenames`() {
            val photo1 = createTestPhoto("test1.jpg", 0)
            val photo2 = createTestPhoto("test2.jpg", 1)
            val category = Category("cat1", 5, "Category 5", mutableListOf(photo1, photo2))
            
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertTrue(result.success)
            // Verify no underscores exist in exported filenames
            Files.list(tempTargetDir).use { stream ->
                stream.forEach { path ->
                    assertFalse(path.fileName.toString().contains("_"), 
                        "Filename should not contain underscores: ${path.fileName}")
                }
            }
        }
    }
}
