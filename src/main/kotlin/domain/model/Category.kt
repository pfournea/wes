package domain.model

/**
 * Represents a category that can contain photos.
 * This is a pure domain model with no UI dependencies.
 */
data class Category(
    val id: String,
    val number: Int,
    val name: String,
    val photos: MutableList<Photo> = mutableListOf()
) {
    fun addPhoto(photo: Photo, position: Int? = null) {
        if (position != null && position in 0..photos.size) {
            photos.add(position, photo)
        } else {
            photos.add(photo)
        }
    }

    fun removePhoto(photo: Photo) {
        photos.remove(photo)
    }

    fun containsPhoto(photo: Photo): Boolean {
        return photos.contains(photo)
    }

    /**
     * Reorders a photo within the category by moving it to a new position.
     * @param photo The photo to move
     * @param newPosition The target position (0-based index)
     * @return true if the photo was successfully reordered, false if not found or invalid position
     */
    fun reorderPhoto(photo: Photo, newPosition: Int): Boolean {
        val currentIndex = photos.indexOf(photo)
        if (currentIndex == -1) return false
        if (newPosition < 0 || newPosition > photos.size) return false
        if (currentIndex == newPosition) return true // Already at target position

        photos.removeAt(currentIndex)
        // Adjust target position if we removed from before it
        val adjustedPosition = if (newPosition > currentIndex) newPosition - 1 else newPosition
        photos.add(adjustedPosition.coerceIn(0, photos.size), photo)
        return true
    }

    /**
     * Gets the position of a photo in this category.
     * @return The index of the photo, or -1 if not found
     */
    fun getPhotoPosition(photo: Photo): Int {
        return photos.indexOf(photo)
    }

    companion object {
        fun create(number: Int): Category {
            return Category(
                id = "category_$number",
                number = number,
                name = "Category $number"
            )
        }
    }
}
