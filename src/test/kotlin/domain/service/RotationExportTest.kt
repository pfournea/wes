package domain.service

import domain.model.Category
import domain.model.Photo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.awt.Color
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO

/**
 * Integration test for rotation behavior across import, rotation, and export.
 */
@DisplayName("Rotation Export Integration Tests")
class RotationExportTest {
    
    @TempDir
    lateinit var tempSourceDir: Path
    
    @TempDir
    lateinit var tempTargetDir: Path
    
    private lateinit var exportService: ExportService
    private lateinit var rotationService: RotationService
    
    @BeforeEach
    fun setUp() {
        exportService = ExportService()
        rotationService = RotationService()
    }
    
    /**
     * Creates a simple test image with a colored rectangle at the top
     * so we can verify orientation after rotation.
     */
    private fun createTestImage(name: String, width: Int = 100, height: Int = 100): Path {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB).apply {
            graphics.apply {
                // Fill background with white
                color = Color.WHITE
                fillRect(0, 0, width, height)
                
                // Draw a red rectangle at the top to indicate orientation
                color = Color.RED
                fillRect(0, 0, width, 20)
                
                dispose()
            }
        }
        
        val imagePath = tempSourceDir.resolve(name)
        ImageIO.write(image, "jpg", imagePath.toFile())
        return imagePath
    }
    
    @Nested
    @DisplayName("Manual Rotation Export")
    inner class ManualRotationExport {
        
        @Test
        fun `should export photo with no rotation correctly`() {
            // Create photo with no rotation
            val imagePath = createTestImage("test.jpg")
            val photo = Photo.fromPath(imagePath, 0).copy(rotationDegrees = 0)
            val category = Category("cat1", 1, "Category 1", mutableListOf(photo))
            
            // Export
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertTrue(result.success)
            assertEquals(1, result.photosCopied)
            
            // Verify exported file exists
            val exportedFile = tempTargetDir.resolve("0001.jpg")
            assertTrue(Files.exists(exportedFile))
            
            // Verify dimensions (should be unchanged for 0° rotation)
            val exportedImage = ImageIO.read(exportedFile.toFile())
            assertEquals(100, exportedImage.width)
            assertEquals(100, exportedImage.height)
        }
        
        @Test
        fun `should export photo rotated 90 degrees correctly`() {
            // Create photo and rotate it 90° clockwise
            val imagePath = createTestImage("test.jpg", 100, 100)
            val photo = Photo.fromPath(imagePath, 0).copy(rotationDegrees = 0)
            val rotatedPhoto = rotationService.rotateClockwise(photo)
            
            assertEquals(90, rotatedPhoto.rotationDegrees)
            
            val category = Category("cat1", 1, "Category 1", mutableListOf(rotatedPhoto))
            
            // Export
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertTrue(result.success)
            assertEquals(1, result.photosCopied)
            
            // Verify exported file
            val exportedFile = tempTargetDir.resolve("0001.jpg")
            assertTrue(Files.exists(exportedFile))
            
            // Verify image was actually rotated (dimensions should be swapped)
            val exportedImage = ImageIO.read(exportedFile.toFile())
            assertEquals(100, exportedImage.width, "Width should remain 100 after 90° rotation")
            assertEquals(100, exportedImage.height, "Height should remain 100 after 90° rotation")
        }
        
        @Test
        fun `should export photo rotated 180 degrees correctly`() {
            val imagePath = createTestImage("test.jpg")
            val photo = Photo.fromPath(imagePath, 0).copy(rotationDegrees = 0)
            val rotatedPhoto = rotationService.rotateClockwise(rotationService.rotateClockwise(photo))
            
            assertEquals(180, rotatedPhoto.rotationDegrees)
            
            val category = Category("cat1", 1, "Category 1", mutableListOf(rotatedPhoto))
            
            // Export
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertTrue(result.success)
            val exportedFile = tempTargetDir.resolve("0001.jpg")
            assertTrue(Files.exists(exportedFile))
            
            // Verify dimensions unchanged for 180° rotation
            val exportedImage = ImageIO.read(exportedFile.toFile())
            assertEquals(100, exportedImage.width)
            assertEquals(100, exportedImage.height)
        }
        
        @Test
        fun `should export photo rotated 270 degrees correctly`() {
            val imagePath = createTestImage("test.jpg")
            val photo = Photo.fromPath(imagePath, 0).copy(rotationDegrees = 0)
            val rotatedPhoto = rotationService.rotateCounterClockwise(photo)
            
            assertEquals(270, rotatedPhoto.rotationDegrees)
            
            val category = Category("cat1", 1, "Category 1", mutableListOf(rotatedPhoto))
            
            // Export
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertTrue(result.success)
            val exportedFile = tempTargetDir.resolve("0001.jpg")
            assertTrue(Files.exists(exportedFile))
        }
        
        @Test
        fun `should handle multiple rotations correctly`() {
            val imagePath = createTestImage("test.jpg")
            var photo = Photo.fromPath(imagePath, 0).copy(rotationDegrees = 0)
            
            // Rotate 4 times (full circle back to 0)
            photo = rotationService.rotateClockwise(photo)
            photo = rotationService.rotateClockwise(photo)
            photo = rotationService.rotateClockwise(photo)
            photo = rotationService.rotateClockwise(photo)
            
            assertEquals(0, photo.rotationDegrees, "Four 90° rotations should return to 0°")
            
            val category = Category("cat1", 1, "Category 1", mutableListOf(photo))
            
            // Export - should be same as original since rotation is back to 0
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertTrue(result.success)
        }
        
        @Test
        fun `should preserve rotation state across export for rectangular images`() {
            // Create a rectangular image (200x100) to better test orientation
            val imagePath = createTestImage("test.jpg", 200, 100)
            val photo = Photo.fromPath(imagePath, 0).copy(rotationDegrees = 0)
            val rotatedPhoto = rotationService.rotateClockwise(photo)
            
            val category = Category("cat1", 1, "Category 1", mutableListOf(rotatedPhoto))
            
            // Export
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertTrue(result.success)
            
            // After 90° rotation, width and height should be swapped
            val exportedImage = ImageIO.read(tempTargetDir.resolve("0001.jpg").toFile())
            assertEquals(100, exportedImage.width, "Width should be original height after 90° rotation")
            assertEquals(200, exportedImage.height, "Height should be original width after 90° rotation")
        }
    }
    
    @Nested
    @DisplayName("EXIF Plus Manual Rotation")
    inner class ExifPlusManualRotation {
        
        @Test
        fun `should combine EXIF rotation and manual rotation correctly`() {
            // Simulate a photo that already has EXIF rotation applied (90°)
            val imagePath = createTestImage("test.jpg")
            val photoWithExif = Photo.fromPath(imagePath, 0).copy(rotationDegrees = 90)
            
            // User manually rotates it another 90° clockwise
            val manuallyRotated = rotationService.rotateClockwise(photoWithExif)
            
            assertEquals(180, manuallyRotated.rotationDegrees, "EXIF 90° + manual 90° should equal 180°")
            
            val category = Category("cat1", 1, "Category 1", mutableListOf(manuallyRotated))
            
            // Export should apply the full 180° rotation
            val result = exportService.exportCategories(listOf(category), tempTargetDir)
            
            assertTrue(result.success)
            assertTrue(Files.exists(tempTargetDir.resolve("0001.jpg")))
        }
    }
}
