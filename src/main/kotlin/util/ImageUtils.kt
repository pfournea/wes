package util

import domain.model.Photo
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import java.nio.file.Path

/**
 * Utility functions for working with images.
 */
object ImageUtils {
    /**
     * Creates an ImageView from a Photo model.
     * Uses ImageCache for performance.
     */
    fun createImageView(photo: Photo, width: Double, preserveRatio: Boolean = true): ImageView {
        val image = ImageCache.getImage(photo.path, width)
        return ImageView(image).apply {
            fitWidth = width
            isPreserveRatio = preserveRatio
            isSmooth = false  // No high-quality filtering needed
            // Store photo ID as user data for easy retrieval
            userData = photo.id
        }
    }

    /**
     * Gets the photo ID from an ImageView.
     */
    fun getPhotoId(imageView: ImageView): String? {
        return imageView.userData as? String
    }

    /**
     * Checks if a file path represents a supported image.
     */
    fun isSupportedImage(path: Path): Boolean {
        val filename = path.fileName.toString().lowercase()
        return filename.endsWith(".jpg") ||
                filename.endsWith(".jpeg") ||
                filename.endsWith(".png") ||
                filename.endsWith(".gif") ||
                filename.endsWith(".bmp")
    }
}
