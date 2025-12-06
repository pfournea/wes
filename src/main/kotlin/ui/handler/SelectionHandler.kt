package ui.handler

import domain.model.Photo
import domain.service.PhotoService
import domain.service.SelectionService
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import util.ImageUtils
import util.StyleConstants

/**
 * Handles selection events and delegates to SelectionService.
 * Bridges UI events to business logic.
 */
class SelectionHandler(
    private val selectionService: SelectionService,
    private val photoService: PhotoService,
    private val imageViews: MutableList<ImageView>,
    private val columns: () -> Int
) {
    /**
     * Handles mouse click on an image.
     */
    fun handleImageClick(event: MouseEvent, imageView: ImageView) {
        val photoId = ImageUtils.getPhotoId(imageView) ?: return
        val index = imageViews.indexOf(imageView)
        if (index < 0) return

        val ctrl = event.isControlDown
        val shift = event.isShiftDown

        when {
            ctrl -> handleCtrlClick(index, photoId)
            shift -> handleShiftClick(index)
            else -> handleSingleClick(index, photoId)
        }

        updateVisualSelection()
        event.consume()
    }

    private fun handleSingleClick(index: Int, photoId: String) {
        selectionService.handleSingleClick(index, photoId)
    }

    private fun handleCtrlClick(index: Int, photoId: String) {
        selectionService.handleCtrlClick(index, photoId)
    }

    private fun handleShiftClick(clickedIndex: Int) {
        val result = selectionService.handleShiftClick(
            clickedIndex,
            photoService.getPhotoCount(),
            columns()
        )

        // Clear previous selection visually
        selectionService.clearSelection()

        // Add new range selection
        for (index in result.selectedIndices) {
            val iv = imageViews.getOrNull(index) ?: continue
            val photoId = ImageUtils.getPhotoId(iv) ?: continue
            selectionService.getSelection().addSelection(photoId)
        }
    }

    /**
     * Updates visual selection borders on all images.
     */
    fun updateVisualSelection() {
        imageViews.forEach { iv ->
            val photoId = ImageUtils.getPhotoId(iv) ?: return@forEach
            if (selectionService.isSelected(photoId)) {
                iv.style = StyleConstants.SELECTED_STYLE
            } else {
                iv.style = ""
            }
        }
    }

    /**
     * Clears all selection.
     */
    fun clearSelection() {
        selectionService.clearSelection()
        updateVisualSelection()
    }

    /**
     * Gets currently selected photo IDs.
     */
    fun getSelectedPhotoIds(): Set<String> {
        return selectionService.getSelectedPhotoIds()
    }

    /**
     * Gets currently selected ImageViews.
     */
    fun getSelectedImageViews(): List<ImageView> {
        val selectedIds = selectionService.getSelectedPhotoIds()
        return imageViews.filter { iv ->
            val photoId = ImageUtils.getPhotoId(iv)
            photoId != null && selectedIds.contains(photoId)
        }
    }
}
