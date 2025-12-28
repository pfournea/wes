package domain.service

import domain.model.Photo
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

class FileService {
    private val supportedExtensions = setOf("jpg", "jpeg", "png", "gif", "bmp")
    private val tempDirectories = mutableListOf<Path>()

    fun extractPhotosFromZip(zipFile: File): List<Photo> {
        cleanupTempDirectories()
        
        val tempDir = Files.createTempDirectory("photo_categorizer")
        tempDirectories.add(tempDir)
        
        val imageFiles = mutableListOf<Path>()

        ZipFile(zipFile).use { zip ->
            for (entry in zip.entries()) {
                if (!entry.isDirectory && isImageFile(entry.name)) {
                    val extractedFile = tempDir.resolve(entry.name)
                    Files.createDirectories(extractedFile.parent)
                    zip.getInputStream(entry).use { input ->
                        Files.copy(input, extractedFile)
                    }
                    imageFiles.add(extractedFile)
                }
            }
        }

        return imageFiles.mapIndexed { index, path ->
            Photo.fromPath(path, index)
        }
    }

    fun isImageFile(filename: String): Boolean {
        val extension = filename.substringAfterLast('.', "").lowercase()
        return extension in supportedExtensions
    }

    fun getSupportedExtensions(): Set<String> {
        return supportedExtensions.toSet()
    }

    fun cleanupTempDirectories() {
        val iterator = tempDirectories.iterator()
        while (iterator.hasNext()) {
            val dir = iterator.next()
            deleteDirectoryRecursively(dir)
            iterator.remove()
        }
    }

    fun getTempDirectoryCount(): Int = tempDirectories.size

    private fun deleteDirectoryRecursively(path: Path) {
        if (!path.exists()) return
        
        if (path.isDirectory()) {
            path.listDirectoryEntries().forEach { child ->
                deleteDirectoryRecursively(child)
            }
        }
        path.deleteIfExists()
    }
}
