package domain.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.metadata.IIOMetadataNode

@DisplayName("ExifService")
class ExifServiceTest {
    
    @TempDir
    lateinit var tempDir: Path
    
    private val exifService = ExifService()
    
    @Nested
    @DisplayName("readOrientation")
    inner class ReadOrientation {
        
        @Test
        fun `should return 0 for image without EXIF data`() {
            val imagePath = createImageWithoutExif()
            val rotation = exifService.readOrientation(imagePath)
            assertEquals(0, rotation)
        }
        
        @Test
        fun `should return 0 for non-existent file`() {
            val nonExistentPath = tempDir.resolve("nonexistent.jpg")
            val rotation = exifService.readOrientation(nonExistentPath)
            assertEquals(0, rotation)
        }
        
        @Test
        fun `should return 0 for image with orientation 1 (normal)`() {
            val imagePath = createImageWithExifOrientation(1)
            val rotation = exifService.readOrientation(imagePath)
            assertEquals(0, rotation)
        }
        
        @Test
        fun `should return 180 for image with orientation 3 (upside down)`() {
            val imagePath = createImageWithExifOrientation(3)
            val rotation = exifService.readOrientation(imagePath)
            assertEquals(180, rotation)
        }
        
        @Test
        fun `should return 90 for image with orientation 6 (rotated CW)`() {
            val imagePath = createImageWithExifOrientation(6)
            val rotation = exifService.readOrientation(imagePath)
            assertEquals(90, rotation)
        }
        
        @Test
        fun `should return 270 for image with orientation 8 (rotated CCW)`() {
            val imagePath = createImageWithExifOrientation(8)
            val rotation = exifService.readOrientation(imagePath)
            assertEquals(270, rotation)
        }
        
        @Test
        fun `should return 0 for image with unsupported orientation (mirrored)`() {
            // Orientation 2, 4, 5, 7 involve mirroring, which we don't support
            val imagePath = createImageWithExifOrientation(2)
            val rotation = exifService.readOrientation(imagePath)
            assertEquals(0, rotation)
        }
    }
    
    /**
     * Creates a simple JPEG image without EXIF metadata.
     */
    private fun createImageWithoutExif(): Path {
        val image = BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB).apply {
            graphics.apply {
                color = Color.BLUE
                fillRect(0, 0, 100, 100)
                dispose()
            }
        }
        
        val imagePath = tempDir.resolve("test_no_exif.jpg")
        ImageIO.write(image, "jpg", imagePath.toFile())
        return imagePath
    }
    
    /**
     * Creates a JPEG image with specified EXIF orientation tag.
     * Note: This is a simplified approach that writes EXIF orientation metadata.
     */
    private fun createImageWithExifOrientation(orientation: Int): Path {
        val image = BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB).apply {
            graphics.apply {
                color = Color.GREEN
                fillRect(0, 0, 100, 100)
                dispose()
            }
        }
        
        val imagePath = tempDir.resolve("test_exif_$orientation.jpg")
        
        // Use ImageIO to write with EXIF metadata
        val writers = ImageIO.getImageWritersByFormatName("jpg")
        val writer = writers.next()
        
        val output = ByteArrayOutputStream()
        ImageIO.createImageOutputStream(output).use { ios ->
            writer.output = ios
            
            val writeParam = writer.defaultWriteParam
            val metadata = writer.getDefaultImageMetadata(
                javax.imageio.ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB),
                writeParam
            )
            
            // Add EXIF orientation metadata
            try {
                val root = metadata.getAsTree("javax_imageio_jpeg_image_1.0") as IIOMetadataNode
                val markerSequence = root.getElementsByTagName("markerSequence").item(0) as IIOMetadataNode
                
                // Create APP1 marker for EXIF data
                val app1 = IIOMetadataNode("unknown").apply {
                    setAttribute("MarkerTag", "225") // APP1 marker
                }
                
                // Build minimal EXIF structure with orientation tag
                val exifData = buildExifData(orientation)
                app1.userObject = exifData
                
                markerSequence.appendChild(app1)
                metadata.setFromTree("javax_imageio_jpeg_image_1.0", root)
            } catch (e: Exception) {
                // If EXIF injection fails, continue without it
                // The test will still verify graceful handling
            }
            
            val iioImage = IIOImage(image, null, metadata)
            writer.write(null, iioImage, writeParam)
        }
        
        Files.write(imagePath, output.toByteArray())
        writer.dispose()
        
        return imagePath
    }
    
    /**
     * Builds a minimal EXIF byte array with orientation tag.
     * EXIF structure: TIFF header + IFD0 with orientation tag
     */
    private fun buildExifData(orientation: Int): ByteArray {
        // This is a simplified EXIF structure
        // Real EXIF data would be more complex, but metadata-extractor can read this
        val output = ByteArrayOutputStream()
        
        // EXIF identifier
        output.write("Exif\u0000\u0000".toByteArray())
        
        // TIFF header (little-endian)
        output.write(byteArrayOf(
            0x49, 0x49,  // "II" - Intel byte order (little-endian)
            0x2A, 0x00,  // TIFF magic number (42)
            0x08, 0x00, 0x00, 0x00  // Offset to first IFD (8 bytes from TIFF header start)
        ))
        
        // IFD0 (Image File Directory)
        output.write(byteArrayOf(
            0x01, 0x00  // Number of directory entries (1)
        ))
        
        // Orientation tag entry (12 bytes)
        output.write(byteArrayOf(
            0x12, 0x01,  // Tag number (0x0112 = 274 = Orientation)
            0x03, 0x00,  // Type (3 = SHORT)
            0x01, 0x00, 0x00, 0x00,  // Count (1)
            orientation.toByte(), 0x00, 0x00, 0x00  // Value
        ))
        
        // Offset to next IFD (0 = no more IFDs)
        output.write(byteArrayOf(0x00, 0x00, 0x00, 0x00))
        
        return output.toByteArray()
    }
}
