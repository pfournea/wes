package domain.service

import domain.model.Category
import domain.model.Photo
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Service for managing categories and photo assignments.
 * Thread-safe and maintains immutable Category instances internally.
 */
class CategoryService {
    private val categories = CopyOnWriteArrayList<Category>()
    private var nextCategoryNumber = 1

    fun createCategory(): Category {
        val category = Category.create(nextCategoryNumber)
        nextCategoryNumber++
        categories.add(category)
        return category
    }

    fun getCategories(): List<Category> = categories.toList()

    fun addPhotoToCategory(photo: Photo, category: Category, position: Int? = null): Category {
        val index = categories.indexOfFirst { it.id == category.id }
        if (index == -1) return category

        val currentCategory = categories[index]
        val newPhotos = currentCategory.photos.toMutableList()
        
        if (position != null && position in 0..newPhotos.size) {
            newPhotos.add(position, photo)
        } else {
            newPhotos.add(photo)
        }
        
        val updatedCategory = currentCategory.copy(photos = newPhotos)
        categories[index] = updatedCategory
        return updatedCategory
    }

    fun removePhotoFromCategory(photo: Photo, category: Category): Category {
        val index = categories.indexOfFirst { it.id == category.id }
        if (index == -1) return category

        val currentCategory = categories[index]
        val updatedCategory = currentCategory.copy(
            photos = currentCategory.photos.filter { it != photo }
        )
        categories[index] = updatedCategory
        return updatedCategory
    }

    fun reorderPhotoInCategory(photoId: String, category: Category, newPosition: Int): Boolean {
        val index = categories.indexOfFirst { it.id == category.id }
        if (index == -1) return false

        val currentCategory = categories[index]
        val photos = currentCategory.photos.toMutableList()
        val currentIndex = photos.indexOfFirst { it.id == photoId }
        
        if (currentIndex == -1) return false
        if (newPosition < 0 || newPosition > photos.size) return false
        if (currentIndex == newPosition) return true

        val photo = photos.removeAt(currentIndex)
        val adjustedPosition = if (newPosition > currentIndex) newPosition - 1 else newPosition
        photos.add(adjustedPosition.coerceIn(0, photos.size), photo)
        
        categories[index] = currentCategory.copy(photos = photos)
        return true
    }

    fun findCategoryContainingPhoto(photo: Photo): Category? {
        return categories.find { it.containsPhoto(photo) }
    }

    fun findPhotoById(photoId: String): Photo? {
        categories.forEach { category ->
            val photo = category.photos.find { it.id == photoId }
            if (photo != null) return photo
        }
        return null
    }

    fun getCategoryById(categoryId: String): Category? {
        return categories.find { it.id == categoryId }
    }

    fun clearCategories() {
        categories.clear()
        nextCategoryNumber = 1
    }

    fun getCategoryCount(): Int = categories.size

    fun removeCategory(categoryId: String): Boolean {
        val index = categories.indexOfFirst { it.id == categoryId }
        if (index == -1) return false
        categories.removeAt(index)
        return true
    }

    fun removeCategoryAndReturnPhotos(categoryId: String): List<Photo> {
        val index = categories.indexOfFirst { it.id == categoryId }
        if (index == -1) return emptyList()
        val category = categories.removeAt(index)
        return category.photos.toList()
    }
    
    fun updateCategory(category: Category): Category {
        val index = categories.indexOfFirst { it.id == category.id }
        if (index != -1) {
            categories[index] = category
        }
        return category
    }

    /**
     * Updates a photo's rotation within a category.
     * 
     * @param photoId Photo ID to update
     * @param categoryId Category ID containing the photo
     * @param rotationDegrees New rotation value (0, 90, 180, 270)
     * @return Updated category or null if not found
     */
    fun updatePhotoRotationInCategory(photoId: String, categoryId: String, rotationDegrees: Int): Category? {
        val index = categories.indexOfFirst { it.id == categoryId }
        if (index == -1) return null
        
        val currentCategory = categories[index]
        val photoIndex = currentCategory.photos.indexOfFirst { it.id == photoId }
        if (photoIndex == -1) return null
        
        val photos = currentCategory.photos.toMutableList()
        val photo = photos[photoIndex]
        photos[photoIndex] = photo.copy(rotationDegrees = rotationDegrees)
        
        val updatedCategory = currentCategory.copy(photos = photos)
        categories[index] = updatedCategory
        return updatedCategory
    }
}
