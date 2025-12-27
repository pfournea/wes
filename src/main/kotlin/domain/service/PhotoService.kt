package domain.service

import domain.model.Photo

/**
 * Service for managing the photo collection.
 * Maintains the list of photos in their original order.
 */
class PhotoService {
    private val photos = mutableListOf<Photo>()

    /**
     * Sets the photo collection.
     */
    fun setPhotos(photoList: List<Photo>) {
        photos.clear()
        photos.addAll(photoList)
    }

    /**
     * Gets all photos.
     */
    fun getPhotos(): List<Photo> {
        return photos.toList()
    }

    /**
     * Gets a photo by index.
     */
    fun getPhotoByIndex(index: Int): Photo? {
        return photos.getOrNull(index)
    }

    /**
     * Gets a photo by ID.
     */
    fun getPhotoById(photoId: String): Photo? {
        return photos.find { it.id == photoId }
    }

    /**
     * Gets the index of a photo by ID.
     */
    fun getIndexOfPhoto(photoId: String): Int {
        return photos.indexOfFirst { it.id == photoId }
    }

    /**
     * Removes a photo from the main collection.
     */
    fun removePhoto(photo: Photo) {
        photos.remove(photo)
    }

    /**
     * Removes photos by IDs.
     */
    fun removePhotosByIds(photoIds: Collection<String>) {
        photos.removeIf { photoIds.contains(it.id) }
    }

    /**
     * Gets the total count of photos.
     */
    fun getPhotoCount(): Int {
        return photos.size
    }

    /**
     * Clears all photos.
     */
    fun clearPhotos() {
        photos.clear()
    }

    /**
     * Restores photos to the collection, maintaining original index order.
     * Photos are inserted at positions that preserve their originalIndex sequence.
     */
    fun restorePhotos(photosToRestore: List<Photo>) {
        if (photosToRestore.isEmpty()) return
        
        photos.addAll(photosToRestore)
        photos.sortBy { it.originalIndex }
    }

    /**
     * Gets photos by indices.
     */
    fun getPhotosByIndices(indices: List<Int>): List<Photo> {
        return indices.mapNotNull { photos.getOrNull(it) }
    }
}
