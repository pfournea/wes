package ui.controller

import domain.model.Photo
import domain.service.FileService
import domain.service.PhotoService
import javafx.scene.image.ImageView
import javafx.stage.FileChooser
import javafx.stage.Stage
import util.ImageCache
import util.ImageUtils
import util.StyleConstants
import java.io.File

class UploadController(
    private val fileService: FileService,
    private val photoService: PhotoService,
    private val onResetState: () -> Unit,
    private val onPhotosLoaded: (List<Photo>, List<ImageView>) -> Unit
) {
    fun handleUploadZip(primaryStage: Stage) {
        val fileChooser = FileChooser().apply {
            title = "Select Zip File"
            extensionFilters.add(FileChooser.ExtensionFilter("Zip Files", "*.zip"))
        }
        fileChooser.showOpenDialog(primaryStage)?.let { loadPhotosFromZip(it) }
    }

    private fun loadPhotosFromZip(zipFile: File) {
        onResetState()
        
        val photos = fileService.extractPhotosFromZip(zipFile)
        photoService.setPhotos(photos)

        ImageCache.clear()

        val imageViews = mutableListOf<ImageView>()
        for (photo in photos) {
            val imageView = ImageUtils.createImageView(photo, StyleConstants.PHOTO_GRID_WIDTH)
            imageViews.add(imageView)
        }

        onPhotosLoaded(photos, imageViews)
    }
}
