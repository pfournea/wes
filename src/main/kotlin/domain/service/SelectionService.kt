package domain.service

import domain.model.Selection

/**
 * Service for managing photo selection logic.
 * Thread-safe and maintains immutable Selection state internally.
 */
class SelectionService {
    @Volatile
    private var selection = Selection()

    fun handleSingleClick(photoIndex: Int, photoId: String): SelectionResult {
        selection = Selection(
            anchorIndex = photoIndex,
            selectedPhotoIds = setOf(photoId)
        )
        return SelectionResult(setOf(photoId), photoIndex)
    }

    fun handleCtrlClick(photoIndex: Int, photoId: String): SelectionResult {
        val currentIds = selection.selectedPhotoIds
        val newIds = if (currentIds.contains(photoId)) {
            currentIds - photoId
        } else {
            currentIds + photoId
        }
        selection = selection.copy(
            anchorIndex = photoIndex,
            selectedPhotoIds = newIds
        )
        return SelectionResult(newIds, photoIndex)
    }

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

                if (row == startRow && col < startCol) continue
                if (row == endRow && col > endCol) continue

                selectedIndices.add(i)
            }
        }

        return RangeSelectionResult(selectedIndices)
    }

    fun clearSelection() {
        selection = selection.copy(selectedPhotoIds = emptySet())
    }

    fun getSelection(): Selection = selection

    fun isSelected(photoId: String): Boolean = selection.isSelected(photoId)

    fun getSelectedPhotoIds(): Set<String> = selection.selectedPhotoIds

    fun addSelections(photoIds: Collection<String>) {
        selection = selection.copy(
            selectedPhotoIds = selection.selectedPhotoIds + photoIds
        )
    }

    fun addSelection(photoId: String) {
        selection = selection.copy(
            selectedPhotoIds = selection.selectedPhotoIds + photoId
        )
    }
}

data class SelectionResult(
    val selectedPhotoIds: Set<String>,
    val anchorIndex: Int?
)

data class RangeSelectionResult(
    val selectedIndices: List<Int>
)
