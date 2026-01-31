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
import javafx.stage.Stage
import ui.component.ButtonFactory
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
        hgap = 16.0
        vgap = 16.0
        prefColumns = StyleConstants.DEFAULT_COLUMNS
        padding = Insets(20.0)
    }
    private val categoryContainer = TilePane().apply {
        hgap = 20.0
        vgap = 20.0
        prefColumns = 2
        padding = Insets(20.0)
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

        val addCategoryButton = ButtonFactory.createAddCategoryButton().apply {
            setOnAction { categoryController.addCategory() }
        }

        // Modern category scroll pane with gradient background
        val categoryScrollPane = ScrollPane(categoryContainer).apply {
            fitToWidthProperty().set(true)
            style = """
                -fx-background: linear-gradient(to bottom, #f8f9fa, #e9ecef);
                -fx-background-color: linear-gradient(to bottom, #f8f9fa, #e9ecef);
            """.trimIndent()
        }

        val categoryVBox = VBox(addCategoryButton, categoryScrollPane).apply {
            spacing = 16.0
            minWidth = StyleConstants.MIN_CATEGORY_PANE_WIDTH
            padding = Insets(16.0)
            style = "-fx-background-color: linear-gradient(to bottom, #f8f9fa, #e9ecef);"
        }

        // Modern photo grid scroll pane
        val scrollPane = ScrollPane(imageContainer).apply {
            fitToWidthProperty().set(true)
            minWidth = StyleConstants.MIN_PHOTO_PANE_WIDTH
            style = """
                -fx-background: ${StyleConstants.BACKGROUND_LIGHT};
                -fx-background-color: ${StyleConstants.BACKGROUND_LIGHT};
            """.trimIndent()
        }

        // Modern split pane with subtle divider
        val root = javafx.scene.control.SplitPane(scrollPane, categoryVBox).apply {
            orientation = Orientation.HORIZONTAL
            setDividerPositions(StyleConstants.DEFAULT_DIVIDER_POSITION)
            style = """
                -fx-background-color: ${StyleConstants.BACKGROUND_LIGHT};
                -fx-padding: 0;
            """.trimIndent()
        }

        // Modern control panel with gradient background
        val controlsBox = HBox(uploadButton, saveButton).apply {
            spacing = 16.0
            padding = Insets(20.0)
            style = """
                -fx-background-color: linear-gradient(to right, 
                    ${StyleConstants.PRIMARY_GRADIENT_START}, 
                    ${StyleConstants.PRIMARY_GRADIENT_END});
                -fx-effect: ${StyleConstants.ELEVATION_2};
            """.trimIndent()
        }

        val scene = Scene(VBox(controlsBox, root))
        primaryStage.scene = scene
        primaryStage.isMaximized = true
        primaryStage.show()

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
