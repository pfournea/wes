package domain.model

import java.nio.file.Path

/**
 * Represents a photo with its metadata.
 * This is a pure domain model with no UI dependencies.
 */
data class Photo(
    val id: String,
    val path: Path,
    val fileName: String,
    val originalIndex: Int
) {
    companion object {
        fun fromPath(path: Path, index: Int): Photo {
            return Photo(
                id = generateId(path, index),
                path = path,
                fileName = path.fileName.toString(),
                originalIndex = index
            )
        }

        private fun generateId(path: Path, index: Int): String {
            return "${index}_${path.fileName}"
        }
    }
}
