package ui.component

import domain.model.Category
import javafx.animation.FadeTransition
import javafx.animation.ScaleTransition
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.util.Duration
import util.ImageCache
import util.StyleConstants

/**
 * Modern category card component with glassmorphism design.
 * Features:
 * - Single thumbnail preview (first photo)
 * - Photo count badge with gradient
 * - Select button (eye icon) to filter photos in main grid
 * - Delete button (visible on hover with animation)
 * - Empty state message
 * - Selection visual feedback (gradient border + elevation)
 * - Smooth animations and micro-interactions
 */
class CategoryCard(
    category: Category,
    private val onDeleteRequested: () -> Unit = {},
    private val onSelectionChanged: (Boolean) -> Unit = {}
) : VBox() {

    private var category: Category = category

    private val nameLabel = Label(category.name)
    private val selectButton = Button("👁")
    private val deleteButton = Button("✕")
    private val photoCountBadge = Label()
    private val thumbnailContainer = StackPane()
    private val emptyStateLabel = Label("Drag photos here")
    private val emptyStateIcon = Label("📸")
    private val photoContainer = VBox()
    
    private var selected = false

    init {
        setupCard()
        setupHeader()
        setupPhotoCountBadge()
        setupThumbnailView()
        setupPhotoContainer()
        updatePhotoCount()
    }

    private fun setupCard() {
        styleClass.add("category-card")
        spacing = 12.0
        padding = Insets(StyleConstants.CATEGORY_CARD_PADDING)
        prefWidth = StyleConstants.CATEGORY_CARD_WIDTH
        maxWidth = StyleConstants.CATEGORY_CARD_WIDTH
        isFillWidth = true
        
        updateCardStyle()

        // Smooth hover animation
        setOnMouseEntered {
            deleteButton.opacity = 1.0
            animateCardElevation(true)
        }

        setOnMouseExited {
            deleteButton.opacity = 0.0
            animateCardElevation(false)
        }
    }

    private fun setupHeader() {
        val headerBox = HBox(12.0)
        headerBox.alignment = Pos.CENTER_LEFT

        // Name label with modern typography
        nameLabel.font = Font.font("System", FontWeight.BOLD, 17.0)
        nameLabel.styleClass.add("category-name")
        nameLabel.style = "-fx-text-fill: #2d3748;"

        // Select button (eye icon) - modern gradient style
        selectButton.style = """
            -fx-background-color: linear-gradient(to bottom right, 
                ${StyleConstants.PRIMARY_GRADIENT_START}, 
                ${StyleConstants.PRIMARY_GRADIENT_END});
            -fx-text-fill: white;
            -fx-font-size: 18;
            -fx-padding: 6 10 6 10;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            -fx-effect: ${StyleConstants.ELEVATION_1};
        """.trimIndent()
        selectButton.setOnAction {
            toggleSelection()
        }
        
        // Hover effect for select button
        selectButton.setOnMouseEntered {
            animateButtonScale(selectButton, 1.1)
        }
        selectButton.setOnMouseExited {
            animateButtonScale(selectButton, 1.0)
        }

        // Delete button (hidden by default) - danger style
        deleteButton.style = """
            -fx-background-color: linear-gradient(to bottom right, 
                ${StyleConstants.WARNING_GRADIENT_START}, 
                ${StyleConstants.WARNING_GRADIENT_END});
            -fx-text-fill: white;
            -fx-font-size: 16;
            -fx-font-weight: bold;
            -fx-padding: 6 10 6 10;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            -fx-effect: ${StyleConstants.ELEVATION_1};
        """.trimIndent()
        deleteButton.opacity = 0.0
        deleteButton.setOnAction {
            showDeleteConfirmation()
        }
        
        // Hover effect for delete button
        deleteButton.setOnMouseEntered {
            animateButtonScale(deleteButton, 1.1)
        }
        deleteButton.setOnMouseExited {
            animateButtonScale(deleteButton, 1.0)
        }

        val spacer = Region()
        HBox.setHgrow(spacer, Priority.ALWAYS)

        headerBox.children.addAll(nameLabel, spacer, selectButton, deleteButton)
        children.add(headerBox)
    }

    private fun setupPhotoCountBadge() {
        photoCountBadge.font = Font.font("System", FontWeight.SEMI_BOLD, 13.0)
        photoCountBadge.style = """
            -fx-background-color: linear-gradient(to right, 
                ${StyleConstants.ACCENT_GRADIENT_START}, 
                ${StyleConstants.ACCENT_GRADIENT_END});
            -fx-text-fill: white;
            -fx-padding: 4 12 4 12;
            -fx-background-radius: 12;
            -fx-effect: ${StyleConstants.ELEVATION_1};
        """.trimIndent()
        
        val badgeContainer = HBox(photoCountBadge)
        badgeContainer.alignment = Pos.CENTER_LEFT
        children.add(badgeContainer)
    }

    private fun setupThumbnailView() {
        thumbnailContainer.minHeight = StyleConstants.CATEGORY_CARD_THUMBNAIL_SIZE
        thumbnailContainer.maxHeight = StyleConstants.CATEGORY_CARD_THUMBNAIL_SIZE
        thumbnailContainer.style = """
            -fx-alignment: center;
            -fx-background-color: #f7fafc;
            -fx-background-radius: 12;
            -fx-border-color: #e2e8f0;
            -fx-border-width: 1;
            -fx-border-radius: 12;
        """.trimIndent()
        
        // Empty state setup - modern style
        val emptyStateBox = VBox(8.0)
        emptyStateBox.alignment = Pos.CENTER
        
        emptyStateIcon.font = Font.font("System", 40.0)
        emptyStateIcon.style = "-fx-text-fill: #cbd5e0;"
        
        emptyStateLabel.font = Font.font("System", FontWeight.NORMAL, 13.0)
        emptyStateLabel.style = "-fx-text-fill: #a0aec0;"
        emptyStateLabel.alignment = Pos.CENTER
        
        emptyStateBox.children.addAll(emptyStateIcon, emptyStateLabel)
        
        children.add(thumbnailContainer)
    }

    private fun setupPhotoContainer() {
        photoContainer.spacing = 5.0
        photoContainer.styleClass.add("photo-container")
        photoContainer.minHeight = 50.0
    }

    private fun toggleSelection() {
        selected = !selected
        updateCardStyle()
        onSelectionChanged(selected)
    }

    private fun updateCardStyle() {
        if (selected) {
            // Use THE SAME constant as photo selection
            style = StyleConstants.SELECTED_STYLE
        } else {
            // Normal state - clean card with subtle shadow
            style = """
                -fx-background-color: white;
                -fx-background-radius: ${StyleConstants.CATEGORY_CARD_BORDER_RADIUS};
                -fx-border-color: ${StyleConstants.CATEGORY_CARD_BORDER_COLOR};
                -fx-border-radius: ${StyleConstants.CATEGORY_CARD_BORDER_RADIUS};
                -fx-border-width: 1;
                -fx-effect: ${StyleConstants.ELEVATION_2};
            """.trimIndent()
        }
    }
    
    private fun animateCardElevation(elevated: Boolean) {
        val effect = if (elevated) StyleConstants.ELEVATION_3 else StyleConstants.ELEVATION_2
        
        if (selected) {
            // Use THE SAME constant as photo selection
            style = StyleConstants.SELECTED_STYLE
        } else {
            style = """
                -fx-background-color: white;
                -fx-background-radius: ${StyleConstants.CATEGORY_CARD_BORDER_RADIUS};
                -fx-border-color: ${if (elevated) StyleConstants.CATEGORY_CARD_HOVER_BORDER_COLOR else StyleConstants.CATEGORY_CARD_BORDER_COLOR};
                -fx-border-radius: ${StyleConstants.CATEGORY_CARD_BORDER_RADIUS};
                -fx-border-width: 1;
                -fx-effect: $effect;
            """.trimIndent()
        }
    }
    
    private fun animateButtonScale(button: Button, targetScale: Double) {
        val scaleTransition = ScaleTransition(Duration.millis(150.0), button).apply {
            toX = targetScale
            toY = targetScale
        }
        scaleTransition.play()
    }

    private fun showDeleteConfirmation() {
        val alert = Alert(Alert.AlertType.CONFIRMATION)
        alert.title = "Delete Category"
        alert.headerText = "Delete \"${category.name}\"?"
        alert.contentText = "Are you sure you want to delete this category? Photos will be returned to the main grid."

        val result = alert.showAndWait()
        if (result.isPresent && result.get() == ButtonType.OK) {
            onDeleteRequested()
        }
    }

    /**
     * Updates the photo count and thumbnail preview.
     */
    fun updatePhotoCount() {
        val count = category.photos.size
        photoCountBadge.text = if (count == 1) "$count photo" else "$count photos"
        
        // Update thumbnail - show only first photo
        thumbnailContainer.children.clear()
        
        if (count == 0) {
            // Show empty state
            val emptyStateBox = VBox(8.0)
            emptyStateBox.alignment = Pos.CENTER
            
            val icon = Label("📸")
            icon.font = Font.font("System", 40.0)
            icon.style = "-fx-text-fill: #cbd5e0;"
            
            val text = Label("Drag photos here")
            text.font = Font.font("System", FontWeight.NORMAL, 13.0)
            text.style = "-fx-text-fill: #a0aec0;"
            
            emptyStateBox.children.addAll(icon, text)
            thumbnailContainer.children.add(emptyStateBox)
        } else {
            // Show ONLY first photo as thumbnail
            val firstPhoto = category.photos[0]
            val image = ImageCache.getImage(firstPhoto.path, StyleConstants.CATEGORY_CARD_THUMBNAIL_SIZE)
            val imageView = ImageView(image).apply {
                fitWidth = StyleConstants.CATEGORY_CARD_THUMBNAIL_SIZE
                fitHeight = StyleConstants.CATEGORY_CARD_THUMBNAIL_SIZE
                isPreserveRatio = true
                isSmooth = true
                
                style = "-fx-background-radius: 8; -fx-border-radius: 8;"
                
                // Apply rotation if needed
                if (firstPhoto.rotationDegrees != 0) {
                    util.ImageUtils.applyRotation(this, firstPhoto.rotationDegrees)
                }
            }
            
            thumbnailContainer.children.add(imageView)
        }
    }

    /**
     * Gets the photo container for drag-and-drop operations.
     */
    fun getPhotoContainer(): VBox {
        return photoContainer
    }

    /**
     * Sets the drag-over visual feedback.
     */
    fun setDragOver(isDragOver: Boolean) {
        if (isDragOver) {
            // Drag-over state: exact same border as selection, with light background tint
            style = """
                -fx-background-color: rgba(76, 99, 210, 0.08);
                -fx-background-radius: 12;
                -fx-border-color: #4c63d2;
                -fx-border-radius: 12;
                -fx-border-width: 5;
                -fx-effect: dropshadow(gaussian, rgba(76, 99, 210, 0.6), 28.0, 0.7, 0, 0);
            """.trimIndent()
        } else {
            updateCardStyle()
        }
    }

    /**
     * Sets the selection state programmatically.
     */
    fun setSelected(isSelected: Boolean) {
        if (selected != isSelected) {
            selected = isSelected
            updateCardStyle()
        }
    }

    /**
     * Gets the selection state.
     */
    fun isSelected(): Boolean {
        return selected
    }

    /**
     * Gets the category associated with this card.
     */
    fun getCategory(): Category {
        return category
    }

    /**
     * Updates the category reference and refreshes the display.
     */
    fun updateCategory(newCategory: Category) {
        category = newCategory
        updatePhotoCount()
    }
}
