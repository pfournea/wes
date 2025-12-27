package ui.handler

import domain.model.Category
import domain.service.CategoryService
import javafx.geometry.Bounds
import javafx.scene.effect.DropShadow
import javafx.scene.image.ImageView
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.TilePane
import javafx.scene.paint.Color
import util.ImageUtils
import util.StyleConstants

class ReorderDragDropHandler(
    private val categoryService: CategoryService,
    private val imageViews: MutableList<ImageView>,
    private val onReorderComplete: () -> Unit
) {
    private var nodeWithDropIndicator: javafx.scene.Node? = null
    private var originalEffectOfIndicatorNode: javafx.scene.effect.Effect? = null

    fun handleDragOver(event: DragEvent, imageContainer: TilePane, category: Category) {
        if (event.dragboard.hasString() && isValidDragSource(event)) {
            event.acceptTransferModes(TransferMode.MOVE)
            
            val targetIndex = calculateDropIndex(event, imageContainer)
            updateDropIndicator(targetIndex, imageContainer)
        }
        event.consume()
    }

    fun handleDragDropped(event: DragEvent, category: Category, imageContainer: TilePane): Boolean {
        val dragboard = event.dragboard
        if (!dragboard.hasString()) return false
        
        val indices = dragboard.string.split(",").mapNotNull { it.toIntOrNull() }
        if (indices.isEmpty()) return false
        
        val currentCategory = categoryService.getCategoryById(category.id) ?: return false
        
        val photoIdsToReorder = indices.mapNotNull { index ->
            imageViews.getOrNull(index)?.let { iv ->
                val photoId = ImageUtils.getPhotoId(iv)
                photoId?.takeIf { id -> currentCategory.photos.any { p -> p.id == id } }
            }
        }
        
        if (photoIdsToReorder.isEmpty()) return false
        
        val targetIndex = calculateDropIndex(event, imageContainer)
        
        var success = false
        photoIdsToReorder.forEach { photoId ->
            val latestCategory = categoryService.getCategoryById(category.id) ?: return@forEach
            if (categoryService.reorderPhotoInCategory(photoId, latestCategory, targetIndex)) {
                success = true
            }
        }
        
        if (success) {
            onReorderComplete()
        }
        
        clearDropIndicator()
        return success
    }

    fun handleDragExited(event: DragEvent, imageContainer: TilePane) {
        clearDropIndicator()
        event.consume()
    }

    private fun isValidDragSource(event: DragEvent): Boolean {
        val gestureSource = event.gestureSource
        return gestureSource is ImageView && imageViews.contains(gestureSource)
    }

    private fun calculateDropIndex(event: DragEvent, imageContainer: TilePane): Int {
        val children = imageContainer.children
        if (children.isEmpty()) return 0
        
        val mouseX = event.x
        val mouseY = event.y
        
        for ((index, node) in children.withIndex()) {
            val bounds: Bounds = node.boundsInParent
            val centerX = bounds.minX + bounds.width / 2
            
            if (mouseY < bounds.maxY) {
                if (mouseY < bounds.minY) {
                    return index
                }
                if (mouseX < centerX) {
                    return index
                }
                if (mouseX >= centerX && mouseX < bounds.maxX) {
                    return index + 1
                }
            }
        }
        
        return children.size
    }

    private fun updateDropIndicator(targetIndex: Int, imageContainer: TilePane) {
        clearDropIndicator()
        
        val children = imageContainer.children
        if (children.isEmpty() || targetIndex >= children.size) return
        
        val targetNode = children[targetIndex]
        originalEffectOfIndicatorNode = targetNode.effect
        nodeWithDropIndicator = targetNode
        
        targetNode.effect = DropShadow().apply {
            color = Color.web(StyleConstants.SELECTED_BORDER_COLOR)
            radius = 20.0
            spread = 0.8
        }
    }

    private fun clearDropIndicator() {
        nodeWithDropIndicator?.let { node ->
            node.effect = originalEffectOfIndicatorNode
        }
        nodeWithDropIndicator = null
        originalEffectOfIndicatorNode = null
    }
}
