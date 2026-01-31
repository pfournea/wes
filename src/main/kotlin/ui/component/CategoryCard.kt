package ui.component

import domain.model.Category
import javafx.animation.ScaleTransition
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.util.Duration
import util.Icons
import util.ImageCache
import util.StyleConstants

/**
 * Professional category card component with clean, enterprise design.
 */
class CategoryCard(
    category: Category,
    private val onDeleteRequested: () -> Unit = {},
    private val onSelectionChanged: (Boolean) -> Unit = {}
) : VBox() {

    private var category: Category = category

    private val nameLabel = Label(category.name)
    private val selectButton = Button(Icons.VIEW)
    private val deleteButton = Button(Icons.REMOVE)
    private val photoCountBadge = Label()
    private val thumbnailContainer = StackPane()
    
    private var selected = false

    init {
        setupCard()
        setupHeader()
        setupPhotoCountBadge()
        setupThumbnailView()
        updatePhotoCount()
    }

    private fun setupCard() {
        styleClass.add("category-card")
        spacing = StyleConstants.SPACING_MD
        padding = Insets(StyleConstants.CATEGORY_CARD_PADDING)
        prefWidth = StyleConstants.CATEGORY_CARD_WIDTH
        maxWidth = StyleConstants.CATEGORY_CARD_WIDTH
        isFillWidth = true
        
        updateCardStyle()

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
        val headerBox = HBox(StyleConstants.SPACING_SM)
        headerBox.alignment = Pos.CENTER_LEFT

        nameLabel.font = Font.font("System", FontWeight.SEMI_BOLD, StyleConstants.FONT_SIZE_LG)
        nameLabel.style = "-fx-text-fill: ${StyleConstants.TEXT_PRIMARY};"
        nameLabel.minWidth = Region.USE_PREF_SIZE
        nameLabel.tooltip = Tooltip(category.name)

        selectButton.style = buildIconButtonStyle(StyleConstants.PRIMARY_500, false)
        selectButton.setOnAction { toggleSelection() }
        
        selectButton.setOnMouseEntered {
            selectButton.style = buildIconButtonStyle(StyleConstants.PRIMARY_600, true)
            animateButtonScale(selectButton, 1.05)
        }
        selectButton.setOnMouseExited {
            selectButton.style = buildIconButtonStyle(StyleConstants.PRIMARY_500, false)
            animateButtonScale(selectButton, 1.0)
        }

        deleteButton.style = buildIconButtonStyle(StyleConstants.DANGER_500, false)
        deleteButton.opacity = 0.0
        deleteButton.setOnAction { showDeleteConfirmation() }
        
        deleteButton.setOnMouseEntered {
            deleteButton.style = buildIconButtonStyle(StyleConstants.DANGER_600, true)
            animateButtonScale(deleteButton, 1.05)
        }
        deleteButton.setOnMouseExited {
            deleteButton.style = buildIconButtonStyle(StyleConstants.DANGER_500, false)
            animateButtonScale(deleteButton, 1.0)
        }

        val spacer = Region()
        HBox.setHgrow(spacer, Priority.ALWAYS)

        headerBox.children.addAll(nameLabel, spacer, selectButton, deleteButton)
        children.add(headerBox)
    }

    private fun setupPhotoCountBadge() {
        photoCountBadge.font = Font.font("System", FontWeight.MEDIUM, StyleConstants.FONT_SIZE_SM)
        photoCountBadge.style = """
            -fx-background-color: ${StyleConstants.NEUTRAL_100};
            -fx-text-fill: ${StyleConstants.TEXT_SECONDARY};
            -fx-padding: 4 10 4 10;
            -fx-background-radius: ${StyleConstants.RADIUS_FULL};
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
            -fx-background-color: ${StyleConstants.NEUTRAL_50};
            -fx-background-radius: ${StyleConstants.RADIUS_BASE};
            -fx-border-color: ${StyleConstants.BORDER_DEFAULT};
            -fx-border-width: 1;
            -fx-border-radius: ${StyleConstants.RADIUS_BASE};
        """.trimIndent()
        
        children.add(thumbnailContainer)
    }

    private fun buildIconButtonStyle(color: String, isHovered: Boolean): String {
        val bgColor = if (isHovered) color else "transparent"
        val textColor = if (isHovered) "white" else color
        val borderColor = if (isHovered) color else StyleConstants.BORDER_DEFAULT
        
        return """
            -fx-background-color: $bgColor;
            -fx-text-fill: $textColor;
            -fx-font-size: ${StyleConstants.FONT_SIZE_BASE};
            -fx-font-weight: bold;
            -fx-padding: 6 10 6 10;
            -fx-background-radius: ${StyleConstants.RADIUS_MD};
            -fx-border-color: $borderColor;
            -fx-border-radius: ${StyleConstants.RADIUS_MD};
            -fx-border-width: 1;
            -fx-cursor: hand;
        """.trimIndent()
    }

    private fun toggleSelection() {
        selected = !selected
        updateCardStyle()
        onSelectionChanged(selected)
    }

    private fun updateCardStyle() {
        if (selected) {
            style = """
                -fx-background-color: ${StyleConstants.PRIMARY_50};
                -fx-background-radius: ${StyleConstants.CATEGORY_CARD_BORDER_RADIUS};
                -fx-border-color: ${StyleConstants.PRIMARY_500};
                -fx-border-radius: ${StyleConstants.CATEGORY_CARD_BORDER_RADIUS};
                -fx-border-width: 2;
                -fx-effect: ${StyleConstants.SHADOW_MD};
            """.trimIndent()
        } else {
            style = """
                -fx-background-color: white;
                -fx-background-radius: ${StyleConstants.CATEGORY_CARD_BORDER_RADIUS};
                -fx-border-color: ${StyleConstants.CATEGORY_CARD_BORDER_COLOR};
                -fx-border-radius: ${StyleConstants.CATEGORY_CARD_BORDER_RADIUS};
                -fx-border-width: 1;
                -fx-effect: ${StyleConstants.SHADOW_SM};
            """.trimIndent()
        }
    }
    
    private fun animateCardElevation(elevated: Boolean) {
        val shadow = if (elevated) StyleConstants.SHADOW_MD else StyleConstants.SHADOW_SM
        val borderColor = if (elevated) StyleConstants.CATEGORY_CARD_HOVER_BORDER_COLOR else StyleConstants.CATEGORY_CARD_BORDER_COLOR
        
        if (selected) {
            style = """
                -fx-background-color: ${StyleConstants.PRIMARY_50};
                -fx-background-radius: ${StyleConstants.CATEGORY_CARD_BORDER_RADIUS};
                -fx-border-color: ${StyleConstants.PRIMARY_500};
                -fx-border-radius: ${StyleConstants.CATEGORY_CARD_BORDER_RADIUS};
                -fx-border-width: 2;
                -fx-effect: ${StyleConstants.SHADOW_LG};
            """.trimIndent()
        } else {
            style = """
                -fx-background-color: white;
                -fx-background-radius: ${StyleConstants.CATEGORY_CARD_BORDER_RADIUS};
                -fx-border-color: $borderColor;
                -fx-border-radius: ${StyleConstants.CATEGORY_CARD_BORDER_RADIUS};
                -fx-border-width: 1;
                -fx-effect: $shadow;
            """.trimIndent()
        }
    }
    
    private fun animateButtonScale(button: Button, targetScale: Double) {
        val scaleTransition = ScaleTransition(Duration.millis(StyleConstants.ANIMATION_FAST), button).apply {
            toX = targetScale
            toY = targetScale
        }
        scaleTransition.play()
    }

    private fun showDeleteConfirmation() {
        val alert = Alert(Alert.AlertType.CONFIRMATION)
        alert.title = "Delete Category"
        alert.headerText = "Delete \"${category.name}\"?"
        alert.contentText = "Photos will be returned to the main grid."

        val result = alert.showAndWait()
        if (result.isPresent && result.get() == ButtonType.OK) {
            onDeleteRequested()
        }
    }

    fun updatePhotoCount() {
        val count = category.photos.size
        photoCountBadge.text = if (count == 1) "$count photo" else "$count photos"
        
        thumbnailContainer.children.clear()
        
        if (count == 0) {
            val emptyStateBox = VBox(StyleConstants.SPACING_SM)
            emptyStateBox.alignment = Pos.CENTER
            
            val icon = Label(Icons.IMAGE)
            icon.font = Font.font("System", 32.0)
            icon.style = "-fx-text-fill: ${StyleConstants.NEUTRAL_300};"
            
            val text = Label("Drop photos here")
            text.font = Font.font("System", FontWeight.NORMAL, StyleConstants.FONT_SIZE_SM)
            text.style = "-fx-text-fill: ${StyleConstants.TEXT_MUTED};"
            
            emptyStateBox.children.addAll(icon, text)
            thumbnailContainer.children.add(emptyStateBox)
        } else {
            val firstPhoto = category.photos[0]
            val image = ImageCache.getImage(firstPhoto.path, StyleConstants.CATEGORY_CARD_THUMBNAIL_SIZE)
            val imageView = ImageView(image).apply {
                fitWidth = StyleConstants.CATEGORY_CARD_THUMBNAIL_SIZE
                fitHeight = StyleConstants.CATEGORY_CARD_THUMBNAIL_SIZE
                isPreserveRatio = true
                isSmooth = true
                
                if (firstPhoto.rotationDegrees != 0) {
                    util.ImageUtils.applyRotation(this, firstPhoto.rotationDegrees)
                }
            }
            
            thumbnailContainer.children.add(imageView)
        }
    }

    fun getPhotoContainer(): VBox = VBox()

    fun setDragOver(isDragOver: Boolean) {
        if (isDragOver) {
            style = """
                -fx-background-color: ${StyleConstants.PRIMARY_50};
                -fx-background-radius: ${StyleConstants.CATEGORY_CARD_BORDER_RADIUS};
                -fx-border-color: ${StyleConstants.PRIMARY_500};
                -fx-border-radius: ${StyleConstants.CATEGORY_CARD_BORDER_RADIUS};
                -fx-border-width: 2;
                -fx-effect: ${StyleConstants.SHADOW_LG};
            """.trimIndent()
        } else {
            updateCardStyle()
        }
    }

    fun setSelected(isSelected: Boolean) {
        if (selected != isSelected) {
            selected = isSelected
            updateCardStyle()
        }
    }

    fun isSelected(): Boolean = selected

    fun getCategory(): Category = category

    fun updateCategory(newCategory: Category) {
        category = newCategory
        updatePhotoCount()
    }
}
