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
        }
    }

    fun getPhotoId(imageView: ImageView): String? {
        return imageView.userData as? String
    }

    fun isSupportedImage(path: Path): Boolean {
        return fileService.isImageFile(path.fileName.toString())
    }
}
