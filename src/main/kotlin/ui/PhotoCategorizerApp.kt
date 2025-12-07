package ui

import domain.model.Category
import domain.service.CategoryService
import domain.service.ExportService
import domain.service.FileService
import domain.service.PhotoService
import domain.service.SelectionService
import javafx.application.Application
import javafx.concurrent.Task
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.TilePane
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.Stage
import ui.handler.DragDropHandler
import ui.handler.SelectionHandler
import util.ImageCache
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
    private val exportService = ExportService()

    // UI Components
    private val imageViews = mutableListOf<ImageView>()
    private val categoryCardMap = mutableMapOf<String, ui.component.CategoryCard>()
    
    // Selection state
    private var selectedCategory: Category? = null
    private val imageContainer = TilePane().apply {
        orientation = javafx.geometry.Orientation.HORIZONTAL
        hgap = 10.0
        vgap = 10.0
        prefColumns = StyleConstants.DEFAULT_COLUMNS
    }
    private val categoryContainer = TilePane().apply { 
        hgap = 15.0
        vgap = 15.0
        prefColumns = 2
        style = "-fx-padding: 10;"
    }

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
            // Refresh the main grid based on current selection state
            updateMainGridForCategory(selectedCategory)
            selectionHandler.clearSelection()
        }

        // Create UI
        val saveButton = createSaveButton(primaryStage)
        val uploadButton = createUploadButton(primaryStage)
        val addCategoryButton = createAddCategoryButton()

        val categoryScrollPane = ScrollPane(categoryContainer).apply {
            fitToWidthProperty().set(true)
            style = "-fx-background-color: #f5f5f5; -fx-background: #f5f5f5;"
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

        val controlsVBox = HBox(uploadButton, saveButton).apply {
            spacing = 10.0
            style = "-fx-padding: 10;"
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
            style = """
                -fx-background-color: #4CAF50;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-padding: 10 20 10 20;
                -fx-background-radius: 5;
                -fx-cursor: hand;
            """.trimIndent()
            
            setOnMouseEntered {
                style = """
                    -fx-background-color: #45a049;
                    -fx-text-fill: white;
                    -fx-font-size: 14px;
                    -fx-font-weight: bold;
                    -fx-padding: 10 20 10 20;
                    -fx-background-radius: 5;
                    -fx-cursor: hand;
                """.trimIndent()
            }
            
            setOnMouseExited {
                style = """
                    -fx-background-color: #4CAF50;
                    -fx-text-fill: white;
                    -fx-font-size: 14px;
                    -fx-font-weight: bold;
                    -fx-padding: 10 20 10 20;
                    -fx-background-radius: 5;
                    -fx-cursor: hand;
                """.trimIndent()
            }
            
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

    private fun createSaveButton(primaryStage: Stage): Button {
        return Button("Save Images").apply {
            style = """
                -fx-background-color: #2196F3;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-padding: 10 20 10 20;
                -fx-background-radius: 5;
                -fx-cursor: hand;
            """.trimIndent()
            
            setOnMouseEntered {
                style = """
                    -fx-background-color: #1976D2;
                    -fx-text-fill: white;
                    -fx-font-size: 14px;
                    -fx-font-weight: bold;
                    -fx-padding: 10 20 10 20;
                    -fx-background-radius: 5;
                    -fx-cursor: hand;
                """.trimIndent()
            }
            
            setOnMouseExited {
                style = """
                    -fx-background-color: #2196F3;
                    -fx-text-fill: white;
                    -fx-font-size: 14px;
                    -fx-font-weight: bold;
                    -fx-padding: 10 20 10 20;
                    -fx-background-radius: 5;
                    -fx-cursor: hand;
                """.trimIndent()
            }
            
            setOnAction {
                handleSaveImages(primaryStage)
            }
        }
    }

    private fun createAddCategoryButton(): Button {
        return Button("Add Category").apply {
            style = """
                -fx-background-color: #FF9800;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-padding: 10 20 10 20;
                -fx-background-radius: 5;
                -fx-cursor: hand;
            """.trimIndent()
            
            setOnMouseEntered {
                style = """
                    -fx-background-color: #F57C00;
                    -fx-text-fill: white;
                    -fx-font-size: 14px;
                    -fx-font-weight: bold;
                    -fx-padding: 10 20 10 20;
                    -fx-background-radius: 5;
                    -fx-cursor: hand;
                """.trimIndent()
            }
            
            setOnMouseExited {
                style = """
                    -fx-background-color: #FF9800;
                    -fx-text-fill: white;
                    -fx-font-size: 14px;
                    -fx-font-weight: bold;
                    -fx-padding: 10 20 10 20;
                    -fx-background-radius: 5;
                    -fx-cursor: hand;
                """.trimIndent()
            }
            
            setOnAction {
                addCategory()
            }
        }
    }

    private fun handleSaveImages(primaryStage: Stage) {
        // Check if there are categorized photos
        val categories = categoryService.getCategories()
        val totalPhotos = categories.sumOf { it.photos.size }

        if (totalPhotos == 0) {
            showAlert(
                Alert.AlertType.WARNING,
                "No Photos to Save",
                "Please add photos to categories first."
            )
            return
        }

        // Show directory chooser
        val dirChooser = DirectoryChooser().apply {
            title = "Select Directory to Save Categorized Photos"
        }
        val directory = dirChooser.showDialog(primaryStage) ?: return

        // Check if directory has existing files
        val existingFiles = exportService.countFilesInDirectory(directory.toPath())
        if (existingFiles > 0) {
            val confirm = showConfirmDialog(
                "Directory Warning",
                "Directory contains $existingFiles files. All files will be deleted and replaced. Continue?"
            )
            if (!confirm) return
        }

        // Show progress dialog
        val progressDialog = Dialog<Void>()
        progressDialog.title = "Exporting Photos"
        progressDialog.headerText = "Copying photos to ${directory.name}..."

        val progressBar = ProgressBar()
        progressBar.prefWidth = 300.0

        val progressLabel = Label("Preparing...")
        
        val vbox = VBox(10.0, progressLabel, progressBar)
        vbox.style = "-fx-padding: 20;"
        progressDialog.dialogPane.content = vbox
        progressDialog.dialogPane.buttonTypes.add(ButtonType.CANCEL)

        // Create background task for export
        val exportTask = object : Task<domain.service.ExportResult>() {
            override fun call(): domain.service.ExportResult {
                val totalPhotoCount = categories.sumOf { it.photos.size }

                updateMessage("Deleting existing files...")
                updateProgress(0.0, 1.0)

                // Perform export
                val result = exportService.exportCategories(categories, directory.toPath())

                // Update progress to complete
                updateProgress(1.0, 1.0)
                updateMessage("Complete!")

                return result
            }
        }

        // Bind progress
        progressBar.progressProperty().bind(exportTask.progressProperty())
        progressLabel.textProperty().bind(exportTask.messageProperty())

        // Handle completion
        exportTask.setOnSucceeded {
            progressDialog.close()
            val result = exportTask.value
            
            if (result.success) {
                showAlert(
                    Alert.AlertType.INFORMATION,
                    "Export Successful",
                    "${result.photosCopied} photos saved to ${directory.absolutePath}\n" +
                    "${result.filesDeleted} old files were deleted."
                )
            } else {
                showAlert(
                    Alert.AlertType.ERROR,
                    "Export Failed",
                    "Some errors occurred:\n${result.errors.joinToString("\n")}"
                )
            }
        }

        exportTask.setOnFailed {
            progressDialog.close()
            showAlert(
                Alert.AlertType.ERROR,
                "Export Failed",
                "An error occurred during export: ${exportTask.exception?.message}"
            )
        }

        exportTask.setOnCancelled {
            progressDialog.close()
            showAlert(
                Alert.AlertType.INFORMATION,
                "Export Cancelled",
                "Export operation was cancelled by user."
            )
        }

        // Start task in background thread
        val thread = Thread(exportTask)
        thread.isDaemon = true
        thread.start()

        // Show dialog
        progressDialog.showAndWait()
    }

    private fun showAlert(type: Alert.AlertType, title: String, content: String) {
        val alert = Alert(type)
        alert.title = title
        alert.headerText = null
        alert.contentText = content
        alert.showAndWait()
    }

    private fun showConfirmDialog(title: String, content: String): Boolean {
        val alert = Alert(Alert.AlertType.CONFIRMATION)
        alert.title = title
        alert.headerText = null
        alert.contentText = content
        
        val result = alert.showAndWait()
        return result.isPresent && result.get() == ButtonType.OK
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
            
            // Update photo grid columns
            val photoPaneWidth = width * StyleConstants.PHOTO_PANE_WIDTH_RATIO
            val columns = maxOf(1, (photoPaneWidth / StyleConstants.COLUMN_WIDTH_ESTIMATE).toInt())
            imageContainer.prefColumns = columns
            
            // Update category grid columns
            val categoryPaneWidth = width * StyleConstants.CATEGORY_PANE_WIDTH_RATIO
            val categoryColumns = maxOf(1, (categoryPaneWidth / (StyleConstants.CATEGORY_CARD_WIDTH + 15)).toInt())
            categoryContainer.prefColumns = categoryColumns
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
        ImageCache.clear()  // Clear cache when loading new photo set
        imageViews.clear()
        selectionHandler.clearSelection()
        
        // Reset category selection
        selectedCategory?.let { category ->
            categoryCardMap[category.id]?.setSelected(false)
        }
        selectedCategory = null

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
        
        // Create card reference first
        lateinit var categoryCard: ui.component.CategoryCard
        
        categoryCard = ui.component.CategoryCard(
            category = category,
            onDeleteRequested = {
                deleteCategory(category)
            },
            onSelectionChanged = { isSelected ->
                handleCategorySelection(category, categoryCard, isSelected)
            }
        )

        // Store category-card mapping
        categoryCardMap[category.id] = categoryCard

        val photoContainer = categoryCard.getPhotoContainer()

        // Setup drag-and-drop handlers
        categoryCard.setOnDragOver { event ->
            dragDropHandler.handleDragOver(event)
        }

        categoryCard.setOnDragEntered { event ->
            if (dragDropHandler.getDraggedImageView() != null && event.dragboard.hasString()) {
                categoryCard.setDragOver(true)
            }
            event.consume()
        }

        categoryCard.setOnDragExited { event ->
            categoryCard.setDragOver(false)
            event.consume()
        }

        categoryCard.setOnDragDropped { event ->
            val success = dragDropHandler.handleDragDropped(event, category, photoContainer)
            if (success) {
                categoryCard.updatePhotoCount()
                // If this category is selected, refresh the main grid
                if (selectedCategory == category) {
                    updateMainGridForCategory(category)
                }
            }
            categoryCard.setDragOver(false)
            event.isDropCompleted = success
            event.consume()
        }

        categoryContainer.children.add(categoryCard)
    }
    
    private fun handleCategorySelection(category: Category, categoryCard: ui.component.CategoryCard, isSelected: Boolean) {
        if (isSelected) {
            // Deselect previous category if any
            selectedCategory?.let { prevCategory ->
                categoryCardMap[prevCategory.id]?.setSelected(false)
            }
            selectedCategory = category
            updateMainGridForCategory(category)
        } else {
            selectedCategory = null
            updateMainGridForCategory(null) // Show uncategorized photos
        }
    }
    
    private fun updateMainGridForCategory(category: Category?) {
        imageViews.clear()
        
        val photosToShow = if (category != null) {
            // Show ONLY photos from selected category
            category.photos
        } else {
            // Show ONLY uncategorized photos (from PhotoService)
            photoService.getPhotos()
        }
        
        // If no photos to show, display a message
        if (photosToShow.isEmpty() && category != null) {
            imageContainer.children.clear()
            val emptyLabel = Label("No photos in this category").apply {
                style = "-fx-font-size: 16; -fx-text-fill: #999999; -fx-padding: 20;"
            }
            imageContainer.children.add(emptyLabel)
            return
        }
        
        // Create ImageViews for filtered photos
        for (photo in photosToShow) {
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
    
    private fun deleteCategory(category: Category) {
        // Clear selection if this category was selected
        if (selectedCategory == category) {
            selectedCategory = null
        }
        
        // Remove from mapping
        categoryCardMap.remove(category.id)
        
        // Remove from UI
        val cardToRemove = categoryContainer.children.find { 
            it is ui.component.CategoryCard && it.getCategory() == category
        }
        if (cardToRemove != null) {
            categoryContainer.children.remove(cardToRemove)
        }
        
        // Remove photos back to main grid
        if (category.photos.isNotEmpty()) {
            val photosToRestore = category.photos.toList()
            photosToRestore.forEach { photo ->
                photoService.setPhotos(photoService.getPhotos() + photo)
            }
        }
        
        // Update main grid - will show uncategorized photos if nothing selected
        updateMainGridForCategory(selectedCategory)
    }
    
    private fun loadPhotosIntoGrid() {
        imageViews.clear()
        
        for (photo in photoService.getPhotos()) {
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

    private fun updateImageDisplay() {
        imageContainer.children.clear()
        imageContainer.children.addAll(imageViews)
        selectionHandler.updateVisualSelection()
    }
}
