package ui

import domain.service.CategoryService
import domain.service.ExportService
import domain.service.FileService
import domain.service.PhotoService
import domain.service.SelectionService
import javafx.application.Application
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.control.ScrollPane
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.TilePane
import javafx.scene.layout.VBox
import javafx.stage.Screen
import javafx.stage.Stage
import ui.component.ButtonFactory
import ui.component.HelpDialog
import ui.controller.CategoryController
import ui.controller.ExportController
import ui.controller.LayoutController
import ui.controller.PhotoGridController
import ui.controller.UploadController
import ui.handler.DragDropHandler
import ui.handler.ReorderDragDropHandler
import ui.handler.SelectionHandler
import util.StyleConstants

class PhotoCategorizerApp : Application() {
    private val photoService = PhotoService()
    private val selectionService = SelectionService()
    private val categoryService = CategoryService()
    private val fileService = FileService()
    private val exportService = ExportService()

    private val imageContainer = TilePane().apply {
        orientation = Orientation.HORIZONTAL
        hgap = StyleConstants.GRID_GAP
        vgap = StyleConstants.GRID_GAP
        prefColumns = StyleConstants.DEFAULT_COLUMNS
        padding = Insets(StyleConstants.SPACING_XL)
    }
    private val categoryContainer = TilePane().apply {
        hgap = StyleConstants.SPACING_BASE
        vgap = StyleConstants.SPACING_BASE
        prefColumns = 1
        padding = Insets(StyleConstants.SPACING_XL)
    }

    private val sharedImageViews = mutableListOf<ImageView>()

    private lateinit var selectionHandler: SelectionHandler
    private lateinit var dragDropHandler: DragDropHandler
    private lateinit var reorderDragDropHandler: ReorderDragDropHandler

    private lateinit var categoryController: CategoryController
    private lateinit var photoGridController: PhotoGridController
    private lateinit var uploadController: UploadController
    private lateinit var exportController: ExportController
    private lateinit var layoutController: LayoutController

    override fun start(primaryStage: Stage) {
        primaryStage.title = "Photo Categorizer"

        initializeComponents()

        val uploadButton = ButtonFactory.createUploadButton().apply {
            setOnAction { uploadController.handleUploadZip(primaryStage) }
        }

        val saveButton = ButtonFactory.createSaveButton().apply {
            setOnAction { exportController.handleSaveImages(primaryStage, categoryService.getCategories()) }
        }

        val helpButton = ButtonFactory.createHelpButton().apply {
            setOnAction { HelpDialog().showAndWait() }
        }

        val addCategoryButton = ButtonFactory.createAddCategoryButton().apply {
            setOnAction { categoryController.addCategory() }
        }

        // Modern category scroll pane
        val categoryScrollPane = ScrollPane(categoryContainer).apply {
            fitToWidthProperty().set(true)
            style = """
                -fx-background: ${StyleConstants.BACKGROUND_SECONDARY};
                -fx-background-color: ${StyleConstants.BACKGROUND_SECONDARY};
            """.trimIndent()
        }

        val categoryVBox = VBox(addCategoryButton, categoryScrollPane).apply {
            spacing = StyleConstants.SPACING_BASE
            minWidth = StyleConstants.MIN_CATEGORY_PANE_WIDTH
            padding = Insets(StyleConstants.SPACING_BASE)
            style = "-fx-background-color: ${StyleConstants.BACKGROUND_SECONDARY};"
        }

        // Modern photo grid scroll pane
        val scrollPane = ScrollPane(imageContainer).apply {
            fitToWidthProperty().set(true)
            minWidth = StyleConstants.MIN_PHOTO_PANE_WIDTH
            style = """
                -fx-background: ${StyleConstants.BACKGROUND_PRIMARY};
                -fx-background-color: ${StyleConstants.BACKGROUND_PRIMARY};
            """.trimIndent()
        }

        // Modern split pane
        val root = javafx.scene.control.SplitPane(scrollPane, categoryVBox).apply {
            orientation = Orientation.HORIZONTAL
            setDividerPositions(StyleConstants.DEFAULT_DIVIDER_POSITION)
            style = """
                -fx-background-color: ${StyleConstants.BACKGROUND_PRIMARY};
                -fx-padding: 0;
            """.trimIndent()
        }

        val controlsBox = HBox(uploadButton, saveButton, helpButton).apply {
            spacing = StyleConstants.SPACING_MD
            padding = Insets(StyleConstants.SPACING_BASE, StyleConstants.SPACING_XL, StyleConstants.SPACING_BASE, StyleConstants.SPACING_XL)
            style = """
                -fx-background-color: ${StyleConstants.BACKGROUND_PRIMARY};
                -fx-border-color: ${StyleConstants.BORDER_DEFAULT};
                -fx-border-width: 0 0 1 0;
            """.trimIndent()
        }

        val scene = Scene(VBox(controlsBox, root))
        primaryStage.scene = scene

        // Set minimum window size to ensure usability
        primaryStage.minWidth = StyleConstants.MIN_PHOTO_PANE_WIDTH + StyleConstants.MIN_CATEGORY_PANE_WIDTH
        primaryStage.minHeight = 400.0

        // Set default size based on screen dimensions (fallback if maximize fails)
        val screenBounds = Screen.getPrimary().visualBounds
        primaryStage.width = screenBounds.width * 0.9
        primaryStage.height = screenBounds.height * 0.9

        primaryStage.show()

        // Maximize after showing (more reliable across platforms)
        primaryStage.isMaximized = true

        layoutController.setupResponsiveLayout(primaryStage, scrollPane, categoryScrollPane)
    }

    override fun stop() {
        fileService.cleanupTempDirectories()
    }

    private fun initializeComponents() {
        selectionHandler = SelectionHandler(
            selectionService,
            photoService,
            sharedImageViews
        ) { imageContainer.prefColumns.toInt() }

        dragDropHandler = DragDropHandler(
            photoService,
            categoryService,
            sharedImageViews,
            onPhotosDropped = {
                photoGridController.updateForCategory(categoryController.getSelectedCategory())
                selectionHandler.clearSelection()
            },
            onSourceCategoryUpdated = { categoryId ->
                categoryController.updateCategoryCard(categoryId)
            }
        )

        reorderDragDropHandler = ReorderDragDropHandler(
            categoryService,
            sharedImageViews
        ) {
            val selectedCategory = categoryController.getSelectedCategory()
            photoGridController.updateForCategory(selectedCategory)
            selectedCategory?.let { categoryController.updateCategoryCard(it.id) }
        }

        photoGridController = PhotoGridController(
            photoService,
            categoryService,
            imageContainer,
            sharedImageViews,
            dragDropHandler,
            reorderDragDropHandler,
            selectionHandler,
            onPhotoRemoved = { updatedCategory ->
                categoryController.updateCategoryCard(updatedCategory.id)
                photoGridController.updateForCategory(categoryController.getSelectedCategory())
            }
        )

        categoryController = CategoryController(
            categoryService,
            photoService,
            categoryContainer,
            dragDropHandler,
            onCategorySelected = { category ->
                photoGridController.updateForCategory(category)
            },
            onCategoryDeleted = { _ ->
                photoGridController.updateForCategory(categoryController.getSelectedCategory())
            }
        )

        uploadController = UploadController(
            fileService,
            photoService,
            onResetState = {
                categoryController.clearAllCategories()
                selectionHandler.clearSelection()
                sharedImageViews.clear()
                imageContainer.children.clear()
            }
        ) { _, _ ->
            photoGridController.updateForCategory(null)
        }

        exportController = ExportController(exportService)
        layoutController = LayoutController(imageContainer, categoryContainer)
    }
}
