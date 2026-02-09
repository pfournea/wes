package ui.controller

import domain.model.Category
import domain.service.ExportResult
import domain.service.ExportService
import javafx.application.Platform
import javafx.concurrent.Task
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import ui.component.ExportSuccessDialog
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

        showExportProgress(primaryStage, directory, categories)
    }

    private fun chooseDirectory(stage: Stage): File? {
        return DirectoryChooser().apply {
            title = "Select Directory to Save Categorized Photos"
        }.showDialog(stage)
    }

    private fun showExportProgress(primaryStage: Stage, directory: File, categories: List<Category>) {
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
            Platform.runLater { handleExportSuccess(primaryStage, exportTask.value, directory) }
        }

        exportTask.setOnFailed {
            progressDialog.close()
            Platform.runLater {
                showAlert(
                    Alert.AlertType.ERROR,
                    "Export Failed",
                    "An error occurred during export: ${exportTask.exception?.message}"
                )
            }
        }

        exportTask.setOnCancelled {
            progressDialog.close()
            Platform.runLater {
                showAlert(Alert.AlertType.INFORMATION, "Export Cancelled", "Export operation was cancelled by user.")
            }
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

    private fun handleExportSuccess(primaryStage: Stage, result: ExportResult, directory: File) {
        if (result.success) {
            ExportSuccessDialog(directory, result.photosCopied).showAndWait()
        } else {
            showAlert(
                Alert.AlertType.ERROR,
                "Export Failed",
                "Some errors occurred:\n${result.errors.joinToString("\n")}"
            )
        }
    }

    private fun showAlert(type: Alert.AlertType, title: String, content: String) {
        Alert(type).apply {
            this.title = title
            headerText = null
            contentText = content
        }.showAndWait()
    }

    private fun showConfirmDialog(title: String, content: String): Boolean {
        val result = Alert(Alert.AlertType.WARNING).apply {
            this.title = title
            headerText = null
            contentText = content
            buttonTypes.setAll(ButtonType.OK, ButtonType.CANCEL)
        }.showAndWait()
        return result.isPresent && result.get() == ButtonType.OK
    }
}
