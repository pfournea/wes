package ui.component

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.scene.control.Label
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.scene.layout.GridPane
import util.StyleConstants

/**
 * Professional dialog for adding categories.
 */
class AddCategoryDialog : Dialog<AddCategoryDialog.Result>() {

    data class Result(
        val startNumber: Int,
        val amount: Int
    )

    private val startNumberSpinner = Spinner<Int>().apply {
        valueFactory = SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, 1)
        isEditable = true
        prefWidth = 120.0
        style = buildSpinnerStyle()
    }

    private val amountSpinner = Spinner<Int>().apply {
        valueFactory = SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1)
        isEditable = true
        prefWidth = 120.0
        style = buildSpinnerStyle()
    }

    init {
        title = "Add Categories"
        headerText = "Create new categories"

        dialogPane.content = createContent()
        dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)

        dialogPane.style = """
            -fx-background-color: ${StyleConstants.BACKGROUND_PRIMARY};
            -fx-font-family: ${StyleConstants.FONT_FAMILY};
        """.trimIndent()

        val okButton = dialogPane.lookupButton(ButtonType.OK) as Button
        okButton.style = buildPrimaryButtonStyle()
        okButton.text = "Create"

        val cancelButton = dialogPane.lookupButton(ButtonType.CANCEL) as Button
        cancelButton.style = buildSecondaryButtonStyle()

        setResultConverter { buttonType ->
            if (buttonType == ButtonType.OK) {
                Result(
                    startNumber = startNumberSpinner.value,
                    amount = amountSpinner.value
                )
            } else {
                null
            }
        }
    }

    fun setSuggestedStartNumber(number: Int) {
        (startNumberSpinner.valueFactory as SpinnerValueFactory.IntegerSpinnerValueFactory).value = number
    }

    private fun createContent(): GridPane {
        return GridPane().apply {
            hgap = StyleConstants.SPACING_BASE
            vgap = StyleConstants.SPACING_BASE
            padding = Insets(StyleConstants.SPACING_XL)
            alignment = Pos.CENTER

            val startLabel = Label("Start number:").apply {
                style = labelStyle()
            }
            add(startLabel, 0, 0)
            add(startNumberSpinner, 1, 0)

            val amountLabel = Label("Number of categories:").apply {
                style = labelStyle()
            }
            add(amountLabel, 0, 1)
            add(amountSpinner, 1, 1)

            val previewLabel = Label().apply {
                style = """
                    -fx-font-size: ${StyleConstants.FONT_SIZE_SM}px;
                    -fx-text-fill: ${StyleConstants.TEXT_SECONDARY};
                """.trimIndent()
            }
            add(previewLabel, 0, 2, 2, 1)

            val updatePreview = {
                val start = startNumberSpinner.value
                val amount = amountSpinner.value
                val end = start + amount - 1
                val preview = if (amount <= 5) {
                    (start until start + amount).joinToString(", ")
                } else {
                    "${start}, ${start + 1}, ${start + 2}, ..., $end"
                }
                previewLabel.text = "Will create: $preview"
            }

            startNumberSpinner.valueProperty().addListener { _, _, _ -> updatePreview() }
            amountSpinner.valueProperty().addListener { _, _, _ -> updatePreview() }
            updatePreview()
        }
    }

    private fun labelStyle(): String = """
        -fx-font-size: ${StyleConstants.FONT_SIZE_BASE}px;
        -fx-font-weight: 500;
        -fx-text-fill: ${StyleConstants.TEXT_PRIMARY};
    """.trimIndent()

    private fun buildSpinnerStyle(): String = """
        -fx-background-color: white;
        -fx-background-radius: ${StyleConstants.RADIUS_MD};
        -fx-border-color: ${StyleConstants.BORDER_DEFAULT};
        -fx-border-radius: ${StyleConstants.RADIUS_MD};
        -fx-font-size: ${StyleConstants.FONT_SIZE_BASE}px;
    """.trimIndent()

    private fun buildPrimaryButtonStyle(): String = """
        -fx-background-color: ${StyleConstants.PRIMARY_500};
        -fx-text-fill: white;
        -fx-font-size: ${StyleConstants.FONT_SIZE_BASE}px;
        -fx-font-weight: 600;
        -fx-padding: 10 20 10 20;
        -fx-background-radius: ${StyleConstants.RADIUS_BASE};
        -fx-cursor: hand;
    """.trimIndent()

    private fun buildSecondaryButtonStyle(): String = """
        -fx-background-color: transparent;
        -fx-text-fill: ${StyleConstants.TEXT_PRIMARY};
        -fx-font-size: ${StyleConstants.FONT_SIZE_BASE}px;
        -fx-font-weight: 500;
        -fx-padding: 10 20 10 20;
        -fx-background-radius: ${StyleConstants.RADIUS_BASE};
        -fx-border-color: ${StyleConstants.BORDER_DEFAULT};
        -fx-border-radius: ${StyleConstants.RADIUS_BASE};
        -fx-cursor: hand;
    """.trimIndent()
}
