package ui.component

import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import util.StyleConstants
import java.io.File

/**
 * Dialog shown after a successful export operation.
 * Displays a success message, the export location, and the number of photos exported.
 */
class ExportSuccessDialog(
    private val directory: File,
    private val photoCount: Int
) : Dialog<Void?>() {

    init {
        title = "Export Geslaagd"
        headerText = "Export succesvol afgerond!"

        dialogPane.content = createContent()
        dialogPane.buttonTypes.add(ButtonType.OK)

        dialogPane.style = """
            -fx-background-color: ${StyleConstants.BACKGROUND_PRIMARY};
            -fx-font-family: ${StyleConstants.FONT_FAMILY};
        """.trimIndent()

        val okButton = dialogPane.lookupButton(ButtonType.OK) as Button
        okButton.text = "Sluiten"
        okButton.style = buildPrimaryButtonStyle()

        dialogPane.prefWidth = 450.0
        dialogPane.prefHeight = 200.0

        setResultConverter { null }
    }

    private fun createContent(): VBox {
        return VBox(StyleConstants.SPACING_BASE).apply {
            padding = Insets(StyleConstants.SPACING_XL)
            style = "-fx-background-color: ${StyleConstants.BACKGROUND_PRIMARY};"

            val countLabel = Label("Aantal foto's: $photoCount")
            countLabel.style = """
                -fx-font-size: ${StyleConstants.FONT_SIZE_BASE}px;
                -fx-text-fill: ${StyleConstants.TEXT_PRIMARY};
            """.trimIndent()

            val locationHeaderLabel = Label("Locatie:")
            locationHeaderLabel.style = """
                -fx-font-size: ${StyleConstants.FONT_SIZE_BASE}px;
                -fx-font-weight: 600;
                -fx-text-fill: ${StyleConstants.TEXT_SECONDARY};
            """.trimIndent()

            val locationLabel = Label(directory.absolutePath)
            locationLabel.style = """
                -fx-font-size: ${StyleConstants.FONT_SIZE_BASE}px;
                -fx-text-fill: ${StyleConstants.TEXT_PRIMARY};
            """.trimIndent()
            locationLabel.isWrapText = true
            locationLabel.maxWidth = 400.0

            children.addAll(
                countLabel,
                locationHeaderLabel,
                locationLabel
            )
        }
    }

    private fun buildPrimaryButtonStyle(): String = """
        -fx-background-color: ${StyleConstants.PRIMARY_500};
        -fx-text-fill: white;
        -fx-font-size: ${StyleConstants.FONT_SIZE_BASE}px;
        -fx-font-weight: 600;
        -fx-padding: 10 24 10 24;
        -fx-background-radius: ${StyleConstants.RADIUS_BASE};
        -fx-cursor: hand;
    """.trimIndent()
}
