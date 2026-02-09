package ui.handler

import domain.service.PhotoService
import domain.service.SelectionService
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import util.ImageUtils
import util.StyleConstants

class SelectionHandler(
    private val selectionService: SelectionService,
    private val photoService: PhotoService,
    private val imageViews: MutableList<ImageView>,
    private val columns: () -> Int
) {
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
            imageViews.size,
            columns()
        )

        selectionService.clearSelection()

        val photoIds = result.selectedIndices.mapNotNull { index ->
            imageViews.getOrNull(index)?.let { ImageUtils.getPhotoId(it) }
        }
        selectionService.addSelections(photoIds)
    }

    fun updateVisualSelection() {
        imageViews.forEach { iv ->
            val photoId = ImageUtils.getPhotoId(iv) ?: return@forEach
            val container = iv.parent as? StackPane
            if (selectionService.isSelected(photoId)) {
                container?.style = StyleConstants.SELECTED_CONTAINER_STYLE
            } else {
                container?.style = StyleConstants.UNSELECTED_CONTAINER_STYLE
            }
        }
    }

    fun clearSelection() {
        selectionService.clearSelection()
        updateVisualSelection()
    }

    fun getSelectedPhotoIds(): Set<String> = selectionService.getSelectedPhotoIds()

    fun getSelectedImageViews(): List<ImageView> {
        val selectedIds = selectionService.getSelectedPhotoIds()
        return imageViews.filter { iv ->
            val photoId = ImageUtils.getPhotoId(iv)
            photoId != null && selectedIds.contains(photoId)
        }
    }
}
