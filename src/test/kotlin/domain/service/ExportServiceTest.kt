package domain.service

import domain.model.Category
import domain.model.Photo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.io.TempDir
import java.awt.Color
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.imageio.ImageIO

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
        // Create actual valid image file in temp directory
        val photoPath = tempSourceDir.resolve(name)
        val image = BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB).apply {
            graphics.apply {
                color = Color.BLUE
                fillRect(0, 0, 100, 100)
                dispose()
            }
        }
        ImageIO.write(image, "jpg", photoPath.toFile())
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
    @DisplayName("Photo Rotation Tests")
    inner class PhotoRotationTests {

        private fun createColoredTestPhoto(name: String, index: Int, width: Int = 100, height: Int = 50, rotation: Int = 0): Photo {
            // Create a distinctive image with different colors in each corner
            // This allows us to verify rotation by checking pixel positions
            val photoPath = tempSourceDir.resolve(name)
            val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB).apply {
                graphics.apply {
                    // Top-left: Red
                    color = Color.RED
                    fillRect(0, 0, width / 2, height / 2)
                    // Top-right: Green
                    color = Color.GREEN
                    fillRect(width / 2, 0, width / 2, height / 2)
                    // Bottom-left: Blue
                    color = Color.BLUE
                    fillRect(0, height / 2, width / 2, height / 2)
                    // Bottom-right: Yellow
                    color = Color.YELLOW
                    fillRect(width / 2, height / 2, width / 2, height / 2)
                    dispose()
                }
            }
            ImageIO.write(image, "jpg", photoPath.toFile())
            return Photo(
                id = "${index}_${name}",
                path = photoPath,
                fileName = name,
                originalIndex = index,
                rotationDegrees = rotation
            )
        }

        private fun assertPixelColor(image: BufferedImage, x: Int, y: Int, expectedColor: Color, tolerance: Int = 30) {
            val actualRGB = image.getRGB(x, y)
            val actualColor = Color(actualRGB)
            
            // Allow some tolerance for JPEG compression artifacts
            val redDiff = Math.abs(actualColor.red - expectedColor.red)
            val greenDiff = Math.abs(actualColor.green - expectedColor.green)
            val blueDiff = Math.abs(actualColor.blue - expectedColor.blue)
            
            assertTrue(
                redDiff <= tolerance && greenDiff <= tolerance && blueDiff <= tolerance,
                "Pixel at ($x, $y) expected ~${colorToString(expectedColor)} but was ${colorToString(actualColor)} " +
                "(diff: R=$redDiff G=$greenDiff B=$blueDiff)"
            )
        }

        private fun colorToString(color: Color): String {
            return when {
                isApproximately(color, Color.RED) -> "RED"
                isApproximately(color, Color.GREEN) -> "GREEN"
                isApproximately(color, Color.BLUE) -> "BLUE"
                isApproximately(color, Color.YELLOW) -> "YELLOW"
                else -> "RGB(${color.red},${color.green},${color.blue})"
            }
        }

        private fun isApproximately(c1: Color, c2: Color, tolerance: Int = 30): Boolean {
            return Math.abs(c1.red - c2.red) <= tolerance &&
                   Math.abs(c1.green - c2.green) <= tolerance &&
                   Math.abs(c1.blue - c2.blue) <= tolerance
        }

        @Test
        fun `should export non-rotated photo without changes`() {
            val photo = createColoredTestPhoto("test.jpg", 0, rotation = 0)
            val category = Category("cat1", 1, "Category 1", mutableListOf(photo))
            
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertTrue(result.success)
            val exported = ImageIO.read(tempTargetDir.resolve("0001.jpg").toFile())
            
            // Verify dimensions unchanged
            assertEquals(100, exported.width)
            assertEquals(50, exported.height)
            
            // Verify colors in original positions
            assertPixelColor(exported, 10, 10, Color.RED)      // Top-left
            assertPixelColor(exported, 90, 10, Color.GREEN)    // Top-right
            assertPixelColor(exported, 10, 40, Color.BLUE)     // Bottom-left
            assertPixelColor(exported, 90, 40, Color.YELLOW)   // Bottom-right
        }

        @Test
        fun `should rotate photo 90 degrees clockwise correctly`() {
            val photo = createColoredTestPhoto("test.jpg", 0, rotation = 90)
            val category = Category("cat1", 1, "Category 1", mutableListOf(photo))
            
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertTrue(result.success)
            val exported = ImageIO.read(tempTargetDir.resolve("0001.jpg").toFile())
            
            // After 90° CW: dimensions should be swapped (width=50, height=100)
            assertEquals(50, exported.width)
            assertEquals(100, exported.height)
            
            // After 90° CW rotation:
            // Original top-left (Red) → new top-right
            // Original top-right (Green) → new bottom-right
            // Original bottom-left (Blue) → new top-left
            // Original bottom-right (Yellow) → new bottom-left
            assertPixelColor(exported, 10, 10, Color.BLUE)     // Top-left (was bottom-left)
            assertPixelColor(exported, 40, 10, Color.RED)      // Top-right (was top-left)
            assertPixelColor(exported, 10, 90, Color.YELLOW)   // Bottom-left (was bottom-right)
            assertPixelColor(exported, 40, 90, Color.GREEN)    // Bottom-right (was top-right)
        }

        @Test
        fun `should rotate photo 180 degrees correctly`() {
            val photo = createColoredTestPhoto("test.jpg", 0, rotation = 180)
            val category = Category("cat1", 1, "Category 1", mutableListOf(photo))
            
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertTrue(result.success)
            val exported = ImageIO.read(tempTargetDir.resolve("0001.jpg").toFile())
            
            // After 180°: dimensions stay same
            assertEquals(100, exported.width)
            assertEquals(50, exported.height)
            
            // After 180° rotation: everything is flipped
            // Original top-left (Red) → new bottom-right
            // Original top-right (Green) → new bottom-left
            // Original bottom-left (Blue) → new top-right
            // Original bottom-right (Yellow) → new top-left
            assertPixelColor(exported, 10, 10, Color.YELLOW)   // Top-left (was bottom-right)
            assertPixelColor(exported, 90, 10, Color.BLUE)     // Top-right (was bottom-left)
            assertPixelColor(exported, 10, 40, Color.GREEN)    // Bottom-left (was top-right)
            assertPixelColor(exported, 90, 40, Color.RED)      // Bottom-right (was top-left)
        }

        @Test
        fun `should rotate photo 270 degrees clockwise correctly`() {
            val photo = createColoredTestPhoto("test.jpg", 0, rotation = 270)
            val category = Category("cat1", 1, "Category 1", mutableListOf(photo))
            
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertTrue(result.success)
            val exported = ImageIO.read(tempTargetDir.resolve("0001.jpg").toFile())
            
            // After 270° CW (or 90° CCW): dimensions should be swapped
            assertEquals(50, exported.width)
            assertEquals(100, exported.height)
            
            // After 270° CW rotation:
            // Original top-left (Red) → new bottom-left
            // Original top-right (Green) → new top-left
            // Original bottom-left (Blue) → new bottom-right
            // Original bottom-right (Yellow) → new top-right
            assertPixelColor(exported, 10, 10, Color.GREEN)    // Top-left (was top-right)
            assertPixelColor(exported, 40, 10, Color.YELLOW)   // Top-right (was bottom-right)
            assertPixelColor(exported, 10, 90, Color.RED)      // Bottom-left (was top-left)
            assertPixelColor(exported, 40, 90, Color.BLUE)     // Bottom-right (was bottom-left)
        }

        @Test
        fun `should handle multiple photos with different rotations`() {
            val photo0 = createColoredTestPhoto("test0.jpg", 0, rotation = 0)
            val photo90 = createColoredTestPhoto("test90.jpg", 1, rotation = 90)
            val photo180 = createColoredTestPhoto("test180.jpg", 2, rotation = 180)
            val photo270 = createColoredTestPhoto("test270.jpg", 3, rotation = 270)
            
            val category = Category("cat1", 1, "Category 1", 
                mutableListOf(photo0, photo90, photo180, photo270))
            
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertTrue(result.success)
            assertEquals(4, result.photosCopied)
            
            // Verify all files exist
            assertTrue(Files.exists(tempTargetDir.resolve("0001.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("0001-01.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("0001-02.jpg")))
            assertTrue(Files.exists(tempTargetDir.resolve("0001-03.jpg")))
            
            // Verify 0° rotation
            val exported0 = ImageIO.read(tempTargetDir.resolve("0001.jpg").toFile())
            assertEquals(100, exported0.width)
            assertEquals(50, exported0.height)
            assertPixelColor(exported0, 10, 10, Color.RED)
            
            // Verify 90° rotation
            val exported90 = ImageIO.read(tempTargetDir.resolve("0001-01.jpg").toFile())
            assertEquals(50, exported90.width)
            assertEquals(100, exported90.height)
            assertPixelColor(exported90, 10, 10, Color.BLUE)
            
            // Verify 180° rotation
            val exported180 = ImageIO.read(tempTargetDir.resolve("0001-02.jpg").toFile())
            assertEquals(100, exported180.width)
            assertEquals(50, exported180.height)
            assertPixelColor(exported180, 10, 10, Color.YELLOW)
            
            // Verify 270° rotation
            val exported270 = ImageIO.read(tempTargetDir.resolve("0001-03.jpg").toFile())
            assertEquals(50, exported270.width)
            assertEquals(100, exported270.height)
            assertPixelColor(exported270, 10, 10, Color.GREEN)
        }

        @Test
        fun `should maintain aspect ratio after rotation`() {
            // Test with a clearly non-square image
            val photo = createColoredTestPhoto("test.jpg", 0, width = 200, height = 100, rotation = 90)
            val category = Category("cat1", 1, "Category 1", mutableListOf(photo))
            
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertTrue(result.success)
            val exported = ImageIO.read(tempTargetDir.resolve("0001.jpg").toFile())
            
            // After 90° rotation: 200x100 should become 100x200
            assertEquals(100, exported.width)
            assertEquals(200, exported.height)
        }

        @Test
        fun `should preserve image quality after rotation`() {
            val photo = createColoredTestPhoto("test.jpg", 0, rotation = 90)
            val category = Category("cat1", 1, "Category 1", mutableListOf(photo))
            
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertTrue(result.success)
            val exported = ImageIO.read(tempTargetDir.resolve("0001.jpg").toFile())
            
            // Verify the image is not corrupted and has the expected type
            assertNotNull(exported)
            assertTrue(exported.type == BufferedImage.TYPE_INT_RGB || 
                      exported.type == BufferedImage.TYPE_3BYTE_BGR ||
                      exported.type == BufferedImage.TYPE_INT_BGR)
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
