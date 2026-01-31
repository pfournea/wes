package ui.handler

import domain.model.Photo
import domain.service.CategoryService
import domain.service.PhotoService
import domain.service.SelectionService
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.layout.TilePane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils
import ui.component.CategoryCard
import util.StyleConstants
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

abstract class DragDropTestBase : ApplicationTest() {

    protected lateinit var photoService: PhotoService
    protected lateinit var categoryService: CategoryService
    protected lateinit var selectionService: SelectionService

    protected lateinit var imageContainer: TilePane
    protected lateinit var categoryContainer: TilePane
    protected lateinit var sharedImageViews: MutableList<ImageView>

    protected lateinit var dragDropHandler: DragDropHandler
    protected lateinit var selectionHandler: SelectionHandler

    override fun start(stage: Stage) {
        photoService = PhotoService()
        categoryService = CategoryService()
        selectionService = SelectionService()

        imageContainer = TilePane().apply {
            orientation = Orientation.HORIZONTAL
            hgap = 10.0
            vgap = 10.0
            prefColumns = 3
            id = "image-container"
        }

        categoryContainer = TilePane().apply {
            hgap = 15.0
            vgap = 15.0
            prefColumns = 2
            id = "category-container"
        }

        sharedImageViews = mutableListOf()

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
                updateImageDisplay()
            }
        )

        val root = VBox(10.0, imageContainer, categoryContainer)
        stage.scene = Scene(root, 800.0, 600.0)
        stage.show()
    }

    protected fun createTestPhoto(index: Int): Photo {
        return Photo.fromPath(Paths.get("/test/photo$index.jpg"), index)
    }

    protected fun addPhotoToGrid(photo: Photo): ImageView {
        val currentPhotos = photoService.getPhotos().toMutableList()
        currentPhotos.add(photo)
        photoService.setPhotos(currentPhotos)

        val imageView = createTestImageView(photo)
        setupImageViewHandlers(imageView)
        sharedImageViews.add(imageView)
        
        interact {
            imageContainer.children.add(imageView)
        }

        return imageView
    }

    protected fun createCategory(): CategoryCard {
        val category = categoryService.createCategory()

        val categoryCard = CategoryCard(
            category = category,
            onDeleteRequested = {},
            onSelectionChanged = {}
        )

        categoryCard.id = "category-card-${category.id}"
        setupCategoryCardDragHandlers(categoryCard, category.id)
        
        interact {
            categoryContainer.children.add(categoryCard)
        }

        return categoryCard
    }

    private fun createTestImageView(photo: Photo): ImageView {
        val image = createPlaceholderImage()

        return ImageView(image).apply {
            fitWidth = StyleConstants.PHOTO_GRID_WIDTH
            fitHeight = StyleConstants.PHOTO_GRID_WIDTH
            isPreserveRatio = true
            userData = photo.id
            id = "photo-${photo.id}"
        }
    }

    private fun createPlaceholderImage(): Image {
        val width = 10
        val height = 10
        val image = javafx.scene.image.WritableImage(width, height)
        val writer = image.pixelWriter
        for (y in 0 until height) {
            for (x in 0 until width) {
                writer.setColor(x, y, javafx.scene.paint.Color.LIGHTGRAY)
            }
        }
        return image
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

    private fun setupCategoryCardDragHandlers(categoryCard: CategoryCard, categoryId: String) {
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
            val category = categoryService.getCategoryById(categoryId) ?: return@setOnDragDropped
            val success = dragDropHandler.handleDragDropped(event, category, categoryCard.getPhotoContainer())
            if (success) {
                val updatedCategory = categoryService.getCategoryById(categoryId)
                if (updatedCategory != null) {
                    categoryCard.updateCategory(updatedCategory)
                }
            }
            categoryCard.setDragOver(false)
            event.isDropCompleted = success
            event.consume()
        }
    }

    protected fun updateImageDisplay() {
        interact {
            imageContainer.children.clear()
            imageContainer.children.addAll(sharedImageViews)
        }
    }

    protected fun getPhotoId(imageView: ImageView): String? {
        return imageView.userData as? String
    }

    protected fun performDragDrop(source: Node, target: Node) {
        drag(source, MouseButton.PRIMARY)
        WaitForAsyncUtils.waitForFxEvents()
        dropTo(target)
        WaitForAsyncUtils.waitForFxEvents()
        WaitForAsyncUtils.sleep(100, TimeUnit.MILLISECONDS)
    }

    protected fun waitForFxEvents() {
        WaitForAsyncUtils.waitForFxEvents()
    }

    protected fun selectPhotos(vararg photoIds: String) {
        photoIds.forEach { photoId ->
            selectionService.addSelection(photoId)
        }
        interact {
            selectionHandler.updateVisualSelection()
        }
        WaitForAsyncUtils.waitForFxEvents()
        WaitForAsyncUtils.sleep(50, TimeUnit.MILLISECONDS)
    }
}
