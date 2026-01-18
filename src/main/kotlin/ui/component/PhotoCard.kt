package ui.component

import domain.model.Photo
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import util.StyleConstants

/**
 * Wrapper that adds a delete button and rotation controls to an ImageView.
 * The delete button appears on hover and is only visible when viewing a category (top-right).
 * Rotation controls appear on hover for all photos (bottom-left and bottom-right corners).
 * This creates a StackPane container with the ImageView and control buttons.
 */
class PhotoCard(
    val imageView: ImageView,
    val photo: Photo,
    private val onDeleteRequested: () -> Unit = {},
    private val onRotateLeft: () -> Unit = {},
    private val onRotateRight: () -> Unit = {},
    isInCategory: Boolean = false
) {
    private val deleteButton = Button("❌")
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
        // Left rotation button (bottom-left corner)
        rotateLeftButton.style = """
            -fx-background-color: white;
            -fx-text-fill: #333333;
            -fx-font-size: ${StyleConstants.ROTATION_BUTTON_ICON_SIZE};
            -fx-padding: 0;
            -fx-min-width: ${StyleConstants.ROTATION_BUTTON_SIZE};
            -fx-min-height: ${StyleConstants.ROTATION_BUTTON_SIZE};
            -fx-pref-width: ${StyleConstants.ROTATION_BUTTON_SIZE};
            -fx-pref-height: ${StyleConstants.ROTATION_BUTTON_SIZE};
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 0);
            -fx-background-radius: 50%;
        """.trimIndent()

        rotateLeftButton.isVisible = false
        rotateLeftButton.setOnAction {
            onRotateLeft()
        }
        StackPane.setAlignment(rotateLeftButton, Pos.BOTTOM_LEFT)
        StackPane.setMargin(rotateLeftButton, Insets(0.0, 0.0, 5.0, 5.0))

        // Right rotation button (bottom-right corner)
        rotateRightButton.style = """
            -fx-background-color: white;
            -fx-text-fill: #333333;
            -fx-font-size: ${StyleConstants.ROTATION_BUTTON_ICON_SIZE};
            -fx-padding: 0;
            -fx-min-width: ${StyleConstants.ROTATION_BUTTON_SIZE};
            -fx-min-height: ${StyleConstants.ROTATION_BUTTON_SIZE};
            -fx-pref-width: ${StyleConstants.ROTATION_BUTTON_SIZE};
            -fx-pref-height: ${StyleConstants.ROTATION_BUTTON_SIZE};
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 0);
            -fx-background-radius: 50%;
        """.trimIndent()

        rotateRightButton.isVisible = false
        rotateRightButton.setOnAction {
            onRotateRight()
        }
        StackPane.setAlignment(rotateRightButton, Pos.BOTTOM_RIGHT)
        StackPane.setMargin(rotateRightButton, Insets(0.0, 5.0, 5.0, 0.0))

        container.children.addAll(rotateLeftButton, rotateRightButton)
    }

    private fun setupDeleteButton() {
        // Style the delete button
        deleteButton.style = """
            -fx-background-color: white;
            -fx-text-fill: #cc0000;
            -fx-font-size: 18;
            -fx-padding: 0;
            -fx-min-width: 40;
            -fx-min-height: 40;
            -fx-pref-width: 40;
            -fx-pref-height: 40;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 0);
            -fx-background-radius: 50%;
        """.trimIndent()

        deleteButton.isVisible = false
        deleteButton.setOnAction {
            onDeleteRequested()
        }

        // Position the delete button at the top-right corner
        StackPane.setAlignment(deleteButton, Pos.TOP_RIGHT)
        container.children.add(deleteButton)
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

    /**
     * Sets whether the photo is in a category view (enables delete button).
     */
    fun setInCategoryView(inCategory: Boolean) {
        isInCategoryView = inCategory
        if (!inCategory) {
            deleteButton.isVisible = false
        }
    }
}
