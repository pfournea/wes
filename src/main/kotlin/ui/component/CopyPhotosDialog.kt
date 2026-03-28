package ui.component

import domain.model.Category
import domain.service.CopyResult
import domain.service.CopyService
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.CheckBox
import javafx.scene.control.Dialog
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import util.Icons
import util.StyleConstants
import java.util.Optional

class CopyPhotosDialog(
    private val sourceCategory: Category,
    private val allCategories: List<Category>,
    private val copyService: CopyService,
    private val onCategoriesCreated: (List<Category>) -> Unit,
    private val getFreshCategories: () -> List<Category>
) : Dialog<CopyResult>() {

    private val categoryCheckBoxes = mutableMapOf<Int, CheckBox>()
    private val rangeInputField = TextField().apply {
        promptText = "e.g., 10-16 or 1,3,5 or 2-4,8,10-12"
        prefWidth = 280.0
        style = buildTextFieldStyle()
    }

    private val previewLabel = Label().apply {
        style = """
            -fx-font-size: ${StyleConstants.FONT_SIZE_SM}px;
            -fx-text-fill: ${StyleConstants.TEXT_SECONDARY};
            -fx-padding: ${StyleConstants.SPACING_SM} 0 ${StyleConstants.SPACING_SM} 0;
        """.trimIndent()
    }

    private val errorLabel = Label().apply {
        style = """
            -fx-font-size: ${StyleConstants.FONT_SIZE_SM}px;
            -fx-text-fill: ${StyleConstants.DANGER_500};
        """.trimIndent()
        isVisible = false
    }

    init {
        title = "Copy Photos"
        headerText = "${Icons.PHOTO_STACK} Copy photos from \"${sourceCategory.name}\" (${sourceCategory.photos.size} photos)"

        dialogPane.content = createContent()
        dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)

        dialogPane.style = """
            -fx-background-color: ${StyleConstants.BACKGROUND_PRIMARY};
            -fx-font-family: ${StyleConstants.FONT_FAMILY};
        """.trimIndent()

        val okButton = dialogPane.lookupButton(ButtonType.OK) as Button
        okButton.style = buildPrimaryButtonStyle()
        okButton.text = "Copy Photos"
        okButton.setOnAction { handleCopy() }

        val cancelButton = dialogPane.lookupButton(ButtonType.CANCEL) as Button
        cancelButton.style = buildSecondaryButtonStyle()

        updatePreview()
    }

    private fun createContent(): GridPane {
        return GridPane().apply {
            hgap = StyleConstants.SPACING_BASE
            vgap = StyleConstants.SPACING_SM
            padding = Insets(StyleConstants.SPACING_LG)
            alignment = Pos.TOP_LEFT

            val quickButtonsLabel = Label("Quick:").apply {
                style = labelStyle()
            }
            add(quickButtonsLabel, 0, 0)

            val selectAllBtn = Button("Select All").apply {
                style = buildQuickButtonStyle()
                setOnAction { setAllCheckBoxes(true) }
            }
            add(selectAllBtn, 1, 0)

            val deselectAllBtn = Button("Deselect All").apply {
                style = buildQuickButtonStyle()
                setOnAction { setAllCheckBoxes(false) }
            }
            add(deselectAllBtn, 2, 0)

            val rangeLabel = Label("Or type category range:").apply {
                style = labelStyle()
            }
            add(rangeLabel, 0, 1)

            val rangeBox = VBox(StyleConstants.SPACING_SM).apply {
                children.add(rangeInputField)
            }
            add(rangeBox, 1, 1, 2, 1)

            val addRangeBtn = Button("+ Add Range").apply {
                style = buildAddRangeButtonStyle()
                setOnAction { handleAddRange() }
            }
            add(addRangeBtn, 3, 1)

            errorLabel.isVisible = false
            add(errorLabel, 1, 2, 3, 1)

            val selectLabel = Label("Select destination categories:").apply {
                style = """
                    -fx-font-size: ${StyleConstants.FONT_SIZE_BASE}px;
                    -fx-font-weight: 600;
                    -fx-text-fill: ${StyleConstants.TEXT_PRIMARY};
                    -fx-padding: ${StyleConstants.SPACING_SM} 0 ${StyleConstants.SPACING_SM} 0;
                """.trimIndent()
            }
            add(selectLabel, 0, 3, 4, 1)

            val checkBoxContainer = VBox(StyleConstants.SPACING_XS).apply {
                style = """
                    -fx-padding: ${StyleConstants.SPACING_SM};
                    -fx-background-color: ${StyleConstants.NEUTRAL_50};
                    -fx-background-radius: ${StyleConstants.RADIUS_BASE};
                """.trimIndent()
            }

            val sortedCategories = allCategories
                .filter { it.id != sourceCategory.id }
                .sortedBy { it.number }

            for (category in sortedCategories) {
                val checkBox = CheckBox("${category.name}  (${category.photos.size} photos)").apply {
                    style = """
                        -fx-font-size: ${StyleConstants.FONT_SIZE_BASE}px;
                        -fx-text-fill: ${StyleConstants.TEXT_PRIMARY};
                    """.trimIndent()
                    selectedProperty().addListener { _, _, _ -> updatePreview() }
                }
                categoryCheckBoxes[category.number] = checkBox
                checkBoxContainer.children.add(checkBox)
            }

            val scrollPane = ScrollPane(checkBoxContainer).apply {
                prefHeight = 200.0
                prefWidth = 400.0
                vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
                hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
                style = """
                    -fx-background-color: transparent;
                """.trimIndent()
            }
            add(scrollPane, 0, 4, 4, 1)

            previewLabel.style = """
                -fx-font-size: ${StyleConstants.FONT_SIZE_BASE}px;
                -fx-text-fill: ${StyleConstants.TEXT_SECONDARY};
                -fx-padding: ${StyleConstants.SPACING_MD} 0 ${StyleConstants.SPACING_SM} 0;
            """.trimIndent()
            add(previewLabel, 0, 5, 4, 1)
        }
    }

    private fun handleAddRange() {
        val input = rangeInputField.text.trim()
        if (input.isEmpty()) {
            showError("Please enter a category range")
            return
        }

        val numbers = copyService.parseCategoryRange(input, allCategories)
        if (numbers.isEmpty()) {
            showError("Invalid format. Use: 10-16, 1,3,5, or 2-4,8,10-12")
            return
        }

        val existingCategories = allCategories.filter { it.number in numbers }
        val existingNumbers = existingCategories.map { it.number }.toSet()
        val missingNumbers = numbers.filter { it !in existingNumbers }

        val categoriesToCreate = mutableListOf<Int>()
        for (num in missingNumbers) {
            categoriesToCreate.add(num)
        }

        if (categoriesToCreate.isNotEmpty()) {
            val createdCategories = mutableListOf<Category>()
            for (num in categoriesToCreate.sorted()) {
                val newCat = allCategories.find { it.number == num }
                    ?: Category.create(num)
                createdCategories.add(newCat)
            }
            onCategoriesCreated(createdCategories)
        }

        var added = 0
        for (num in numbers) {
            val checkBox = categoryCheckBoxes[num]
            if (checkBox != null) {
                checkBox.isSelected = true
                added++
            }
        }

        if (added == 0 && missingNumbers.isNotEmpty()) {
            showError("Categories ${missingNumbers.joinToString(",")} do not exist and could not be created")
        } else {
            hideError()
        }

        rangeInputField.clear()
    }

    private fun showError(message: String) {
        errorLabel.text = message
        errorLabel.isVisible = true
    }

    private fun hideError() {
        errorLabel.isVisible = false
    }

    private fun setAllCheckBoxes(selected: Boolean) {
        categoryCheckBoxes.values.forEach { it.isSelected = selected }
    }

    private fun updatePreview() {
        val selectedCategories = getSelectedCategories()
        val photoCount = sourceCategory.photos.size
        val categoryCount = selectedCategories.size
        val totalFiles = photoCount * categoryCount

        previewLabel.text = if (categoryCount == 0) {
            "Select destination categories to see preview"
        } else {
            "Preview: $photoCount photos → $categoryCount categories = $totalFiles files will be created"
        }
    }

    fun getSelectedCategories(): List<Category> {
        val selectedNumbers = categoryCheckBoxes.filter { it.value.isSelected }.keys
        return allCategories.filter { it.number in selectedNumbers }
    }

    private fun handleCopy() {
        val freshCategories = getFreshCategories()
        val selectedNumbers = categoryCheckBoxes.filter { it.value.isSelected }.keys
        val selectedCategories = freshCategories.filter { it.number in selectedNumbers }

        if (selectedCategories.isEmpty()) {
            showError("Please select at least one destination category")
            return
        }

        val freshSourceCategory = freshCategories.find { it.id == sourceCategory.id } ?: sourceCategory
        val result = copyService.copyPhotosToCategories(freshSourceCategory, selectedCategories)
        setResult(result)
        close()
    }

    private fun labelStyle(): String = """
        -fx-font-size: ${StyleConstants.FONT_SIZE_BASE}px;
        -fx-font-weight: 500;
        -fx-text-fill: ${StyleConstants.TEXT_PRIMARY};
    """.trimIndent()

    private fun buildTextFieldStyle(): String = """
        -fx-background-color: white;
        -fx-background-radius: ${StyleConstants.RADIUS_MD};
        -fx-border-color: ${StyleConstants.BORDER_DEFAULT};
        -fx-border-radius: ${StyleConstants.RADIUS_MD};
        -fx-font-size: ${StyleConstants.FONT_SIZE_BASE}px;
        -fx-padding: 8 12 8 12;
    """.trimIndent()

    private fun buildQuickButtonStyle(): String = """
        -fx-background-color: ${StyleConstants.NEUTRAL_100};
        -fx-text-fill: ${StyleConstants.TEXT_PRIMARY};
        -fx-font-size: ${StyleConstants.FONT_SIZE_SM}px;
        -fx-font-weight: 500;
        -fx-padding: 6 12 6 12;
        -fx-background-radius: ${StyleConstants.RADIUS_BASE};
        -fx-cursor: hand;
    """.trimIndent()

    private fun buildAddRangeButtonStyle(): String = """
        -fx-background-color: ${StyleConstants.PRIMARY_100};
        -fx-text-fill: ${StyleConstants.PRIMARY_700};
        -fx-font-size: ${StyleConstants.FONT_SIZE_SM}px;
        -fx-font-weight: 600;
        -fx-padding: 6 12 6 12;
        -fx-background-radius: ${StyleConstants.RADIUS_BASE};
        -fx-cursor: hand;
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
