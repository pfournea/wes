package ui.component

import domain.model.Category
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import util.ImageCache
import util.StyleConstants

/**
 * Simplified category card component.
 * Features:
 * - Single thumbnail preview (first photo)
 * - Photo count badge
 * - Select button (eye icon) to filter photos in main grid
 * - Delete button (visible on hover)
 * - Empty state message
 * - Selection visual feedback (blue border + background)
 */
class CategoryCard(
    category: Category,
    private val onDeleteRequested: () -> Unit = {},
    private val onSelectionChanged: (Boolean) -> Unit = {}
) : VBox() {

    private var category: Category = category

    private val nameLabel = Label(category.name)
    private val selectButton = Button("👁")
    private val deleteButton = Button("❌")
    private val photoCountLabel = Label()
    private val thumbnailContainer = StackPane()
    private val emptyStateLabel = Label("Drag photos here\n👇")
    private val photoContainer = VBox()
    
    private var selected = false

    init {
        setupCard()
        setupHeader()
        setupPhotoCount()
        setupThumbnailView()
        setupPhotoContainer()
        updatePhotoCount()
    }

    private fun setupCard() {
        styleClass.add("category-card")
        spacing = 10.0
        padding = Insets(StyleConstants.CATEGORY_CARD_PADDING)
        prefWidth = StyleConstants.CATEGORY_CARD_WIDTH
        maxWidth = StyleConstants.CATEGORY_CARD_WIDTH
        isFillWidth = true
        
        updateCardStyle()

        // Subtle hover effect for delete button
        setOnMouseEntered {
            deleteButton.isVisible = true
        }

        setOnMouseExited {
            deleteButton.isVisible = false
        }
    }

    private fun setupHeader() {
        val headerBox = HBox(10.0)
        headerBox.alignment = Pos.CENTER_LEFT

        // Name label
        nameLabel.font = Font.font("System", FontWeight.SEMI_BOLD, 16.0)
        nameLabel.styleClass.add("category-name")

        // Select button (eye icon) - always visible
        selectButton.style = """
            -fx-background-color: transparent;
            -fx-text-fill: #0077ff;
            -fx-font-size: 20;
            -fx-padding: 0 5 0 5;
            -fx-cursor: hand;
        """.trimIndent()
        selectButton.setOnAction {
            toggleSelection()
        }

        // Delete button (hidden by default)
        deleteButton.style = """
            -fx-background-color: transparent;
            -fx-text-fill: #cc0000;
            -fx-font-size: 16;
            -fx-padding: 0 5 0 5;
            -fx-cursor: hand;
        """.trimIndent()
        deleteButton.isVisible = false
        deleteButton.setOnAction {
            showDeleteConfirmation()
        }

        val spacer = Region()
        HBox.setHgrow(spacer, Priority.ALWAYS)

        headerBox.children.addAll(nameLabel, spacer, selectButton, deleteButton)
        children.add(headerBox)
    }

    private fun setupPhotoCount() {
        photoCountLabel.font = Font.font("System", FontWeight.NORMAL, 13.0)
        photoCountLabel.style = "-fx-text-fill: #666666;"
        children.add(photoCountLabel)
    }

    private fun setupThumbnailView() {
        thumbnailContainer.minHeight = StyleConstants.CATEGORY_CARD_THUMBNAIL_SIZE
        thumbnailContainer.maxHeight = StyleConstants.CATEGORY_CARD_THUMBNAIL_SIZE
        thumbnailContainer.style = "-fx-alignment: center;"
        
        // Empty state setup
        emptyStateLabel.font = Font.font("System", 12.0)
        emptyStateLabel.style = """
            -fx-text-fill: #999999;
            -fx-text-alignment: center;
        """.trimIndent()
        emptyStateLabel.alignment = Pos.CENTER
        emptyStateLabel.maxWidth = Double.MAX_VALUE
        
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
            // Selected state: blue border + light blue background
            style = """
                -fx-background-color: ${StyleConstants.CATEGORY_SELECTED_BACKGROUND};
                -fx-background-radius: 8;
                -fx-border-color: ${StyleConstants.SELECTED_BORDER_COLOR};
                -fx-border-radius: 8;
                -fx-border-width: 3;
            """.trimIndent()
        } else {
            // Normal state
            style = """
                -fx-background-color: white;
                -fx-background-radius: 8;
                -fx-border-color: ${StyleConstants.CATEGORY_CARD_BORDER_COLOR};
                -fx-border-radius: 8;
                -fx-border-width: 1;
            """.trimIndent()
        }
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
        photoCountLabel.text = if (count == 1) "📷 $count photo" else "📷 $count photos"
        
        // Update thumbnail - show only first photo
        thumbnailContainer.children.clear()
        
        if (count == 0) {
            // Show empty state
            thumbnailContainer.children.add(emptyStateLabel)
        } else {
            // Show ONLY first photo as thumbnail
            val firstPhoto = category.photos[0]
            val image = ImageCache.getImage(firstPhoto.path, StyleConstants.CATEGORY_CARD_THUMBNAIL_SIZE)
            val imageView = ImageView(image).apply {
                fitWidth = StyleConstants.CATEGORY_CARD_THUMBNAIL_SIZE
                fitHeight = StyleConstants.CATEGORY_CARD_THUMBNAIL_SIZE
                isPreserveRatio = true
                isSmooth = false  // No high-quality rendering needed
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
            style = """
                -fx-background-color: #e3f2fd;
                -fx-background-radius: 8;
                -fx-border-color: ${StyleConstants.SELECTED_BORDER_COLOR};
                -fx-border-radius: 8;
                -fx-border-width: 3;
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
