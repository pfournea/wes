package domain.service

import domain.model.Photo
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipFile

/**
 * Service for handling file operations.
 * Manages zip extraction and image file validation.
 */
class FileService {
    private val supportedExtensions = setOf("jpg", "jpeg", "png", "gif", "bmp")

    /**
     * Extracts photos from a zip file and returns them in order.
     */
    fun extractPhotosFromZip(zipFile: File): List<Photo> {
        val tempDir = Files.createTempDirectory("photo_categorizer")
        val imageFiles = mutableListOf<Path>()

        ZipFile(zipFile).use { zip ->
            for (entry in zip.entries()) {
                if (!entry.isDirectory && isImageFile(entry.name)) {
                    val extractedFile = tempDir.resolve(entry.name)
                    Files.createDirectories(extractedFile.parent)
                    zip.getInputStream(entry).use { input ->
                        Files.copy(input, extractedFile)
                    }
                    imageFiles.add(extractedFile)
                }
            }
        }

        return imageFiles.mapIndexed { index, path ->
            Photo.fromPath(path, index)
        }
    }

    /**
     * Checks if a filename represents an image file.
     */
    fun isImageFile(filename: String): Boolean {
        val extension = filename.substringAfterLast('.', "").lowercase()
        return extension in supportedExtensions
    }

    /**
     * Gets the list of supported image extensions.
     */
    fun getSupportedExtensions(): Set<String> {
        return supportedExtensions.toSet()
    }
}
