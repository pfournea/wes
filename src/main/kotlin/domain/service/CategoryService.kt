package domain.service

import domain.model.Category
import domain.model.Photo

/**
 * Service for managing categories and photo assignments.
 * Contains pure business logic with no UI dependencies.
 */
class CategoryService {
    private val categories = mutableListOf<Category>()

    /**
     * Creates a new category with auto-incremented number.
     */
    fun createCategory(): Category {
        val nextNumber = categories.size + 1
        val category = Category.create(nextNumber)
        categories.add(category)
        return category
    }

    /**
     * Gets all categories.
     */
    fun getCategories(): List<Category> {
        return categories.toList()
    }

    /**
     * Adds a photo to a category at a specific position.
     */
    fun addPhotoToCategory(photo: Photo, category: Category, position: Int? = null) {
        category.addPhoto(photo, position)
    }

    /**
     * Removes a photo from a category.
     */
    fun removePhotoFromCategory(photo: Photo, category: Category) {
        category.removePhoto(photo)
    }

    /**
     * Reorders a photo within a category to a new position.
     */
    fun reorderPhotoInCategory(photo: Photo, category: Category, newPosition: Int): Boolean {
        return category.reorderPhoto(photo, newPosition)
    }

    /**
     * Finds which category contains a photo, if any.
     */
    fun findCategoryContainingPhoto(photo: Photo): Category? {
        return categories.find { it.containsPhoto(photo) }
    }

    /**
     * Finds a photo by ID across all categories.
     */
    fun findPhotoById(photoId: String): Photo? {
        categories.forEach { category ->
            val photo = category.photos.find { it.id == photoId }
            if (photo != null) return photo
        }
        return null
    }

    /**
     * Gets a category by its ID.
     */
    fun getCategoryById(categoryId: String): Category? {
        return categories.find { it.id == categoryId }
    }

    /**
     * Clears all categories.
     */
    fun clearCategories() {
        categories.clear()
    }

    /**
     * Gets the count of categories.
     */
    fun getCategoryCount(): Int {
        return categories.size
    }
}
