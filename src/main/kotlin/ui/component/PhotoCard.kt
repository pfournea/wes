package ui.component

import domain.model.Photo
import javafx.animation.ScaleTransition
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import javafx.util.Duration
import util.Icons
import util.StyleConstants

/**
 * Professional photo card with clean control overlays.
 */
class PhotoCard(
    val imageView: ImageView,
    val photo: Photo,
    private val onDeleteRequested: () -> Unit = {},
    private val onRotateLeft: () -> Unit = {},
    private val onRotateRight: () -> Unit = {},
    isInCategory: Boolean = false
) {
    private val deleteButton = Button(Icons.REMOVE)
    private val rotateLeftButton = Button(Icons.ROTATE_LEFT)
    private val rotateRightButton = Button(Icons.ROTATE_RIGHT)
    private var isInCategoryView = isInCategory
    val container = StackPane()

    init {
        setupContainer()
    }

    private fun setupContainer() {
        container.children.add(imageView)
        
        container.maxWidthProperty().bind(imageView.fitWidthProperty())
        
        imageView.imageProperty().addListener { _, _, newImage ->
            if (newImage != null && imageView.isPreserveRatio) {
                val aspectRatio = newImage.height / newImage.width
                container.maxHeight = imageView.fitWidth * aspectRatio
            }
        }
        
        imageView.image?.let { image ->
            if (imageView.isPreserveRatio) {
                val aspectRatio = image.height / image.width
                container.maxHeight = imageView.fitWidth * aspectRatio
            }
        }

        setupRotationControls()
        setupDeleteButton()
        setupHoverEffects()
    }

    private fun setupRotationControls() {
        rotateLeftButton.style = buildControlButtonStyle(false)
        rotateLeftButton.isVisible = false
        rotateLeftButton.setOnAction { onRotateLeft() }
        StackPane.setAlignment(rotateLeftButton, Pos.BOTTOM_LEFT)
        StackPane.setMargin(rotateLeftButton, Insets(0.0, 0.0, 8.0, 8.0))
        
        rotateLeftButton.setOnMouseEntered {
            rotateLeftButton.style = buildControlButtonStyle(true)
            animateButtonScale(rotateLeftButton, 1.1)
        }
        rotateLeftButton.setOnMouseExited {
            rotateLeftButton.style = buildControlButtonStyle(false)
            animateButtonScale(rotateLeftButton, 1.0)
        }

        rotateRightButton.style = buildControlButtonStyle(false)
        rotateRightButton.isVisible = false
        rotateRightButton.setOnAction { onRotateRight() }
        StackPane.setAlignment(rotateRightButton, Pos.BOTTOM_RIGHT)
        StackPane.setMargin(rotateRightButton, Insets(0.0, 8.0, 8.0, 0.0))
        
        rotateRightButton.setOnMouseEntered {
            rotateRightButton.style = buildControlButtonStyle(true)
            animateButtonScale(rotateRightButton, 1.1)
        }
        rotateRightButton.setOnMouseExited {
            rotateRightButton.style = buildControlButtonStyle(false)
            animateButtonScale(rotateRightButton, 1.0)
        }

        container.children.addAll(rotateLeftButton, rotateRightButton)
    }

    private fun setupDeleteButton() {
        deleteButton.style = buildDangerButtonStyle(false)
        deleteButton.isVisible = false
        deleteButton.setOnAction { onDeleteRequested() }
        
        StackPane.setAlignment(deleteButton, Pos.TOP_RIGHT)
        StackPane.setMargin(deleteButton, Insets(8.0, 8.0, 0.0, 0.0))
        
        deleteButton.setOnMouseEntered {
            deleteButton.style = buildDangerButtonStyle(true)
            animateButtonScale(deleteButton, 1.1)
        }
        deleteButton.setOnMouseExited {
            deleteButton.style = buildDangerButtonStyle(false)
            animateButtonScale(deleteButton, 1.0)
        }
        
        container.children.add(deleteButton)
    }

    private fun buildControlButtonStyle(isHovered: Boolean): String {
        val bgColor = if (isHovered) StyleConstants.PRIMARY_600 else StyleConstants.PRIMARY_500
        val size = StyleConstants.CONTROL_BUTTON_SIZE
        
        return """
            -fx-background-color: $bgColor;
            -fx-text-fill: white;
            -fx-font-size: ${StyleConstants.CONTROL_BUTTON_ICON_SIZE};
            -fx-font-weight: bold;
            -fx-padding: 0;
            -fx-min-width: $size;
            -fx-min-height: $size;
            -fx-pref-width: $size;
            -fx-pref-height: $size;
            -fx-cursor: hand;
            -fx-effect: ${StyleConstants.SHADOW_MD};
            -fx-background-radius: ${StyleConstants.RADIUS_FULL};
        """.trimIndent()
    }

    private fun buildDangerButtonStyle(isHovered: Boolean): String {
        val bgColor = if (isHovered) StyleConstants.DANGER_600 else StyleConstants.DANGER_500
        val size = StyleConstants.CONTROL_BUTTON_SIZE
        
        return """
            -fx-background-color: $bgColor;
            -fx-text-fill: white;
            -fx-font-size: ${StyleConstants.CONTROL_BUTTON_ICON_SIZE};
            -fx-font-weight: bold;
            -fx-padding: 0;
            -fx-min-width: $size;
            -fx-min-height: $size;
            -fx-pref-width: $size;
            -fx-pref-height: $size;
            -fx-cursor: hand;
            -fx-effect: ${StyleConstants.SHADOW_MD};
            -fx-background-radius: ${StyleConstants.RADIUS_FULL};
        """.trimIndent()
    }

    private fun setupHoverEffects() {
        container.setOnMouseEntered {
            rotateLeftButton.isVisible = true
            rotateRightButton.isVisible = true
            if (isInCategoryView) {
                deleteButton.isVisible = true
            }
        }

        container.setOnMouseExited {
            rotateLeftButton.isVisible = false
            rotateRightButton.isVisible = false
            deleteButton.isVisible = false
        }
    }
    
    private fun animateButtonScale(button: Button, targetScale: Double) {
        val scaleTransition = ScaleTransition(Duration.millis(StyleConstants.ANIMATION_FAST), button).apply {
            toX = targetScale
            toY = targetScale
        }
        scaleTransition.play()
    }

    fun setInCategoryView(inCategory: Boolean) {
        isInCategoryView = inCategory
        if (!inCategory) {
            deleteButton.isVisible = false
        }
    }
}
