package ui.controller

import domain.model.Category
import domain.service.ExportResult
import domain.service.ExportService
import javafx.concurrent.Task
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import util.StyleConstants
import java.io.File

class ExportController(private val exportService: ExportService) {

    fun handleSaveImages(primaryStage: Stage, categories: List<Category>) {
        val totalPhotos = categories.sumOf { it.photos.size }

        if (totalPhotos == 0) {
            showAlert(Alert.AlertType.WARNING, "No Photos to Save", "Please add photos to categories first.")
            return
        }

        val directory = chooseDirectory(primaryStage) ?: return

        val existingFiles = exportService.countFilesInDirectory(directory.toPath())
        if (existingFiles > 0) {
            val confirmed = showConfirmDialog(
                "Directory Warning",
                "Directory contains $existingFiles files. All files will be deleted and replaced. Continue?"
            )
            if (!confirmed) return
        }

        showExportProgress(directory, categories)
    }

    private fun chooseDirectory(stage: Stage): File? {
        return DirectoryChooser().apply {
            title = "Select Directory to Save Categorized Photos"
        }.showDialog(stage)
    }

    private fun showExportProgress(directory: File, categories: List<Category>) {
        val progressDialog = Dialog<Void>()
        progressDialog.title = "Exporting Photos"
        progressDialog.headerText = "Copying photos to ${directory.name}..."

        val progressBar = ProgressBar().apply { prefWidth = 300.0 }
        val progressLabel = Label("Preparing...")

        progressDialog.dialogPane.content = VBox(10.0, progressLabel, progressBar).apply {
            style = "-fx-padding: 20;"
        }
        progressDialog.dialogPane.buttonTypes.add(ButtonType.CANCEL)

        val exportTask = createExportTask(categories, directory)

        progressBar.progressProperty().bind(exportTask.progressProperty())
        progressLabel.textProperty().bind(exportTask.messageProperty())

        exportTask.setOnSucceeded {
            progressDialog.close()
            handleExportSuccess(exportTask.value, directory)
        }

        exportTask.setOnFailed {
            progressDialog.close()
            showAlert(
                Alert.AlertType.ERROR,
                "Export Failed",
                "An error occurred during export: ${exportTask.exception?.message}"
            )
        }

        exportTask.setOnCancelled {
            progressDialog.close()
            showAlert(Alert.AlertType.INFORMATION, "Export Cancelled", "Export operation was cancelled by user.")
        }

        Thread(exportTask).apply { isDaemon = true }.start()
        progressDialog.showAndWait()
    }

    private fun createExportTask(categories: List<Category>, directory: File): Task<ExportResult> {
        return object : Task<ExportResult>() {
            override fun call(): ExportResult {
                updateMessage("Deleting existing files...")
                updateProgress(0.0, 1.0)

                val result = exportService.exportCategories(categories, directory.toPath())

                updateProgress(1.0, 1.0)
                updateMessage("Complete!")

                return result
            }
        }
    }

    private fun handleExportSuccess(result: ExportResult, directory: File) {
        if (result.success) {
            showSuccessDialog(result, directory)
        } else {
            showAlert(
                Alert.AlertType.ERROR,
                "Export Failed",
                "Some errors occurred:\n${result.errors.joinToString("\n")}"
            )
        }
    }
    
    private fun showSuccessDialog(result: ExportResult, directory: File) {
        val dialog = Dialog<ButtonType>()
        dialog.title = "Export Successful"
        
        val successIcon = Label("✓").apply {
            font = Font.font("System", FontWeight.BOLD, 48.0)
            style = """
                -fx-text-fill: white;
                -fx-background-color: linear-gradient(to bottom right, 
                    ${StyleConstants.SUCCESS_GRADIENT_START}, 
                    ${StyleConstants.SUCCESS_GRADIENT_END});
                -fx-background-radius: 50;
                -fx-padding: 20 28 20 28;
                -fx-effect: ${StyleConstants.ELEVATION_2};
            """.trimIndent()
        }
        
        val titleLabel = Label("Export Complete!").apply {
            font = Font.font("System", FontWeight.BOLD, 22.0)
            style = "-fx-text-fill: #2d3748;"
        }
        
        val photosLabel = Label("${result.photosCopied} photos exported").apply {
            font = Font.font("System", FontWeight.SEMI_BOLD, 16.0)
            style = "-fx-text-fill: #4a5568;"
        }
        
        val pathLabel = Label(directory.absolutePath).apply {
            font = Font.font("System", 13.0)
            style = """
                -fx-text-fill: #718096;
                -fx-background-color: #f7fafc;
                -fx-padding: 8 12 8 12;
                -fx-background-radius: 6;
            """.trimIndent()
            maxWidth = 350.0
            isWrapText = true
        }
        
        val deletedLabel = Label("${result.filesDeleted} old files were cleaned up").apply {
            font = Font.font("System", 12.0)
            style = "-fx-text-fill: #a0aec0;"
        }
        
        val content = VBox(16.0).apply {
            alignment = Pos.CENTER
            padding = Insets(30.0, 40.0, 20.0, 40.0)
            children.addAll(successIcon, titleLabel, photosLabel, pathLabel, deletedLabel)
            style = "-fx-background-color: white;"
        }
        
        dialog.dialogPane.content = content
        dialog.dialogPane.style = """
            -fx-background-color: white;
            -fx-font-family: 'Segoe UI', 'Roboto', sans-serif;
        """.trimIndent()
        
        val okButtonType = ButtonType("Done", ButtonBar.ButtonData.OK_DONE)
        dialog.dialogPane.buttonTypes.add(okButtonType)
        
        val okButton = dialog.dialogPane.lookupButton(okButtonType) as Button
        okButton.style = """
            -fx-background-color: linear-gradient(to right, 
                ${StyleConstants.SUCCESS_GRADIENT_START}, 
                ${StyleConstants.SUCCESS_GRADIENT_END});
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-padding: 10 30 10 30;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            -fx-effect: ${StyleConstants.ELEVATION_1};
        """.trimIndent()
        
        dialog.showAndWait()
    }

    private fun showAlert(type: Alert.AlertType, title: String, content: String) {
        Alert(type).apply {
            this.title = title
            headerText = null
            contentText = content
        }.showAndWait()
    }

    private fun showConfirmDialog(title: String, content: String): Boolean {
        val result = Alert(Alert.AlertType.CONFIRMATION).apply {
            this.title = title
            headerText = null
            contentText = content
        }.showAndWait()
        return result.isPresent && result.get() == ButtonType.OK
    }
}
