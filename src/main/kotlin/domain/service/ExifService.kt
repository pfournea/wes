package domain.service

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifIFD0Directory
import java.nio.file.Path

/**
 * Service for reading EXIF metadata from image files.
 * Primarily used to extract orientation information for proper image display.
 */
class ExifService {
    
    /**
     * Reads the EXIF orientation tag from an image and converts it to rotation degrees.
     * 
     * EXIF Orientation values:
     * - 1 = Normal (0°)
     * - 3 = Upside down (180°)
     * - 6 = Rotated 90° CW (90°)
     * - 8 = Rotated 90° CCW (270°)
     * 
     * Other orientation values (2, 4, 5, 7) involve mirroring, which we don't support.
     * These are treated as 0° rotation.
     * 
     * @param path Path to the image file
     * @return Rotation in degrees (0, 90, 180, 270). Returns 0 if no EXIF data or on error.
     */
    fun readOrientation(path: Path): Int {
        return try {
            val metadata = ImageMetadataReader.readMetadata(path.toFile())
            val exifDirectory = metadata.getFirstDirectoryOfType(ExifIFD0Directory::class.java)
            
            if (exifDirectory != null && exifDirectory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                val orientation = exifDirectory.getInt(ExifIFD0Directory.TAG_ORIENTATION)
                mapOrientationToDegrees(orientation)
            } else {
                0 // No EXIF orientation tag found
            }
        } catch (e: Exception) {
            // If EXIF reading fails for any reason (corrupted metadata, unsupported format, etc.)
            // default to no rotation
            0
        }
    }
    
    /**
     * Maps EXIF orientation tag values to rotation degrees.
     * 
     * @param orientation EXIF orientation value (1-8)
     * @return Rotation in degrees (0, 90, 180, 270)
     */
    private fun mapOrientationToDegrees(orientation: Int): Int {
        return when (orientation) {
            1 -> 0      // Normal
            3 -> 180    // Upside down
            6 -> 90     // Rotated 90° CW
            8 -> 270    // Rotated 90° CCW
            else -> 0   // Unsupported orientation (mirrored images), default to no rotation
        }
    }
}
