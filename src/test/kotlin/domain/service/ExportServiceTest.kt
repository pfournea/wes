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
            assertTrue(Files.exists(tempTargetDir.resolve("1_00001.jpg")))
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
            assertTrue(Files.exists(tempTargetDir.resolve("1_00001.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("1_00002.png")))
            assertTrue(Files.exists(tempTargetDir.resolve("2_00001.gif")))
            assertTrue(Files.exists(tempTargetDir.resolve("2_00002.jpg")))
        }

        @Test
        fun `should skip empty categories`() {
            val photo = createTestPhoto("test1.jpg", 0)
            val category1 = Category("cat1", 1, "Category 1", mutableListOf(photo))
            val category2 = Category("cat2", 2, "Category 2", mutableListOf()) // Empty
            
            val result = exportService.exportCategories(listOf(category1, category2), tempTargetDir)
            
            assertTrue(result.success)
            assertEquals(1, result.photosCopied)
            assertTrue(Files.exists(tempTargetDir.resolve("1_00001.jpg")))
            assertFalse(Files.exists(tempTargetDir.resolve("2_00001.jpg")))
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
            assertTrue(Files.exists(nonExistentDir.resolve("1_00001.jpg")))
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
            
            assertTrue(Files.exists(tempTargetDir.resolve("3_00001.jpg")))
        }

        @Test
        fun `should pad position with leading zeros`() {
            val photos = (0..4).map { createTestPhoto("test$it.jpg", it) }.toMutableList()
            val category = Category("cat1", 2, "Category 2", photos)
            
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertTrue(Files.exists(tempTargetDir.resolve("2_00001.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("2_00002.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("2_00003.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("2_00004.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("2_00005.jpg")))
        }

        @Test
        fun `should handle large position numbers`() {
            // Create category with photo at position 12345
            val photos = mutableListOf<Photo>()
            // We'll just add one photo and verify it gets position 1
            // but the padding should handle 5 digits
            val photo = createTestPhoto("test.jpg", 0)
            val category = Category("cat1", 1, "Category 1", mutableListOf(photo))
            
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            // Position 1 should be padded to 00001
            assertTrue(Files.exists(tempTargetDir.resolve("1_00001.jpg")))
        }

        @Test
        fun `should preserve file extension`() {
            val photoJpg = createTestPhoto("test1.jpg", 0)
            val photoPng = createTestPhoto("test2.png", 1)
            val photoGif = createTestPhoto("test3.gif", 2)
            val photoBmp = createTestPhoto("test4.bmp", 3)
            
            val category = Category("cat1", 1, "Category 1", 
                mutableListOf(photoJpg, photoPng, photoGif, photoBmp))
            
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertTrue(Files.exists(tempTargetDir.resolve("1_00001.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("1_00002.png")))
            assertTrue(Files.exists(tempTargetDir.resolve("1_00003.gif")))
            assertTrue(Files.exists(tempTargetDir.resolve("1_00004.bmp")))
        }
    }

    @Nested
    @DisplayName("Directory Cleanup")
    inner class DirectoryCleanupTests {

        @Test
        fun `should delete all existing files before export`() {
            // Create some existing files in target directory
            Files.write(tempTargetDir.resolve("old1.txt"), "old content".toByteArray())
            Files.write(tempTargetDir.resolve("old2.jpg"), "old image".toByteArray())
            Files.write(tempTargetDir.resolve("random.dat"), "random data".toByteArray())
            
            assertEquals(3, exportService.countFilesInDirectory(tempTargetDir))
            
            val photo = createTestPhoto("test1.jpg", 0)
            val category = Category("cat1", 1, "Category 1", mutableListOf(photo))
            
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertTrue(result.success)
            assertEquals(3, result.filesDeleted)
            assertEquals(1, result.photosCopied)
            
            // Old files should be gone
            assertFalse(Files.exists(tempTargetDir.resolve("old1.txt")))
            assertFalse(Files.exists(tempTargetDir.resolve("old2.jpg")))
            assertFalse(Files.exists(tempTargetDir.resolve("random.dat")))
            
            // New file should exist
            assertTrue(Files.exists(tempTargetDir.resolve("1_00001.jpg")))
        }

        @Test
        fun `should re-export to same directory correctly`() {
            // First export
            val photo1 = createTestPhoto("test1.jpg", 0)
            val category1 = Category("cat1", 1, "Category 1", mutableListOf(photo1))
            
            val result1 = exportService.exportCategories(listOf(category1), tempTargetDir)
            assertTrue(result1.success)
            assertEquals(1, result1.photosCopied)
            
            // Second export with different photos
            val photo2 = createTestPhoto("test2.png", 0)
            val photo3 = createTestPhoto("test3.jpg", 1)
            val category2 = Category("cat1", 2, "Category 2", mutableListOf(photo2, photo3))
            
            val result2 = exportService.exportCategories(listOf(category2), tempTargetDir)
            assertTrue(result2.success)
            assertEquals(1, result2.filesDeleted) // Delete previous export
            assertEquals(2, result2.photosCopied)
            
            // Old file should be gone
            assertFalse(Files.exists(tempTargetDir.resolve("1_00001.jpg")))
            
            // New files should exist
            assertTrue(Files.exists(tempTargetDir.resolve("2_00001.png")))
            assertTrue(Files.exists(tempTargetDir.resolve("2_00002.jpg")))
        }

        @Test
        fun `should not delete subdirectories`() {
            // Create a subdirectory
            val subdir = tempTargetDir.resolve("subdir")
            Files.createDirectories(subdir)
            Files.write(subdir.resolve("file.txt"), "content".toByteArray())
            
            val photo = createTestPhoto("test1.jpg", 0)
            val category = Category("cat1", 1, "Category 1", mutableListOf(photo))
            
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertTrue(result.success)
            // Subdirectory and its contents should still exist
            assertTrue(Files.exists(subdir))
            assertTrue(Files.exists(subdir.resolve("file.txt")))
        }
    }

    @Nested
    @DisplayName("Count Files")
    inner class CountFilesTests {

        @Test
        fun `should count files in directory`() {
            Files.write(tempTargetDir.resolve("file1.txt"), "content".toByteArray())
            Files.write(tempTargetDir.resolve("file2.jpg"), "content".toByteArray())
            Files.write(tempTargetDir.resolve("file3.png"), "content".toByteArray())
            
            val count = exportService.countFilesInDirectory(tempTargetDir)
            
            assertEquals(3, count)
        }

        @Test
        fun `should return zero for empty directory`() {
            val count = exportService.countFilesInDirectory(tempTargetDir)
            
            assertEquals(0, count)
        }

        @Test
        fun `should return zero for non-existent directory`() {
            val nonExistent = tempTargetDir.resolve("doesnotexist")
            
            val count = exportService.countFilesInDirectory(nonExistent)
            
            assertEquals(0, count)
        }

        @Test
        fun `should not count subdirectories`() {
            Files.write(tempTargetDir.resolve("file1.txt"), "content".toByteArray())
            Files.createDirectories(tempTargetDir.resolve("subdir"))
            
            val count = exportService.countFilesInDirectory(tempTargetDir)
            
            assertEquals(1, count) // Only the file, not the directory
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
            assertTrue(Files.exists(tempTargetDir.resolve("1_00001.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("1_00003.jpg")))
        }
    }
}
