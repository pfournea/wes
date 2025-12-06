package domain.model

/**
 * Represents the selection state in the photo grid.
 * Manages anchor point and selected photo IDs.
 */
data class Selection(
    var anchorIndex: Int? = null,
    val selectedPhotoIds: MutableSet<String> = mutableSetOf()
) {
    fun setAnchor(index: Int) {
        anchorIndex = index
    }

    fun clearAnchor() {
        anchorIndex = null
    }

    fun addSelection(photoId: String) {
        selectedPhotoIds.add(photoId)
    }

    fun removeSelection(photoId: String) {
        selectedPhotoIds.remove(photoId)
    }

    fun toggleSelection(photoId: String) {
        if (selectedPhotoIds.contains(photoId)) {
            selectedPhotoIds.remove(photoId)
        } else {
            selectedPhotoIds.add(photoId)
        }
    }

    fun clearSelection() {
        selectedPhotoIds.clear()
    }

    fun isSelected(photoId: String): Boolean {
        return selectedPhotoIds.contains(photoId)
    }

    fun hasSelection(): Boolean {
        return selectedPhotoIds.isNotEmpty()
    }

    fun getSelectionCount(): Int {
        return selectedPhotoIds.size
    }
}
