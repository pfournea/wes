package domain.model

/**
 * Represents the selection state in the photo grid.
 * This is a pure, immutable domain model.
 * All mutations return new instances - use SelectionService for state management.
 */
data class Selection(
    val anchorIndex: Int? = null,
    val selectedPhotoIds: Set<String> = emptySet()
) {
    fun isSelected(photoId: String): Boolean = selectedPhotoIds.contains(photoId)
    fun hasSelection(): Boolean = selectedPhotoIds.isNotEmpty()
    fun getSelectionCount(): Int = selectedPhotoIds.size
}
