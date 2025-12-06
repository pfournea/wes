import javafx.application.Application
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.TilePane
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipFile

class Main : Application() {
    // Track selected images in order
    private val selectedImages = LinkedHashSet<ImageView>()
    private var lastSelectedIndex: Int? = null
    private var anchorIndex: Int? = null
    
    private val imageViews = mutableListOf<ImageView>()
    private val categories: ObservableList<String> = FXCollections.observableArrayList()
    private val imageContainer = TilePane().apply {
        orientation = javafx.geometry.Orientation.HORIZONTAL
        hgap = 10.0
        vgap = 10.0
        prefColumns = 3  // Initial value, will be updated dynamically
    }
    private val categoryContainer = HBox().apply { spacing = 10.0 }
    private var draggedImageView: ImageView? = null

    override fun start(primaryStage: Stage) {
        primaryStage.title = "Photo Categorizer"

        val uploadButton = Button("Upload Zip File").apply {
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

        val addCategoryButton = Button("Add Category").apply {
            setOnAction {
                addCategory()
            }
        }

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

        // Make layouts responsive to window size changes
        primaryStage.scene.widthProperty().addListener { _, _, newValue ->
            val width = newValue.toDouble()
            scrollPane.prefWidth = width * 0.6  // 60% for photos
            categoryVBox.prefWidth = width * 0.4  // 40% for categories
            // Update grid columns based on photo pane width
            val photoPaneWidth = width * 0.6
            val columnWidth = 150.0  // Approximate width per image
            val columns = maxOf(1, (photoPaneWidth / columnWidth).toInt())
            imageContainer.prefColumns = columns
        }

        primaryStage.scene.heightProperty().addListener { _, _, newValue ->
            val height = newValue.toDouble()
            scrollPane.prefHeight = height - 50  // Account for controls
            categoryScrollPane.prefHeight = height - 50
        }
    }

    private fun loadPhotosFromZip(zipFile: File) {
        val tempDir = Files.createTempDirectory("photo_categorizer")
        val imageFiles = mutableListOf<Path>()

        ZipFile(zipFile).use { zip ->
            for (entry in zip.entries()) {
                if (!entry.isDirectory && isImageFile(entry.name)) {
                    val extractedFile = tempDir.resolve(entry.name)
                    Files.createDirectories(extractedFile.parent)
                    zip.getInputStream(entry).use { input ->
                        Files.copy(input, extractedFile)
                    }
                    imageFiles.add(extractedFile)
                }
            }
        }

        // Clear previous images
        imageViews.clear()

        // Load images in order
        for ((idx, imageFile) in imageFiles.withIndex()) {
            val image = Image(imageFile.toUri().toString())
            val imageView = ImageView(image).apply {
                fitWidth = 200.0
                isPreserveRatio = true
                // Selection logic
                setOnMouseClicked { event ->
                    val ctrl = event.isControlDown
                    val shift = event.isShiftDown
                    // Get the actual current index from imageViews list
                    val index = imageViews.indexOf(this)
                    if (index < 0) return@setOnMouseClicked // Safety check
                    
                    val columns = imageContainer.prefColumns.toInt()
                    
                    if (ctrl) {
                        // Toggle selection (one by one)
                        if (selectedImages.contains(this)) {
                            selectedImages.remove(this)
                            style = ""
                        } else {
                            selectedImages.add(this)
                            style = "-fx-border-color: #0077ff; -fx-border-width: 8; -fx-effect: dropshadow(gaussian, #0077ff, 18, 0.7, 0, 0);"
                        }
                        // Update anchor to the last Ctrl+Clicked item
                        anchorIndex = index
                    } else if (shift && anchorIndex != null) {
                        // Row-wise range selection from anchor to clicked
                        selectedImages.forEach { it.style = "" }
                        selectedImages.clear()
                        
                        val anchor = anchorIndex!!
                        // Determine direction: are we going forward or backward?
                        val startIdx: Int
                        val endIdx: Int
                        val anchorRow = anchor / columns
                        val anchorCol = anchor % columns
                        val clickedRow = index / columns
                        val clickedCol = index % columns
                        
                        if (anchor <= index) {
                            // Forward selection
                            startIdx = anchor
                            endIdx = index
                        } else {
                            // Backward selection
                            startIdx = index
                            endIdx = anchor
                        }
                        
                        val startRow = startIdx / columns
                        val startCol = startIdx % columns
                        val endRow = endIdx / columns
                        val endCol = endIdx % columns
                        
                        // Select photos row-wise
                        for (row in startRow..endRow) {
                            for (col in 0 until columns) {
                                val i = row * columns + col
                                if (i >= imageViews.size) break
                                
                                // Skip photos before start column in the start row
                                if (row == startRow && col < startCol) continue
                                // Skip photos after end column in the end row
                                if (row == endRow && col > endCol) continue
                                
                                val iv = imageViews[i]
                                selectedImages.add(iv)
                                iv.style = "-fx-border-color: #0077ff; -fx-border-width: 8; -fx-effect: dropshadow(gaussian, #0077ff, 18, 0.7, 0, 0);"
                            }
                        }
                    } else {
                        // Single select (set anchor)
                        selectedImages.forEach { it.style = "" }
                        selectedImages.clear()
                        selectedImages.add(this)
                        style = "-fx-border-color: #0077ff; -fx-border-width: 8; -fx-effect: dropshadow(gaussian, #0077ff, 18, 0.7, 0, 0);"
                        anchorIndex = index
                    }
                    event.consume()
                }
                setOnDragDetected { event ->
                    // Drag all selected if this is selected, else just this
                    val dragSet = if (selectedImages.contains(this)) selectedImages else linkedSetOf(this)
                    draggedImageView = this
                    val dragboard = startDragAndDrop(javafx.scene.input.TransferMode.MOVE)
                    val content = javafx.scene.input.ClipboardContent()
                    // Transfer indices as comma-separated string
                    val indices = dragSet.map { imageViews.indexOf(it) }.joinToString(",")
                    content.putString(indices)
                    dragboard.setContent(content)
                    dragboard.setDragView(image, event.x, event.y)
                    dragSet.forEach { it.opacity = 0.5 }
                    event.consume()
                }
                setOnDragDone { event ->
                    selectedImages.forEach { it.opacity = 1.0 }
                    if (event.transferMode == javafx.scene.input.TransferMode.MOVE) {
                        draggedImageView = null
                    }
                    event.consume()
                }
            }
            imageViews.add(imageView)
        }

        // Update UI
        updateImageDisplay()
    }

    private fun isImageFile(filename: String): Boolean {
        val lower = filename.lowercase()
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") ||
               lower.endsWith(".gif") || lower.endsWith(".bmp")
    }

    private fun addCategory() {
        val nextNumber = categories.size + 1
        categories.add("Category $nextNumber")
        val categoryLabel = Label("Category $nextNumber").apply {
            font = Font.font(14.0)
        }
        val photoContainer = VBox().apply {
            spacing = 5.0
        }
        val categoryLane = VBox(categoryLabel, photoContainer).apply {
            spacing = 10.0
            style = "-fx-border-color: black; -fx-border-width: 2; -fx-padding: 5;"
            setPrefWidth(250.0)
            setOnDragOver { event ->
                if (draggedImageView != null && event.dragboard.hasString()) {
                    event.acceptTransferModes(javafx.scene.input.TransferMode.MOVE)
                }
                event.consume()
            }
            setOnDragEntered { event ->
                if (draggedImageView != null && event.dragboard.hasString()) {
                    style = "-fx-border-color: blue; -fx-border-width: 3; -fx-padding: 5; -fx-background-color: lightblue;"
                }
                event.consume()
            }
            setOnDragExited { event ->
                style = "-fx-border-color: black; -fx-border-width: 2; -fx-padding: 5;"
                event.consume()
            }
            setOnDragDropped { event ->
                val dragboard = event.dragboard
                var success = false

                if (dragboard.hasString()) {
                    val indices = dragboard.string.split(",").mapNotNull { it.toIntOrNull() }
                    val toMove = indices.mapNotNull { imageViews.getOrNull(it) }
                    // Remove from main grid and any previous category
                    toMove.forEach { iv ->
                        imageViews.remove(iv)
                        iv.parent?.let { parent ->
                            if (parent is VBox) parent.children.remove(iv)
                        }
                        iv.fitWidth = 220.0
                        iv.opacity = 1.0 // Restore normal appearance
                    }
                    // Determine drop position
                    val mouseY = event.y
                    val children = photoContainer.children
                    var insertIdx = children.size
                    for ((i, node) in children.withIndex()) {
                        if (node is ImageView && mouseY < node.layoutY + node.boundsInParent.height / 2) {
                            insertIdx = i
                            break
                        }
                    }
                    // Insert at calculated position
                    children.addAll(insertIdx, toMove)
                    // Update main display
                    updateImageDisplay()
                    success = true
                    // Clear selection and border
                    selectedImages.forEach { it.style = "" }
                    selectedImages.clear()
                }

                event.isDropCompleted = success
                event.consume()
            }
        }
        categoryContainer.children.add(categoryLane)
    }

    private fun updateImageDisplay() {
        imageContainer.children.clear()
        imageContainer.children.addAll(imageViews)
        // Update selection border for all images
        imageViews.forEach {
            if (selectedImages.contains(it)) {
                // Prominent border and shadow for selected
                it.style = "-fx-border-color: #0077ff; -fx-border-width: 8; -fx-effect: dropshadow(gaussian, #0077ff, 18, 0.7, 0, 0);"
            } else {
                it.style = ""
            }
        }
    }
}

fun main(args: Array<String>) {
    Application.launch(Main::class.java, *args)
}
