package ui.controller

import javafx.scene.control.ScrollPane
import javafx.scene.layout.TilePane
import javafx.stage.Stage
import util.StyleConstants

class LayoutController(
    private val imageContainer: TilePane,
    private val categoryContainer: TilePane
) {
    fun setupResponsiveLayout(primaryStage: Stage, scrollPane: ScrollPane, categoryScrollPane: ScrollPane) {
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
}
