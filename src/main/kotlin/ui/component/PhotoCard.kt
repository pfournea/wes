package ui.component

import domain.model.Photo
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle

/**
 * Wrapper that adds a delete button overlay to an ImageView.
 * The delete button appears on hover and is only visible when viewing a category.
 * This creates a StackPane container with the ImageView and delete button.
 */
class PhotoCard(
    val imageView: ImageView,
    val photo: Photo,
    private val onDeleteRequested: () -> Unit = {},
    isInCategory: Boolean = false
) {
    private val deleteButton = Button("❌")
    private var isInCategoryView = isInCategory
    val container = StackPane()

    init {
        setupContainer()
    }

    private fun setupContainer() {
        // Add the image view as the main content
        container.children.add(imageView)

        // Setup delete button with white circle background
        setupDeleteButton()

        // Set up hover effects
        setupHoverEffects()
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
            if (isInCategoryView) {
                deleteButton.isVisible = true
            }
        }

        container.setOnMouseExited {
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

