package util

import domain.model.Photo
import domain.service.FileService
import javafx.scene.image.ImageView
import java.nio.file.Path

object ImageUtils {
    private val fileService = FileService()

    fun createImageView(photo: Photo, width: Double, preserveRatio: Boolean = true): ImageView {
        val image = ImageCache.getImage(photo.path, width)
        return ImageView(image).apply {
            fitWidth = width
            isPreserveRatio = preserveRatio
            isSmooth = false
            userData = photo.id
            
            // Apply rotation transform
            if (photo.rotationDegrees != 0) {
                applyRotation(this, photo.rotationDegrees)
            }
        }
    }

    /**
     * Applies rotation transform to an ImageView.
     * The image stays centered in its original bounds.
     * 
     * @param imageView ImageView to rotate
     * @param degrees Rotation in degrees (0, 90, 180, 270)
     */
    fun applyRotation(imageView: ImageView, degrees: Int) {
        if (degrees == 0) {
            imageView.transforms.clear()
            imageView.rotate = 0.0
            return
        }
        
        // Use JavaFX rotate property which rotates around the center
        // This keeps the image in its grid position
        imageView.rotate = degrees.toDouble()
    }

    fun getPhotoId(imageView: ImageView): String? {
        return imageView.userData as? String
    }

    fun isSupportedImage(path: Path): Boolean {
        return fileService.isImageFile(path.fileName.toString())
    }
}
