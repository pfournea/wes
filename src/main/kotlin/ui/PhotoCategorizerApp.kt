package ui

import domain.service.CategoryService
import domain.service.ExportService
import domain.service.FileService
import domain.service.PhotoService
import domain.service.SelectionService
import javafx.application.Application
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

        val root = javafx.scene.control.SplitPane(scrollPane, categoryVBox).apply {
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
            sharedImageViews
        ) {
            photoGridController.updateForCategory(categoryController.getSelectedCategory())
            selectionHandler.clearSelection()
        }

        reorderDragDropHandler = ReorderDragDropHandler(
            categoryService,
            sharedImageViews
        ) {
            photoGridController.updateForCategory(categoryController.getSelectedCategory())
        }

        photoGridController = PhotoGridController(
            photoService,
            categoryService,
            imageContainer,
            sharedImageViews,
            dragDropHandler,
            reorderDragDropHandler,
            selectionHandler
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
            photoService
        ) { _, imageViews ->
            categoryController.clearSelection()
            selectionHandler.clearSelection()

            sharedImageViews.clear()
            for (iv in imageViews) {
                photoGridController.setupImageViewHandlers(iv)
                sharedImageViews.add(iv)
            }
            photoGridController.updateImageDisplay()
        }

        exportController = ExportController(exportService)
        layoutController = LayoutController(imageContainer, categoryContainer)
    }
}
