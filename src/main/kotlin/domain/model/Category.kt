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
