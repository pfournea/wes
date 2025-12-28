package ui.controller

import domain.model.Category
import domain.service.CategoryService
import domain.service.PhotoService
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.layout.TilePane
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
    private val selectionHandler: SelectionHandler
) {
    fun getImageViews(): MutableList<ImageView> = imageViews

    fun updateForCategory(category: Category?) {
        imageViews.clear()

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
                setupCategoryViewDragHandlers(imageView)
            } else {
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
        imageContainer.children.addAll(imageViews)
        selectionHandler.updateVisualSelection()
    }
}
