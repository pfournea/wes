package ui.controller

import domain.model.Category
import domain.model.Photo
import domain.service.CategoryService
import domain.service.PhotoService
import domain.service.RotationService
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.TilePane
import ui.component.PhotoCard
import ui.handler.DragDropHandler
import ui.handler.ReorderDragDropHandler
import ui.handler.SelectionHandler
import util.ImageUtils
import util.StyleConstants

class PhotoGridController(
    private val photoService: PhotoService,
    private val categoryService: CategoryService,
    private val imageContainer: TilePane,
    private val imageViews: MutableList<ImageView>,
    private val dragDropHandler: DragDropHandler,
    private val reorderDragDropHandler: ReorderDragDropHandler,
    private val selectionHandler: SelectionHandler,
    private val onPhotoRemoved: (Category) -> Unit = {}
) {
    private var currentCategory: Category? = null
    private val photoCards = mutableMapOf<ImageView, PhotoCard>()
    private val rotationService = RotationService()

    fun getImageViews(): MutableList<ImageView> = imageViews

    fun updateForCategory(category: Category?) {
        imageViews.clear()
        photoCards.clear()
        currentCategory = category

        val latestCategory = category?.let { categoryService.getCategoryById(it.id) }

        val photosToShow = if (latestCategory != null) {
            latestCategory.photos
        } else {
            photoService.getPhotos()
        }

        if (photosToShow.isEmpty() && latestCategory != null) {
            imageContainer.children.clear()
            imageContainer.children.add(Label("No photos in this category").apply {
                style = "-fx-font-size: 16; -fx-text-fill: #999999; -fx-padding: 20;"
            })
            return
        }

        for (photo in photosToShow) {
            val imageView = ImageUtils.createImageView(photo, StyleConstants.PHOTO_GRID_WIDTH)

            imageView.setOnMouseClicked { event ->
                selectionHandler.handleImageClick(event, imageView)
            }

            if (latestCategory != null) {
                val photoCard = PhotoCard(
                    imageView = imageView,
                    photo = photo,
                    onDeleteRequested = { handlePhotoDelete(photo, latestCategory) },
                    onRotateLeft = { handleRotateLeft(photo, latestCategory) },
                    onRotateRight = { handleRotateRight(photo, latestCategory) },
                    isInCategory = true
                )
                photoCards[imageView] = photoCard
                setupCategoryViewDragHandlers(imageView)
            } else {
                val photoCard = PhotoCard(
                    imageView = imageView,
                    photo = photo,
                    onRotateLeft = { handleRotateLeft(photo, null) },
                    onRotateRight = { handleRotateRight(photo, null) },
                    isInCategory = false
                )
                photoCards[imageView] = photoCard
                setupImageViewHandlers(imageView)
            }

            imageViews.add(imageView)
        }

        if (latestCategory != null) {
            imageContainer.setOnDragOver { event ->
                reorderDragDropHandler.handleDragOver(event, imageContainer, latestCategory)
            }
            imageContainer.setOnDragDropped { event ->
                reorderDragDropHandler.handleDragDropped(event, latestCategory, imageContainer)
                event.isDropCompleted = true
                event.consume()
            }
            imageContainer.setOnDragExited { event ->
                reorderDragDropHandler.handleDragExited(event, imageContainer)
            }
        } else {
            imageContainer.onDragOver = null
            imageContainer.onDragDropped = null
            imageContainer.onDragExited = null
        }

        updateImageDisplay()
    }

    private fun handleRotateLeft(photo: Photo, category: Category?) {
        val rotatedPhoto = rotationService.rotateCounterClockwise(photo)
        
        if (category != null) {
            // Photo is in a category
            categoryService.updatePhotoRotationInCategory(photo.id, category.id, rotatedPhoto.rotationDegrees)
            updateForCategory(category)
        } else {
            // Photo is in main grid
            photoService.updatePhotoRotation(photo.id, rotatedPhoto.rotationDegrees)
            updateForCategory(null)
        }
    }

    private fun handleRotateRight(photo: Photo, category: Category?) {
        val rotatedPhoto = rotationService.rotateClockwise(photo)
        
        if (category != null) {
            // Photo is in a category
            categoryService.updatePhotoRotationInCategory(photo.id, category.id, rotatedPhoto.rotationDegrees)
            updateForCategory(category)
        } else {
            // Photo is in main grid
            photoService.updatePhotoRotation(photo.id, rotatedPhoto.rotationDegrees)
            updateForCategory(null)
        }
    }

    private fun handlePhotoDelete(photo: Photo, category: Category) {
        // Show confirmation dialog
        val alert = Alert(Alert.AlertType.CONFIRMATION)
        alert.title = "Remove Photo"
        alert.headerText = "Remove \"${photo.fileName}\"?"
        alert.contentText = "This photo will be removed from the category and returned to the main grid."

        val result = alert.showAndWait()
        if (result.isPresent && result.get() == ButtonType.OK) {
            // Remove photo from category
            val updatedCategory = categoryService.removePhotoFromCategory(photo, category)
            
            // Restore photo to main grid
            photoService.restorePhotos(listOf(photo))
            
            // Refresh the category view
            updateForCategory(updatedCategory)
            
            // Notify about the removal so CategoryController can refresh
            onPhotoRemoved(updatedCategory)
        }
    }

    fun setupImageViewHandlers(imageView: ImageView) {
        imageView.setOnMouseClicked { event ->
            selectionHandler.handleImageClick(event, imageView)
        }
        imageView.setOnDragDetected { event ->
            dragDropHandler.handleDragDetected(event, imageView, selectionHandler.getSelectedImageViews())
        }
        imageView.setOnDragDone { event ->
            dragDropHandler.handleDragDone(event, selectionHandler.getSelectedImageViews())
        }
    }

    private fun setupCategoryViewDragHandlers(imageView: ImageView) {
        imageView.setOnDragDetected { event ->
            dragDropHandler.handleDragDetected(event, imageView, selectionHandler.getSelectedImageViews())
        }
        imageView.setOnDragDone { event ->
            dragDropHandler.handleDragDone(event, selectionHandler.getSelectedImageViews())
        }
    }

    fun updateImageDisplay() {
        imageContainer.children.clear()
        
        // Create PhotoCards for any ImageViews that don't have them yet
        for (imageView in imageViews) {
            if (imageView !in photoCards) {
                // Find the corresponding photo
                val photoId = ImageUtils.getPhotoId(imageView) ?: continue
                val photo = photoService.getPhotoById(photoId) 
                    ?: categoryService.findPhotoById(photoId) 
                    ?: continue
                
                val photoCard = PhotoCard(
                    imageView = imageView,
                    photo = photo,
                    onDeleteRequested = { /* Will be handled if in category */ },
                    onRotateLeft = { handleRotateLeft(photo, currentCategory) },
                    onRotateRight = { handleRotateRight(photo, currentCategory) },
                    isInCategory = currentCategory != null
                )
                photoCards[imageView] = photoCard
            }
        }
        
        // Add PhotoCard containers which wrap ImageViews with delete button
        imageContainer.children.addAll(photoCards.values.map { it.container })
        selectionHandler.updateVisualSelection()
    }

    /**
     * Gets the PhotoCard for an ImageView
     */
    fun getPhotoCard(imageView: ImageView): PhotoCard? = photoCards[imageView]
}
