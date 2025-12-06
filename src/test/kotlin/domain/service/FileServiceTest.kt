package domain.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@DisplayName("FileService Tests")
class FileServiceTest {

    private lateinit var fileService: FileService

    @BeforeEach
    fun setUp() {
        fileService = FileService()
    }

    @Nested
    @DisplayName("Image File Validation")
    inner class ImageFileValidationTests {

        @Test
        fun `should recognize jpg files`() {
            assertTrue(fileService.isImageFile("photo.jpg"))
            assertTrue(fileService.isImageFile("photo.JPG"))
            assertTrue(fileService.isImageFile("photo.jpeg"))
            assertTrue(fileService.isImageFile("photo.JPEG"))
        }

        @Test
        fun `should recognize png files`() {
            assertTrue(fileService.isImageFile("photo.png"))
            assertTrue(fileService.isImageFile("photo.PNG"))
        }

        @Test
        fun `should recognize gif files`() {
            assertTrue(fileService.isImageFile("photo.gif"))
            assertTrue(fileService.isImageFile("photo.GIF"))
        }

        @Test
        fun `should recognize bmp files`() {
            assertTrue(fileService.isImageFile("photo.bmp"))
            assertTrue(fileService.isImageFile("photo.BMP"))
        }

        @Test
        fun `should reject non-image files`() {
            assertFalse(fileService.isImageFile("document.txt"))
            assertFalse(fileService.isImageFile("document.pdf"))
            assertFalse(fileService.isImageFile("archive.zip"))
            assertFalse(fileService.isImageFile("video.mp4"))
        }

        @Test
        fun `should handle files without extensions`() {
            assertFalse(fileService.isImageFile("noextension"))
        }

        @Test
        fun `should handle files with multiple dots`() {
            assertTrue(fileService.isImageFile("my.photo.backup.jpg"))
            assertFalse(fileService.isImageFile("my.photo.backup.txt"))
        }

        @Test
        fun `should handle empty filename`() {
            assertFalse(fileService.isImageFile(""))
        }

        @Test
        fun `should be case insensitive`() {
            assertTrue(fileService.isImageFile("photo.JpG"))
            assertTrue(fileService.isImageFile("photo.PnG"))
            assertTrue(fileService.isImageFile("photo.GiF"))
        }
    }

    @Nested
    @DisplayName("Zip File Extraction")
    inner class ZipFileExtractionTests {

        @Test
        fun `should extract image files from zip`(@TempDir tempDir: Path) {
            // Create a test zip file
            val zipFile = tempDir.resolve("test.zip").toFile()
            createZipWithImages(zipFile, listOf("photo1.jpg", "photo2.png", "photo3.gif"))

            val photos = fileService.extractPhotosFromZip(zipFile)

            assertEquals(3, photos.size)
            assertEquals("photo1.jpg", photos[0].fileName)
            assertEquals("photo2.png", photos[1].fileName)
            assertEquals("photo3.gif", photos[2].fileName)
        }

        @Test
        fun `should maintain order of photos from zip`(@TempDir tempDir: Path) {
            val zipFile = tempDir.resolve("test.zip").toFile()
            val imageFiles = listOf("a.jpg", "b.jpg", "c.jpg", "d.jpg")
            createZipWithImages(zipFile, imageFiles)

            val photos = fileService.extractPhotosFromZip(zipFile)

            assertEquals(4, photos.size)
            for (i in imageFiles.indices) {
                assertEquals(imageFiles[i], photos[i].fileName)
                assertEquals(i, photos[i].originalIndex)
            }
        }

        @Test
        fun `should filter out non-image files`(@TempDir tempDir: Path) {
            val zipFile = tempDir.resolve("test.zip").toFile()
            createZipWithMixedFiles(
                zipFile,
                mapOf(
                    "photo1.jpg" to "image",
                    "document.txt" to "text",
                    "photo2.png" to "image",
                    "data.json" to "data"
                )
            )

            val photos = fileService.extractPhotosFromZip(zipFile)

            assertEquals(2, photos.size)
            assertEquals("photo1.jpg", photos[0].fileName)
            assertEquals("photo2.png", photos[1].fileName)
        }

        @Test
        fun `should skip directories in zip`(@TempDir tempDir: Path) {
            val zipFile = tempDir.resolve("test.zip").toFile()
            createZipWithDirectories(zipFile)

            val photos = fileService.extractPhotosFromZip(zipFile)

            // Should only extract files, not directories
            assertEquals(2, photos.size)
        }

        @Test
        fun `should handle empty zip file`(@TempDir tempDir: Path) {
            val zipFile = tempDir.resolve("empty.zip").toFile()
            createEmptyZip(zipFile)

            val photos = fileService.extractPhotosFromZip(zipFile)

            assertTrue(photos.isEmpty())
        }

        @Test
        fun `should handle zip with only non-image files`(@TempDir tempDir: Path) {
            val zipFile = tempDir.resolve("test.zip").toFile()
            createZipWithMixedFiles(
                zipFile,
                mapOf(
                    "document.txt" to "text",
                    "data.json" to "data",
                    "video.mp4" to "video"
                )
            )

            val photos = fileService.extractPhotosFromZip(zipFile)

            assertTrue(photos.isEmpty())
        }

        @Test
        fun `should assign correct original indices`(@TempDir tempDir: Path) {
            val zipFile = tempDir.resolve("test.zip").toFile()
            createZipWithImages(zipFile, listOf("img1.jpg", "img2.jpg", "img3.jpg"))

            val photos = fileService.extractPhotosFromZip(zipFile)

            assertEquals(0, photos[0].originalIndex)
            assertEquals(1, photos[1].originalIndex)
            assertEquals(2, photos[2].originalIndex)
        }

        @Test
        fun `should extract images from nested directories in zip`(@TempDir tempDir: Path) {
            val zipFile = tempDir.resolve("test.zip").toFile()
            createZipWithNestedImages(zipFile)

            val photos = fileService.extractPhotosFromZip(zipFile)

            assertTrue(photos.size >= 2)
            assertTrue(photos.any { it.fileName == "photo1.jpg" })
            assertTrue(photos.any { it.fileName == "photo2.png" })
        }

        // Helper methods to create test zip files

        private fun createZipWithImages(zipFile: File, imageNames: List<String>) {
            ZipOutputStream(zipFile.outputStream()).use { zos ->
                imageNames.forEach { name ->
                    val entry = ZipEntry(name)
                    zos.putNextEntry(entry)
                    zos.write("fake image data".toByteArray())
                    zos.closeEntry()
                }
            }
        }

        private fun createZipWithMixedFiles(zipFile: File, files: Map<String, String>) {
            ZipOutputStream(zipFile.outputStream()).use { zos ->
                files.forEach { (name, content) ->
                    val entry = ZipEntry(name)
                    zos.putNextEntry(entry)
                    zos.write(content.toByteArray())
                    zos.closeEntry()
                }
            }
        }

        private fun createZipWithDirectories(zipFile: File) {
            ZipOutputStream(zipFile.outputStream()).use { zos ->
                // Add directory entries
                zos.putNextEntry(ZipEntry("photos/"))
                zos.closeEntry()
                
                // Add file entries
                zos.putNextEntry(ZipEntry("photos/photo1.jpg"))
                zos.write("image1".toByteArray())
                zos.closeEntry()
                
                zos.putNextEntry(ZipEntry("photos/photo2.jpg"))
                zos.write("image2".toByteArray())
                zos.closeEntry()
            }
        }

        private fun createZipWithNestedImages(zipFile: File) {
            ZipOutputStream(zipFile.outputStream()).use { zos ->
                zos.putNextEntry(ZipEntry("folder1/photo1.jpg"))
                zos.write("image1".toByteArray())
                zos.closeEntry()
                
                zos.putNextEntry(ZipEntry("folder1/subfolder/photo2.png"))
                zos.write("image2".toByteArray())
                zos.closeEntry()
            }
        }

        private fun createEmptyZip(zipFile: File) {
            ZipOutputStream(zipFile.outputStream()).use { 
                // Just close it - empty zip
            }
        }
    }

    @Nested
    @DisplayName("Supported Extensions")
    inner class SupportedExtensionsTests {

        @Test
        fun `should return all supported extensions`() {
            val extensions = fileService.getSupportedExtensions()

            assertTrue(extensions.contains("jpg"))
            assertTrue(extensions.contains("jpeg"))
            assertTrue(extensions.contains("png"))
            assertTrue(extensions.contains("gif"))
            assertTrue(extensions.contains("bmp"))
        }

        @Test
        fun `should return immutable copy of extensions`() {
            val extensions1 = fileService.getSupportedExtensions()
            val extensions2 = fileService.getSupportedExtensions()

            // Should be equal but not the same instance
            assertEquals(extensions1, extensions2)
        }

        @Test
        fun `should have correct number of supported extensions`() {
            val extensions = fileService.getSupportedExtensions()

            assertEquals(5, extensions.size)
        }
    }
}
