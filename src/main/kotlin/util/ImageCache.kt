package util

import javafx.scene.image.Image
import java.nio.file.Path

/**
 * Singleton cache for Image objects to avoid redundant loading.
 * Images are cached by path and requested width to support different sizes.
 * Uses LRU eviction to prevent unbounded memory growth.
 */
object ImageCache {
    /**
     * Maximum number of images to cache. When exceeded, oldest entries are evicted.
     * Default of 500 images at ~200KB each ≈ 100MB max memory usage.
     */
    private const val MAX_CACHE_SIZE = 500

    // LinkedHashMap with accessOrder=true provides LRU ordering
    private val cache = object : LinkedHashMap<String, Image>(MAX_CACHE_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Image>?): Boolean {
            return size > MAX_CACHE_SIZE
        }
    }

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

    fun size(): Int = cache.size
}
