package domain.service

import domain.model.Photo
import domain.model.Selection

/**
 * Service for managing photo selection logic.
 * Contains pure business logic with no UI dependencies - fully testable.
 */
class SelectionService {
    private val selection = Selection()

    /**
     * Handles single click selection - sets anchor and clears previous selection.
     */
    fun handleSingleClick(photoIndex: Int, photoId: String): SelectionResult {
        selection.clearSelection()
        selection.setAnchor(photoIndex)
        selection.addSelection(photoId)
        return SelectionResult(setOf(photoId), photoIndex)
    }

    /**
     * Handles Ctrl+Click selection - toggles individual photo.
     */
    fun handleCtrlClick(photoIndex: Int, photoId: String): SelectionResult {
        selection.toggleSelection(photoId)
        selection.setAnchor(photoIndex)
        return SelectionResult(selection.selectedPhotoIds.toSet(), photoIndex)
    }

    /**
     * Handles Shift+Click selection - row-wise range from anchor to clicked.
     * Returns indices of photos that should be selected.
     */
    fun handleShiftClick(
        clickedIndex: Int,
        totalPhotos: Int,
        columns: Int
    ): RangeSelectionResult {
        val anchor = selection.anchorIndex ?: return RangeSelectionResult(emptyList())

        val startIdx: Int
        val endIdx: Int

        if (anchor <= clickedIndex) {
            startIdx = anchor
            endIdx = clickedIndex
        } else {
            startIdx = clickedIndex
            endIdx = anchor
        }

        val startRow = startIdx / columns
        val startCol = startIdx % columns
        val endRow = endIdx / columns
        val endCol = endIdx % columns

        val selectedIndices = mutableListOf<Int>()

        for (row in startRow..endRow) {
            for (col in 0 until columns) {
                val i = row * columns + col
                if (i >= totalPhotos) break

                // Skip photos before start column in the start row
                if (row == startRow && col < startCol) continue
                // Skip photos after end column in the end row
                if (row == endRow && col > endCol) continue

                selectedIndices.add(i)
            }
        }

        return RangeSelectionResult(selectedIndices)
    }

    /**
     * Clears all selection.
     */
    fun clearSelection() {
        selection.clearSelection()
    }

    /**
     * Returns current selection state.
     */
    fun getSelection(): Selection {
        return selection
    }

    /**
     * Checks if a photo is currently selected.
     */
    fun isSelected(photoId: String): Boolean {
        return selection.isSelected(photoId)
    }

    /**
     * Gets all selected photo IDs.
     */
    fun getSelectedPhotoIds(): Set<String> {
        return selection.selectedPhotoIds.toSet()
    }
}

/**
 * Result of a selection operation.
 */
data class SelectionResult(
    val selectedPhotoIds: Set<String>,
    val anchorIndex: Int?
)

/**
 * Result of a range selection operation.
 */
data class RangeSelectionResult(
    val selectedIndices: List<Int>
)
