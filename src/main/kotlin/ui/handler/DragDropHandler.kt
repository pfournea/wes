package ui.handler

import domain.model.Category
import domain.model.Photo
import domain.service.CategoryService
import domain.service.PhotoService
import javafx.scene.image.ImageView
import javafx.scene.input.DragEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.VBox
import util.ImageUtils
import util.StyleConstants

/**
 * Handles drag and drop operations.
 * Bridges UI drag/drop events to business logic.
 */
class DragDropHandler(
    private val photoService: PhotoService,
    private val categoryService: CategoryService,
    private val imageViews: MutableList<ImageView>,
    private val onPhotosDropped: () -> Unit
) {
    private var draggedImageView: ImageView? = null

    /**
     * Handles drag detected event.
     */
    fun handleDragDetected(event: MouseEvent, imageView: ImageView, selectedImageViews: List<ImageView>) {
        val dragSet = if (selectedImageViews.contains(imageView)) {
            selectedImageViews
        } else {
            listOf(imageView)
        }

        draggedImageView = imageView
        val dragboard = imageView.startDragAndDrop(TransferMode.MOVE)
        val content = javafx.scene.input.ClipboardContent()

        // Transfer indices as comma-separated string
        val indices = dragSet.map { imageViews.indexOf(it) }.joinToString(",")
        content.putString(indices)
        dragboard.setContent(content)
        dragboard.setDragView(imageView.image, event.x, event.y)

        // Set opacity for dragged images
        dragSet.forEach { it.opacity = StyleConstants.DRAG_OPACITY }
        event.consume()
    }

    /**
     * Handles drag done event.
     */
    fun handleDragDone(event: DragEvent, selectedImageViews: List<ImageView>) {
        selectedImageViews.forEach { it.opacity = StyleConstants.NORMAL_OPACITY }
        if (event.transferMode == TransferMode.MOVE) {
            draggedImageView = null
        }
        event.consume()
    }

    /**
     * Handles drag over event on category.
     */
    fun handleDragOver(event: DragEvent) {
        if (draggedImageView != null && event.dragboard.hasString()) {
            event.acceptTransferModes(TransferMode.MOVE)
        }
        event.consume()
    }

    /**
     * Handles drag dropped on category.
     */
    fun handleDragDropped(
        event: DragEvent,
        category: Category,
        photoContainer: VBox
    ): Boolean {
        val dragboard = event.dragboard
        if (!dragboard.hasString()) return false

        val indices = dragboard.string.split(",").mapNotNull { it.toIntOrNull() }
        val photosToMove = indices.mapNotNull { index ->
            imageViews.getOrNull(index)?.let { iv ->
                val photoId = ImageUtils.getPhotoId(iv)
                photoId?.let { photoService.getPhotoById(it) }
            }
        }

        if (photosToMove.isEmpty()) return false

        // Determine drop position
        val mouseY = event.y
        val children = photoContainer.children
        var insertIdx = children.size

        for ((i, node) in children.withIndex()) {
            if (node is ImageView && mouseY < node.layoutY + node.boundsInParent.height / 2) {
                insertIdx = i
                break
            }
        }

        // Remove photos from main grid and add to category
        photosToMove.forEach { photo ->
            // Remove from photo service
            photoService.removePhoto(photo)

            // Remove from any previous category
            val previousCategory = categoryService.findCategoryContainingPhoto(photo)
            previousCategory?.removePhoto(photo)

            // Add to new category
            categoryService.addPhotoToCategory(photo, category, insertIdx)
        }

        // Update ImageViews in category container
        val imageViewsToMove = indices.mapNotNull { imageViews.getOrNull(it) }
        imageViewsToMove.forEach { iv ->
            imageViews.remove(iv)
            iv.parent?.let { parent ->
                if (parent is VBox) parent.children.remove(iv)
            }
            iv.fitWidth = StyleConstants.PHOTO_CATEGORY_WIDTH
            iv.opacity = StyleConstants.NORMAL_OPACITY
            iv.style = "" // Clear selection border when dropped in category
        }

        children.addAll(insertIdx, imageViewsToMove)

        // Notify that photos were dropped
        onPhotosDropped()

        return true
    }

    /**
     * Gets the currently dragged ImageView.
     */
    fun getDraggedImageView(): ImageView? {
        return draggedImageView
    }
}
