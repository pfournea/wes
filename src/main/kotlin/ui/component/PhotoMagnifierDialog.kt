package ui.component

import domain.model.Photo
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.stage.Screen
import javafx.stage.Stage
import util.Icons
import util.StyleConstants

class PhotoMagnifierDialog(private val photo: Photo) {

    private val stage = Stage()

    fun showAndWait() {
        val screenBounds = Screen.getPrimary().visualBounds
        val maxWidth = screenBounds.width * 0.8
        val maxHeight = screenBounds.height * 0.8

        val image = Image(
            photo.path.toUri().toString(),
            maxWidth,
            maxHeight,
            true,
            false,
            false
        )

        val imageWidth = if (image.width > 0 && image.width < maxWidth) image.width else maxWidth
        val imageHeight = if (image.height > 0 && image.height < maxHeight) image.height else maxHeight

        stage.title = photo.fileName

        val imageView = ImageView(image).apply {
            isPreserveRatio = true
            isSmooth = true

            if (photo.rotationDegrees != 0) {
                rotate = photo.rotationDegrees.toDouble()
            }
        }

        val closeButton = Button(Icons.CLOSE).apply {
            style = buildCloseButtonStyle()
            setOnAction { 
                stage.close() 
            }
        }

        val imageContainer = StackPane().apply {
            children.addAll(imageView, closeButton)
            StackPane.setAlignment(closeButton, Pos.TOP_RIGHT)
            StackPane.setMargin(closeButton, Insets(8.0, 8.0, 0.0, 0.0))
            style = "-fx-background-color: #1a1a1a;"
        }

        val content = VBox().apply {
            children.add(imageContainer)
            style = "-fx-background-color: #1a1a1a;"
        }

        val scene = Scene(content)
        scene.fill = javafx.scene.paint.Color.valueOf("#1a1a1a")

        stage.scene = scene
        stage.width = imageWidth + 40
        stage.height = imageHeight + 40
        stage.isResizable = false
        stage.showAndWait()
    }

    private fun buildCloseButtonStyle(): String = """
        -fx-background-color: rgba(0, 0, 0, 0.6);
        -fx-text-fill: white;
        -fx-font-size: 18px;
        -fx-font-weight: bold;
        -fx-padding: 4 8 4 8;
        -fx-cursor: hand;
        -fx-background-radius: 4px;
    """.trimIndent()
}
