package domain.service

import domain.model.Photo

/**
 * Service for managing photo rotation state.
 * Handles rotation transformations for photos.
 */
class RotationService {
    
    /**
     * Rotates a photo 90 degrees clockwise.
     * 
     * @param photo Photo to rotate
     * @return New photo instance with updated rotation
     */
    fun rotateClockwise(photo: Photo): Photo {
        val newRotation = (photo.rotationDegrees + 90) % 360
        return photo.copy(rotationDegrees = newRotation)
    }
    
    /**
     * Rotates a photo 90 degrees counter-clockwise.
     * 
     * @param photo Photo to rotate
     * @return New photo instance with updated rotation
     */
    fun rotateCounterClockwise(photo: Photo): Photo {
        val newRotation = (photo.rotationDegrees - 90 + 360) % 360
        return photo.copy(rotationDegrees = newRotation)
    }
    
    /**
     * Resets a photo's rotation to 0 degrees.
     * 
     * @param photo Photo to reset
     * @return New photo instance with rotation reset
     */
    fun resetRotation(photo: Photo): Photo {
        return photo.copy(rotationDegrees = 0)
    }
}
