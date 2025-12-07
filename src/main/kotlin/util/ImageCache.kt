package util

import javafx.scene.image.Image
import java.nio.file.Path

/**
 * Singleton cache for Image objects to avoid redundant loading.
 * Images are cached by path and requested width to support different sizes.
 */
object ImageCache {
    private val cache = mutableMapOf<String, Image>()

    /**
     * Gets a cached image or loads it with optimal settings.
     *
     * @param path The file path to the image
     * @param width The requested width for pre-scaling
     * @return Cached or newly loaded Image
     */
    fun getImage(path: Path, width: Double): Image {
        val key = "${path}_$width"
        return cache.getOrPut(key) {
            Image(
                path.toUri().toString(),
                width,               // requestedWidth - pre-scale at load time
                0.0,                 // requestedHeight - preserve aspect ratio
                true,                // preserveRatio
                false,               // smooth - no high-quality filtering needed
                true                 // backgroundLoading - load asynchronously
            )
        }
    }

    /**
     * Clears the entire cache (useful when loading new photo sets).
     */
    fun clear() {
        cache.clear()
    }
}
