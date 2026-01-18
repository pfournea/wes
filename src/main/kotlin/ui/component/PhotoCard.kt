package ui.component

import domain.model.Photo
import javafx.animation.ScaleTransition
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import javafx.util.Duration
import util.StyleConstants

/**
 * Wrapper that adds a delete button and rotation controls to an ImageView.
 * The delete button appears on hover and is only visible when viewing a category (top-right).
 * Rotation controls appear on hover for all photos (bottom-left and bottom-right corners).
 * This creates a StackPane container with the ImageView and control buttons.
 * Modern design with gradient buttons and smooth animations.
 */
class PhotoCard(
    val imageView: ImageView,
    val photo: Photo,
    private val onDeleteRequested: () -> Unit = {},
    private val onRotateLeft: () -> Unit = {},
    private val onRotateRight: () -> Unit = {},
    isInCategory: Boolean = false
) {
    private val deleteButton = Button("✕")
    private val rotateLeftButton = Button("↶")
    private val rotateRightButton = Button("↷")
    private var isInCategoryView = isInCategory
    val container = StackPane()

    init {
        setupContainer()
    }

    private fun setupContainer() {
        // Add the image view as the main content
        container.children.add(imageView)

        // Setup rotation controls (corners)
        setupRotationControls()

        // Setup delete button (top-right)
        setupDeleteButton()

        // Set up hover effects
        setupHoverEffects()
    }

    private fun setupRotationControls() {
        // Left rotation button (bottom-left corner) - modern gradient style
        rotateLeftButton.style = """
            -fx-background-color: linear-gradient(to bottom right, 
                ${StyleConstants.ROTATION_BUTTON_GRADIENT_START}, 
                ${StyleConstants.ROTATION_BUTTON_GRADIENT_END});
            -fx-text-fill: white;
            -fx-font-size: ${StyleConstants.ROTATION_BUTTON_ICON_SIZE};
            -fx-font-weight: bold;
            -fx-padding: 0;
            -fx-min-width: ${StyleConstants.ROTATION_BUTTON_SIZE};
            -fx-min-height: ${StyleConstants.ROTATION_BUTTON_SIZE};
            -fx-pref-width: ${StyleConstants.ROTATION_BUTTON_SIZE};
            -fx-pref-height: ${StyleConstants.ROTATION_BUTTON_SIZE};
            -fx-cursor: hand;
            -fx-effect: ${StyleConstants.ELEVATION_3};
            -fx-background-radius: 50%;
        """.trimIndent()

        rotateLeftButton.opacity = 0.0
        rotateLeftButton.setOnAction {
            onRotateLeft()
        }
        
        // Hover animation
        rotateLeftButton.setOnMouseEntered {
            animateButtonScale(rotateLeftButton, 1.1)
        }
        rotateLeftButton.setOnMouseExited {
            animateButtonScale(rotateLeftButton, 1.0)
        }
        
        StackPane.setAlignment(rotateLeftButton, Pos.BOTTOM_LEFT)
        StackPane.setMargin(rotateLeftButton, Insets(0.0, 0.0, 8.0, 8.0))

        // Right rotation button (bottom-right corner) - modern gradient style
        rotateRightButton.style = """
            -fx-background-color: linear-gradient(to bottom right, 
                ${StyleConstants.ROTATION_BUTTON_GRADIENT_START}, 
                ${StyleConstants.ROTATION_BUTTON_GRADIENT_END});
            -fx-text-fill: white;
            -fx-font-size: ${StyleConstants.ROTATION_BUTTON_ICON_SIZE};
            -fx-font-weight: bold;
            -fx-padding: 0;
            -fx-min-width: ${StyleConstants.ROTATION_BUTTON_SIZE};
            -fx-min-height: ${StyleConstants.ROTATION_BUTTON_SIZE};
            -fx-pref-width: ${StyleConstants.ROTATION_BUTTON_SIZE};
            -fx-pref-height: ${StyleConstants.ROTATION_BUTTON_SIZE};
            -fx-cursor: hand;
            -fx-effect: ${StyleConstants.ELEVATION_3};
            -fx-background-radius: 50%;
        """.trimIndent()

        rotateRightButton.opacity = 0.0
        rotateRightButton.setOnAction {
            onRotateRight()
        }
        
        // Hover animation
        rotateRightButton.setOnMouseEntered {
            animateButtonScale(rotateRightButton, 1.1)
        }
        rotateRightButton.setOnMouseExited {
            animateButtonScale(rotateRightButton, 1.0)
        }
        
        StackPane.setAlignment(rotateRightButton, Pos.BOTTOM_RIGHT)
        StackPane.setMargin(rotateRightButton, Insets(0.0, 8.0, 8.0, 0.0))

        container.children.addAll(rotateLeftButton, rotateRightButton)
    }

    private fun setupDeleteButton() {
        // Style the delete button - danger gradient
        deleteButton.style = """
            -fx-background-color: linear-gradient(to bottom right, 
                ${StyleConstants.WARNING_GRADIENT_START}, 
                ${StyleConstants.WARNING_GRADIENT_END});
            -fx-text-fill: white;
            -fx-font-size: 18;
            -fx-font-weight: bold;
            -fx-padding: 0;
            -fx-min-width: 44;
            -fx-min-height: 44;
            -fx-pref-width: 44;
            -fx-pref-height: 44;
            -fx-cursor: hand;
            -fx-effect: ${StyleConstants.ELEVATION_3};
            -fx-background-radius: 50%;
        """.trimIndent()

        deleteButton.opacity = 0.0
        deleteButton.setOnAction {
            onDeleteRequested()
        }
        
        // Hover animation
        deleteButton.setOnMouseEntered {
            animateButtonScale(deleteButton, 1.1)
        }
        deleteButton.setOnMouseExited {
            animateButtonScale(deleteButton, 1.0)
        }

        // Position the delete button at the top-right corner
        StackPane.setAlignment(deleteButton, Pos.TOP_RIGHT)
        StackPane.setMargin(deleteButton, Insets(8.0, 8.0, 0.0, 0.0))
        container.children.add(deleteButton)
    }

    private fun setupHoverEffects() {
        container.setOnMouseEntered {
            rotateLeftButton.opacity = 1.0
            rotateRightButton.opacity = 1.0
            if (isInCategoryView) {
                deleteButton.opacity = 1.0
            }
        }

        container.setOnMouseExited {
            rotateLeftButton.opacity = 0.0
            rotateRightButton.opacity = 0.0
            deleteButton.opacity = 0.0
        }
    }
    
    private fun animateButtonScale(button: Button, targetScale: Double) {
        val scaleTransition = ScaleTransition(Duration.millis(150.0), button).apply {
            toX = targetScale
            toY = targetScale
        }
        scaleTransition.play()
    }

    /**
     * Sets whether the photo is in a category view (enables delete button).
     */
    fun setInCategoryView(inCategory: Boolean) {
        isInCategoryView = inCategory
        if (!inCategory) {
            deleteButton.opacity = 0.0
        }
    }
}
