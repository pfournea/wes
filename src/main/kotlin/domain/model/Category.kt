package domain.model

/**
 * Represents a category that can contain photos.
 * This is a pure, immutable domain model with no UI dependencies.
 * All mutations return new instances - use CategoryService for state management.
 */
data class Category(
    val id: String,
    val number: Int,
    val name: String,
    val photos: List<Photo> = emptyList()
) {
    /**
     * Checks if this category contains the given photo.
     */
    fun containsPhoto(photo: Photo): Boolean {
        return photos.contains(photo)
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
