package ui

import domain.model.Category
import domain.service.CategoryService
import domain.service.FileService
import domain.service.PhotoService
import domain.service.SelectionService
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.TilePane
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.FileChooser
import javafx.stage.Stage
import ui.handler.DragDropHandler
import ui.handler.SelectionHandler
import util.ImageUtils
import util.StyleConstants
import java.io.File

/**
 * Main JavaFX Application for Photo Categorizer.
 * Orchestrates UI components and coordinates between UI and services.
 */
class PhotoCategorizerApp : Application() {
    // Services
    private val photoService = PhotoService()
    private val selectionService = SelectionService()
    private val categoryService = CategoryService()
    private val fileService = FileService()

    // UI Components
    private val imageViews = mutableListOf<ImageView>()
    private val imageContainer = TilePane().apply {
        orientation = javafx.geometry.Orientation.HORIZONTAL
        hgap = 10.0
        vgap = 10.0
        prefColumns = StyleConstants.DEFAULT_COLUMNS
    }
    private val categoryContainer = HBox().apply { spacing = 10.0 }

    // Handlers
    private lateinit var selectionHandler: SelectionHandler
    private lateinit var dragDropHandler: DragDropHandler

    override fun start(primaryStage: Stage) {
        primaryStage.title = "Photo Categorizer"

        // Initialize handlers
        selectionHandler = SelectionHandler(
            selectionService,
            photoService,
            imageViews
        ) { imageContainer.prefColumns.toInt() }

        dragDropHandler = DragDropHandler(
            photoService,
            categoryService,
            imageViews
        ) {
            updateImageDisplay()
            selectionHandler.clearSelection()
        }

        // Create UI
        val uploadButton = createUploadButton(primaryStage)
        val addCategoryButton = createAddCategoryButton()

        val categoryScrollPane = ScrollPane(categoryContainer).apply {
            fitToWidthProperty().set(false)
        }

        val categoryVBox = VBox(addCategoryButton, categoryScrollPane).apply {
            spacing = 10.0
        }

        val scrollPane = ScrollPane(imageContainer).apply {
            fitToWidthProperty().set(true)
        }

        val root = HBox(scrollPane, categoryVBox).apply {
            spacing = 10.0
        }

        val controlsVBox = VBox(uploadButton).apply {
            spacing = 10.0
        }

        val mainVBox = VBox(controlsVBox, root)

        primaryStage.scene = Scene(mainVBox)
        primaryStage.isMaximized = true
        primaryStage.show()

        // Setup responsive layout
        setupResponsiveLayout(primaryStage, scrollPane, categoryVBox, categoryScrollPane)
    }

    private fun createUploadButton(primaryStage: Stage): Button {
        return Button("Upload Zip File").apply {
            setOnAction {
                val fileChooser = FileChooser().apply {
                    title = "Select Zip File"
                    extensionFilters.add(FileChooser.ExtensionFilter("Zip Files", "*.zip"))
                }
                val file = fileChooser.showOpenDialog(primaryStage)
                if (file != null) {
                    loadPhotosFromZip(file)
                }
            }
        }
    }

    private fun createAddCategoryButton(): Button {
        return Button("Add Category").apply {
            setOnAction {
                addCategory()
            }
        }
    }

    private fun setupResponsiveLayout(
        primaryStage: Stage,
        scrollPane: ScrollPane,
        categoryVBox: VBox,
        categoryScrollPane: ScrollPane
    ) {
        primaryStage.scene.widthProperty().addListener { _, _, newValue ->
            val width = newValue.toDouble()
            scrollPane.prefWidth = width * StyleConstants.PHOTO_PANE_WIDTH_RATIO
            categoryVBox.prefWidth = width * StyleConstants.CATEGORY_PANE_WIDTH_RATIO
            
            // Update grid columns
            val photoPaneWidth = width * StyleConstants.PHOTO_PANE_WIDTH_RATIO
            val columns = maxOf(1, (photoPaneWidth / StyleConstants.COLUMN_WIDTH_ESTIMATE).toInt())
            imageContainer.prefColumns = columns
        }

        primaryStage.scene.heightProperty().addListener { _, _, newValue ->
            val height = newValue.toDouble()
            scrollPane.prefHeight = height - 50
            categoryScrollPane.prefHeight = height - 50
        }
    }

    private fun loadPhotosFromZip(zipFile: File) {
        // Extract photos using service
        val photos = fileService.extractPhotosFromZip(zipFile)
        photoService.setPhotos(photos)

        // Clear previous UI state
        imageViews.clear()
        selectionHandler.clearSelection()

        // Create ImageViews for each photo
        for (photo in photos) {
            val imageView = ImageUtils.createImageView(photo, StyleConstants.PHOTO_GRID_WIDTH)
            
            // Setup selection handler
            imageView.setOnMouseClicked { event ->
                selectionHandler.handleImageClick(event, imageView)
            }

            // Setup drag handlers
            imageView.setOnDragDetected { event ->
                val selectedImageViews = selectionHandler.getSelectedImageViews()
                dragDropHandler.handleDragDetected(event, imageView, selectedImageViews)
            }

            imageView.setOnDragDone { event ->
                val selectedImageViews = selectionHandler.getSelectedImageViews()
                dragDropHandler.handleDragDone(event, selectedImageViews)
            }

            imageViews.add(imageView)
        }

        updateImageDisplay()
    }

    private fun addCategory() {
        val category = categoryService.createCategory()
        
        val categoryLabel = Label(category.name).apply {
            font = Font.font(14.0)
        }

        val photoContainer = VBox().apply {
            spacing = 5.0
        }

        val categoryLane = VBox(categoryLabel, photoContainer).apply {
            spacing = 10.0
            style = StyleConstants.CATEGORY_NORMAL_STYLE
            setPrefWidth(250.0)

            setOnDragOver { event ->
                dragDropHandler.handleDragOver(event)
            }

            setOnDragEntered { event ->
                if (dragDropHandler.getDraggedImageView() != null && event.dragboard.hasString()) {
                    style = StyleConstants.CATEGORY_DRAG_OVER_STYLE
                }
                event.consume()
            }

            setOnDragExited { event ->
                style = StyleConstants.CATEGORY_NORMAL_STYLE
                event.consume()
            }

            setOnDragDropped { event ->
                val success = dragDropHandler.handleDragDropped(event, category, photoContainer)
                event.isDropCompleted = success
                event.consume()
            }
        }

        categoryContainer.children.add(categoryLane)
    }

    private fun updateImageDisplay() {
        imageContainer.children.clear()
        imageContainer.children.addAll(imageViews)
        selectionHandler.updateVisualSelection()
    }
}
