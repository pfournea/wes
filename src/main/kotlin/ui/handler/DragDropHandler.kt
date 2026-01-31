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

class DragDropHandler(
    private val photoService: PhotoService,
    private val categoryService: CategoryService,
    private val imageViews: MutableList<ImageView>,
    private val onPhotosDropped: () -> Unit,
    private val onSourceCategoryUpdated: (String) -> Unit = {}
) {
    private var draggedImageView: ImageView? = null

    fun handleDragDetected(event: MouseEvent, imageView: ImageView, selectedImageViews: List<ImageView>) {
        val dragSet = if (selectedImageViews.contains(imageView)) {
            selectedImageViews
        } else {
            listOf(imageView)
        }

        draggedImageView = imageView
        val dragboard = imageView.startDragAndDrop(TransferMode.MOVE)
        val content = javafx.scene.input.ClipboardContent()

        val indices = dragSet.map { imageViews.indexOf(it) }.joinToString(",")
        content.putString(indices)
        dragboard.setContent(content)
        dragboard.setDragView(imageView.image, event.x, event.y)

        dragSet.forEach { it.opacity = StyleConstants.DRAG_OPACITY }
        event.consume()
    }

    fun handleDragDone(event: DragEvent, selectedImageViews: List<ImageView>) {
        selectedImageViews.forEach { it.opacity = StyleConstants.NORMAL_OPACITY }
        if (event.transferMode == TransferMode.MOVE) {
            draggedImageView = null
        }
        event.consume()
    }

    fun handleDragOver(event: DragEvent) {
        if (draggedImageView != null && event.dragboard.hasString()) {
            event.acceptTransferModes(TransferMode.MOVE)
        }
        event.consume()
    }

    fun handleDragDropped(
        event: DragEvent,
        category: Category,
        photoContainer: VBox
    ): Boolean {
        val dragboard = event.dragboard
        if (!dragboard.hasString()) return false

        val indices = dragboard.string.split(",").mapNotNull { it.toIntOrNull() }
        val sortedIndices = indices.sorted()
        
        val photosToMove = sortedIndices.mapNotNull { index ->
            imageViews.getOrNull(index)?.let { iv ->
                val photoId = ImageUtils.getPhotoId(iv)
                photoId?.let { 
                    photoService.getPhotoById(it) ?: categoryService.findPhotoById(it)
                }
            }
        }

        if (photosToMove.isEmpty()) return false

        val currentCategory = categoryService.getCategoryById(category.id) ?: return false
        val allPhotosAlreadyInCategory = photosToMove.all { currentCategory.containsPhoto(it) }
        if (allPhotosAlreadyInCategory) {
            return false
        }

        val children = photoContainer.children
        val insertIdx = currentCategory.photos.size

        val sourceCategoryIds = mutableSetOf<String>()
        
        var currentInsertIdx = insertIdx
        photosToMove.forEach { photo ->
            val latestCategory = categoryService.getCategoryById(category.id) ?: return@forEach
            if (latestCategory.containsPhoto(photo)) {
                return@forEach
            }

            photoService.removePhoto(photo)

            val previousCategory = categoryService.findCategoryContainingPhoto(photo)
            if (previousCategory != null) {
                categoryService.removePhotoFromCategory(photo, previousCategory)
                sourceCategoryIds.add(previousCategory.id)
            }

            categoryService.addPhotoToCategory(photo, latestCategory, currentInsertIdx)
            currentInsertIdx++
        }

        val imageViewsToMove = sortedIndices.mapNotNull { imageViews.getOrNull(it) }
        imageViewsToMove.forEach { iv ->
            imageViews.remove(iv)
            iv.parent?.let { parent ->
                if (parent is VBox) parent.children.remove(iv)
            }
            iv.fitWidth = StyleConstants.PHOTO_CATEGORY_WIDTH
            iv.opacity = StyleConstants.NORMAL_OPACITY
            iv.style = ""
        }

        children.addAll(imageViewsToMove)
        onPhotosDropped()
        
        sourceCategoryIds.forEach { categoryId ->
            onSourceCategoryUpdated(categoryId)
        }

        return true
    }

    fun getDraggedImageView(): ImageView? = draggedImageView
}
