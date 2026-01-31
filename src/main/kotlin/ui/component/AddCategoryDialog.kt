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
        headerText = "Create multiple categories"

        dialogPane.content = createContent()
        dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)

        dialogPane.style = """
            -fx-background-color: ${StyleConstants.BACKGROUND_LIGHT};
            -fx-font-family: 'Segoe UI', 'Roboto', sans-serif;
        """.trimIndent()

        val okButton = dialogPane.lookupButton(ButtonType.OK) as Button
        okButton.style = buildButtonStyle(
            StyleConstants.PRIMARY_GRADIENT_START,
            StyleConstants.PRIMARY_GRADIENT_END
        )
        okButton.text = "Create Categories"

        val cancelButton = dialogPane.lookupButton(ButtonType.CANCEL) as Button
        cancelButton.style = buildButtonStyle("#6c757d", "#495057")

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
            hgap = 16.0
            vgap = 16.0
            padding = Insets(24.0)
            alignment = Pos.CENTER

            val startLabel = Label("Start Category Number:").apply {
                style = labelStyle()
            }
            add(startLabel, 0, 0)
            add(startNumberSpinner, 1, 0)

            val amountLabel = Label("Amount of Categories:").apply {
                style = labelStyle()
            }
            add(amountLabel, 0, 1)
            add(amountSpinner, 1, 1)

            val previewLabel = Label().apply {
                style = """
                    -fx-font-size: 12px;
                    -fx-text-fill: #666666;
                    -fx-font-style: italic;
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
                previewLabel.text = "Categories to create: $preview"
            }

            startNumberSpinner.valueProperty().addListener { _, _, _ -> updatePreview() }
            amountSpinner.valueProperty().addListener { _, _, _ -> updatePreview() }
            updatePreview()
        }
    }

    private fun labelStyle(): String = """
        -fx-font-size: 14px;
        -fx-font-weight: bold;
        -fx-text-fill: #333333;
    """.trimIndent()

    private fun buildSpinnerStyle(): String = """
        -fx-background-color: white;
        -fx-background-radius: 8;
        -fx-border-color: #e0e0e0;
        -fx-border-radius: 8;
        -fx-font-size: 14px;
    """.trimIndent()

    private fun buildButtonStyle(gradientStart: String, gradientEnd: String): String = """
        -fx-background-color: linear-gradient(to right, $gradientStart, $gradientEnd);
        -fx-text-fill: white;
        -fx-font-size: 14px;
        -fx-font-weight: bold;
        -fx-padding: 10 20 10 20;
        -fx-background-radius: 8;
        -fx-cursor: hand;
        -fx-effect: ${StyleConstants.ELEVATION_1};
    """.trimIndent()
}
