package domain.service

import domain.model.Photo
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Service for managing the photo collection.
 * Thread-safe implementation using CopyOnWriteArrayList.
 */
class PhotoService {
    private val photos = CopyOnWriteArrayList<Photo>()

    fun setPhotos(photoList: List<Photo>) {
        photos.clear()
        photos.addAll(photoList)
    }

    fun getPhotos(): List<Photo> = photos.toList()

    fun getPhotoByIndex(index: Int): Photo? = photos.getOrNull(index)

    fun getPhotoById(photoId: String): Photo? = photos.find { it.id == photoId }

    fun getIndexOfPhoto(photoId: String): Int = photos.indexOfFirst { it.id == photoId }

    fun removePhoto(photo: Photo) {
        photos.remove(photo)
    }

    fun removePhotosByIds(photoIds: Collection<String>) {
        photos.removeIf { photoIds.contains(it.id) }
    }

    fun getPhotoCount(): Int = photos.size

    fun clearPhotos() {
        photos.clear()
    }

    fun restorePhotos(photosToRestore: List<Photo>) {
        if (photosToRestore.isEmpty()) return
        photos.addAll(photosToRestore)
        val sorted = photos.sortedBy { it.originalIndex }
        photos.clear()
        photos.addAll(sorted)
    }

    fun getPhotosByIndices(indices: List<Int>): List<Photo> {
        return indices.mapNotNull { photos.getOrNull(it) }
    }

    /**
     * Updates a photo's rotation in the collection.
     * 
     * @param photoId Photo ID to update
     * @param rotationDegrees New rotation value (0, 90, 180, 270)
     * @return Updated photo or null if not found
     */
    fun updatePhotoRotation(photoId: String, rotationDegrees: Int): Photo? {
        val index = photos.indexOfFirst { it.id == photoId }
        if (index == -1) return null
        
        val photo = photos[index]
        val updatedPhoto = photo.copy(rotationDegrees = rotationDegrees)
        photos[index] = updatedPhoto
        return updatedPhoto
    }
}
