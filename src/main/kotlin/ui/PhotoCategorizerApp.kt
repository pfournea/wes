package ui

import domain.model.Category
import domain.service.CategoryService
import domain.service.ExportService
import domain.service.FileService
import domain.service.PhotoService
import domain.service.SelectionService
import javafx.application.Application
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.TilePane
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Stage
import ui.component.ButtonFactory
import ui.controller.ExportController
import ui.handler.DragDropHandler
import ui.handler.ReorderDragDropHandler
import ui.handler.SelectionHandler
import util.ImageCache
import util.ImageUtils
import util.StyleConstants
import java.io.File

class PhotoCategorizerApp : Application() {
    private val photoService = PhotoService()
    private val selectionService = SelectionService()
    private val categoryService = CategoryService()
    private val fileService = FileService()
    private val exportService = ExportService()
    private val exportController = ExportController(exportService)

    private val imageViews = mutableListOf<ImageView>()
    private val categoryCardMap = mutableMapOf<String, ui.component.CategoryCard>()
    
    private var selectedCategory: Category? = null
    private val imageContainer = TilePane().apply {
        orientation = Orientation.HORIZONTAL
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

    private lateinit var selectionHandler: SelectionHandler
    private lateinit var dragDropHandler: DragDropHandler
    private lateinit var reorderDragDropHandler: ReorderDragDropHandler

    override fun start(primaryStage: Stage) {
        primaryStage.title = "Photo Categorizer"

        initializeHandlers()

        val uploadButton = ButtonFactory.createUploadButton().apply {
            setOnAction { handleUploadZip(primaryStage) }
        }
        
        val saveButton = ButtonFactory.createSaveButton().apply {
            setOnAction { exportController.handleSaveImages(primaryStage, categoryService.getCategories()) }
        }
        
        val addCategoryButton = ButtonFactory.createAddCategoryButton().apply {
            setOnAction { addCategory() }
        }

        val categoryScrollPane = ScrollPane(categoryContainer).apply {
            fitToWidthProperty().set(true)
            style = "-fx-background-color: #f5f5f5; -fx-background: #f5f5f5;"
        }

        val categoryVBox = VBox(addCategoryButton, categoryScrollPane).apply {
            spacing = 10.0
            minWidth = StyleConstants.MIN_CATEGORY_PANE_WIDTH
        }

        val scrollPane = ScrollPane(imageContainer).apply {
            fitToWidthProperty().set(true)
            minWidth = StyleConstants.MIN_PHOTO_PANE_WIDTH
        }

        val root = SplitPane(scrollPane, categoryVBox).apply {
            orientation = Orientation.HORIZONTAL
            setDividerPositions(StyleConstants.DEFAULT_DIVIDER_POSITION)
            style = "-fx-background-color: #f5f5f5;"
        }

        val controlsBox = HBox(uploadButton, saveButton).apply {
            spacing = 10.0
            style = "-fx-padding: 10;"
        }

        primaryStage.scene = Scene(VBox(controlsBox, root))
        primaryStage.isMaximized = true
        primaryStage.show()

        setupResponsiveLayout(primaryStage, scrollPane, categoryScrollPane)
    }

    private fun initializeHandlers() {
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
            updateMainGridForCategory(selectedCategory)
            selectionHandler.clearSelection()
        }

        reorderDragDropHandler = ReorderDragDropHandler(
            categoryService,
            imageViews
        ) {
            updateMainGridForCategory(selectedCategory)
        }
    }

    private fun handleUploadZip(primaryStage: Stage) {
        val fileChooser = FileChooser().apply {
            title = "Select Zip File"
            extensionFilters.add(FileChooser.ExtensionFilter("Zip Files", "*.zip"))
        }
        fileChooser.showOpenDialog(primaryStage)?.let { loadPhotosFromZip(it) }
    }

    private fun setupResponsiveLayout(primaryStage: Stage, scrollPane: ScrollPane, categoryScrollPane: ScrollPane) {
        scrollPane.widthProperty().addListener { _, _, newValue ->
            val availableWidth = newValue.toDouble() - StyleConstants.SCROLLBAR_WIDTH_ESTIMATE
            val columnWidth = StyleConstants.PHOTO_GRID_WIDTH + imageContainer.hgap
            imageContainer.prefColumns = maxOf(1, (availableWidth / columnWidth).toInt())
        }
        
        categoryScrollPane.widthProperty().addListener { _, _, newValue ->
            val availableWidth = newValue.toDouble() - StyleConstants.SCROLLBAR_WIDTH_ESTIMATE - 20
            val columnWidth = StyleConstants.CATEGORY_CARD_WIDTH + categoryContainer.hgap
            categoryContainer.prefColumns = maxOf(1, (availableWidth / columnWidth).toInt())
        }

        primaryStage.scene.heightProperty().addListener { _, _, newValue ->
            val height = newValue.toDouble()
            scrollPane.prefHeight = height - 50
            categoryScrollPane.prefHeight = height - 50
        }
    }

    private fun loadPhotosFromZip(zipFile: File) {
        val photos = fileService.extractPhotosFromZip(zipFile)
        photoService.setPhotos(photos)

        ImageCache.clear()
        imageViews.clear()
        selectionHandler.clearSelection()
        
        selectedCategory?.let { category ->
            categoryCardMap[category.id]?.setSelected(false)
        }
        selectedCategory = null

        for (photo in photos) {
            val imageView = ImageUtils.createImageView(photo, StyleConstants.PHOTO_GRID_WIDTH)
            setupImageViewHandlers(imageView)
            imageViews.add(imageView)
        }

        updateImageDisplay()
    }

    private fun setupImageViewHandlers(imageView: ImageView) {
        imageView.setOnMouseClicked { event ->
            selectionHandler.handleImageClick(event, imageView)
        }
        imageView.setOnDragDetected { event ->
            dragDropHandler.handleDragDetected(event, imageView, selectionHandler.getSelectedImageViews())
        }
        imageView.setOnDragDone { event ->
            dragDropHandler.handleDragDone(event, selectionHandler.getSelectedImageViews())
        }
    }

    private fun addCategory() {
        val category = categoryService.createCategory()
        
        lateinit var categoryCard: ui.component.CategoryCard
        
        categoryCard = ui.component.CategoryCard(
            category = category,
            onDeleteRequested = { deleteCategory(category) },
            onSelectionChanged = { isSelected -> handleCategorySelection(category, categoryCard, isSelected) }
        )

        categoryCardMap[category.id] = categoryCard
        setupCategoryCardDragHandlers(categoryCard, category)
        categoryContainer.children.add(categoryCard)
    }

    private fun setupCategoryCardDragHandlers(categoryCard: ui.component.CategoryCard, category: Category) {
        categoryCard.setOnDragOver { event -> dragDropHandler.handleDragOver(event) }

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
            val currentCategory = categoryService.getCategoryById(category.id) ?: category
            val success = dragDropHandler.handleDragDropped(event, currentCategory, categoryCard.getPhotoContainer())
            if (success) {
                categoryCard.updatePhotoCount()
                if (selectedCategory?.id == category.id) {
                    selectedCategory = categoryService.getCategoryById(category.id)
                    updateMainGridForCategory(selectedCategory)
                }
            }
            categoryCard.setDragOver(false)
            event.isDropCompleted = success
            event.consume()
        }
    }
    
    private fun handleCategorySelection(category: Category, categoryCard: ui.component.CategoryCard, isSelected: Boolean) {
        if (isSelected) {
            selectedCategory?.let { prevCategory ->
                categoryCardMap[prevCategory.id]?.setSelected(false)
            }
            selectedCategory = categoryService.getCategoryById(category.id)
            updateMainGridForCategory(selectedCategory)
        } else {
            selectedCategory = null
            updateMainGridForCategory(null)
        }
    }
    
    private fun updateMainGridForCategory(category: Category?) {
        imageViews.clear()
        
        val latestCategory = category?.let { categoryService.getCategoryById(it.id) }
        
        val photosToShow = if (latestCategory != null) {
            latestCategory.photos
        } else {
            photoService.getPhotos()
        }
        
        if (photosToShow.isEmpty() && latestCategory != null) {
            imageContainer.children.clear()
            imageContainer.children.add(Label("No photos in this category").apply {
                style = "-fx-font-size: 16; -fx-text-fill: #999999; -fx-padding: 20;"
            })
            return
        }
        
        for (photo in photosToShow) {
            val imageView = ImageUtils.createImageView(photo, StyleConstants.PHOTO_GRID_WIDTH)
            
            imageView.setOnMouseClicked { event ->
                selectionHandler.handleImageClick(event, imageView)
            }

            if (latestCategory != null) {
                setupCategoryViewDragHandlers(imageView)
            } else {
                setupImageViewHandlers(imageView)
            }

            imageViews.add(imageView)
        }
        
        if (latestCategory != null) {
            imageContainer.setOnDragOver { event ->
                reorderDragDropHandler.handleDragOver(event, imageContainer, latestCategory)
            }
            imageContainer.setOnDragDropped { event ->
                reorderDragDropHandler.handleDragDropped(event, latestCategory, imageContainer)
                event.isDropCompleted = true
                event.consume()
            }
            imageContainer.setOnDragExited { event ->
                reorderDragDropHandler.handleDragExited(event, imageContainer)
            }
        } else {
            imageContainer.onDragOver = null
            imageContainer.onDragDropped = null
            imageContainer.onDragExited = null
        }
        
        updateImageDisplay()
    }
    
    private fun setupCategoryViewDragHandlers(imageView: ImageView) {
        imageView.setOnDragDetected { event ->
            dragDropHandler.handleDragDetected(event, imageView, selectionHandler.getSelectedImageViews())
        }
        imageView.setOnDragDone { event ->
            dragDropHandler.handleDragDone(event, selectionHandler.getSelectedImageViews())
        }
    }
    
    private fun deleteCategory(category: Category) {
        if (selectedCategory?.id == category.id) {
            selectedCategory = null
        }
        
        categoryCardMap.remove(category.id)
        
        categoryContainer.children.removeIf { 
            it is ui.component.CategoryCard && it.getCategory().id == category.id
        }
        
        val currentCategory = categoryService.getCategoryById(category.id)
        if (currentCategory != null && currentCategory.photos.isNotEmpty()) {
            photoService.restorePhotos(currentCategory.photos.toList())
        }
        
        updateMainGridForCategory(selectedCategory)
    }

    private fun updateImageDisplay() {
        imageContainer.children.clear()
        imageContainer.children.addAll(imageViews)
        selectionHandler.updateVisualSelection()
    }
}
