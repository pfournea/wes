package domain.service

import domain.model.Category
import domain.model.Photo

data class CopyResult(
    val success: Boolean,
    val photosCopied: Int,
    val categoriesUpdated: Int,
    val errors: List<String> = emptyList()
)

class CopyService(
    private val categoryService: CategoryService
) {
    fun copyPhotosToCategories(
        sourceCategory: Category,
        targetCategories: List<Category>
    ): CopyResult {
        if (sourceCategory.photos.isEmpty()) {
            return CopyResult(
                success = false,
                photosCopied = 0,
                categoriesUpdated = 0,
                errors = listOf("Source category has no photos")
            )
        }

        if (targetCategories.isEmpty()) {
            return CopyResult(
                success = false,
                photosCopied = 0,
                categoriesUpdated = 0,
                errors = listOf("No target categories selected")
            )
        }

        val errors = mutableListOf<String>()
        var categoriesUpdated = 0

        for (targetCategory in targetCategories) {
            if (targetCategory.id == sourceCategory.id) continue

            sourceCategory.photos.forEachIndexed { index, photo ->
                val result = categoryService.copyPhotoToCategory(photo, targetCategory, index)
                if (result == null) {
                    errors.add("Failed to copy photo to ${targetCategory.name}")
                }
            }
            categoriesUpdated++
        }

        val photosCopied = sourceCategory.photos.size * targetCategories.size

        return CopyResult(
            success = errors.isEmpty(),
            photosCopied = photosCopied,
            categoriesUpdated = categoriesUpdated,
            errors = errors
        )
    }

    fun parseCategoryRange(input: String, existingCategories: List<Category>): List<Int> {
        val result = mutableSetOf<Int>()
        val parts = input.split(",").map { it.trim() }

        for (part in parts) {
            if (part.contains("-")) {
                val rangeParts = part.split("-").map { it.trim().toIntOrNull() }
                if (rangeParts.size == 2 && rangeParts[0] != null && rangeParts[1] != null) {
                    val start = minOf(rangeParts[0]!!, rangeParts[1]!!)
                    val end = maxOf(rangeParts[0]!!, rangeParts[1]!!)
                    for (i in start..end) {
                        result.add(i)
                    }
                }
            } else {
                part.toIntOrNull()?.let { result.add(it) }
            }
        }

        return result.sorted()
    }
}
