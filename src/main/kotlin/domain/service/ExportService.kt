package domain.service

import domain.model.Category
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import javax.imageio.ImageIO

/**
 * Service for exporting categorized photos to a directory.
 * Handles renaming photos based on category and position.
 * Applies rotation transformations during export.
 */
class ExportService {

    /**
     * Exports all categorized photos to the specified directory.
     * The target directory must be empty before exporting.
     * 
     * @param categories List of categories with photos
     * @param targetDirectory Target directory path
     * @return ExportResult with success count and any errors
     */
    fun exportCategories(
        categories: List<Category>,
        targetDirectory: Path
    ): ExportResult {
        val errors = mutableListOf<String>()
        var photosCopied = 0

        try {
            // Ensure target directory exists
            if (!Files.exists(targetDirectory)) {
                Files.createDirectories(targetDirectory)
            }

            // Export photos from each category
            for (category in categories) {
                if (category.photos.isEmpty()) {
                    continue // Skip empty categories
                }

                // Process each photo in the category
                category.photos.forEachIndexed { index, photo ->
                    try {
                        // Position starts at 1
                        val position = index + 1
                        
                        // Extract file extension
                        val extension = photo.fileName.substringAfterLast('.', "")
                        
                        // Generate new filename: <category_number>_<position_5digits>.<ext>
                        val newFilename = generateFilename(category.number, position, extension)
                        
                        // Copy or rotate and save file to target directory
                        val targetFile = targetDirectory.resolve(newFilename)
                        
                        if (photo.rotationDegrees == 0) {
                            // No rotation - simple copy
                            Files.copy(photo.path, targetFile, StandardCopyOption.REPLACE_EXISTING)
                        } else {
                            // Rotation needed - load, rotate, and save
                            saveRotatedImage(photo.path, targetFile, photo.rotationDegrees, extension)
                        }
                        
                        photosCopied++
                    } catch (e: Exception) {
                        errors.add("Failed to copy ${photo.fileName}: ${e.message}")
                    }
                }
            }

            return ExportResult(
                success = errors.isEmpty(),
                photosCopied = photosCopied,
                errors = errors
            )

        } catch (e: Exception) {
            errors.add("Export failed: ${e.message}")
            return ExportResult(
                success = false,
                photosCopied = photosCopied,
                errors = errors
            )
        }
    }

    /**
     * Loads an image, rotates it, and saves to target file.
     * 
     * @param sourcePath Source image path
     * @param targetPath Target save path
     * @param rotationDegrees Rotation angle (90, 180, 270)
     * @param extension File extension to determine format
     */
    private fun saveRotatedImage(sourcePath: Path, targetPath: Path, rotationDegrees: Int, extension: String) {
        // Read the image
        val bufferedImage = ImageIO.read(sourcePath.toFile())
            ?: throw IllegalArgumentException("Failed to read image: $sourcePath")
        
        // Apply rotation
        val rotatedImage = rotateImage(bufferedImage, rotationDegrees)
        
        // Determine image format (default to jpg if unknown)
        val format = when (extension.lowercase()) {
            "jpg", "jpeg" -> "jpg"
            "png" -> "png"
            "gif" -> "gif"
            "bmp" -> "bmp"
            else -> "jpg"
        }
        
        // Save rotated image
        ImageIO.write(rotatedImage, format, targetPath.toFile())
    }

    /**
     * Rotates a BufferedImage by the specified degrees.
     * 
     * @param image Source image
     * @param degrees Rotation angle (90, 180, 270)
     * @return Rotated image
     */
    private fun rotateImage(image: BufferedImage, degrees: Int): BufferedImage {
        val radians = Math.toRadians(degrees.toDouble())
        val sin = Math.abs(Math.sin(radians))
        val cos = Math.abs(Math.cos(radians))
        
        val width = image.width
        val height = image.height
        
        // Calculate new dimensions after rotation
        val newWidth = (width * cos + height * sin).toInt()
        val newHeight = (width * sin + height * cos).toInt()
        
        // Create transform
        val transform = AffineTransform()
        transform.translate((newWidth - width) / 2.0, (newHeight - height) / 2.0)
        transform.rotate(radians, width / 2.0, height / 2.0)
        
        val op = AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR)
        
        // Create new image with rotated dimensions
        val rotatedImage = BufferedImage(newWidth, newHeight, image.type)
        return op.filter(image, rotatedImage)
    }

    /**
     * Generates filename for a photo based on category number and position.
     * Format: 
     * - First photo: <category_4digits>.<extension> (e.g., 0005.jpg)
     * - Other photos: <category_4digits>-<position_2digits>.<extension> (e.g., 0005-01.jpg)
     * 
     * @param categoryNumber Category number (1-9999)
     * @param position Position in category (1-based, 1-99)
     * @param extension File extension
     * @return Generated filename
     */
    private fun generateFilename(categoryNumber: Int, position: Int, extension: String): String {
        val paddedCategory = categoryNumber.toString().padStart(4, '0')
        
        val filename = if (position == 1) {
            // First photo: 0005.jpg
            paddedCategory
        } else {
            // Subsequent photos: 0005-01.jpg, 0005-02.jpg, etc.
            val paddedPosition = (position - 1).toString().padStart(2, '0')
            "${paddedCategory}-${paddedPosition}"
        }
        
        return if (extension.isNotEmpty()) {
            "${filename}.${extension}"
        } else {
            filename
        }
    }

    /**
     * Checks whether a directory is empty (contains no files or subdirectories).
     * 
     * @param directory Directory to check
     * @return true if the directory is empty or does not exist, false otherwise
     */
    fun isDirectoryEmpty(directory: Path): Boolean {
        if (!Files.exists(directory)) {
            return true
        }
        
        return Files.list(directory).use { stream ->
            !stream.findFirst().isPresent
        }
    }
}

/**
 * Result of export operation.
 */
data class ExportResult(
    val success: Boolean,
    val photosCopied: Int,
    val errors: List<String> = emptyList()
)
