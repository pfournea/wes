package ui.component

import javafx.scene.control.Button

object ButtonFactory {
    
    fun createPrimaryButton(text: String, baseColor: String, hoverColor: String): Button {
        return Button(text).apply {
            style = buildButtonStyle(baseColor)
            
            setOnMouseEntered { style = buildButtonStyle(hoverColor) }
            setOnMouseExited { style = buildButtonStyle(baseColor) }
        }
    }
    
    fun createUploadButton(): Button = 
        createPrimaryButton("Upload Zip File", "#4CAF50", "#45a049")
    
    fun createSaveButton(): Button = 
        createPrimaryButton("Save Images", "#2196F3", "#1976D2")
    
    fun createAddCategoryButton(): Button = 
        createPrimaryButton("Add Category", "#FF9800", "#F57C00")
    
    private fun buildButtonStyle(backgroundColor: String): String = """
        -fx-background-color: $backgroundColor;
        -fx-text-fill: white;
        -fx-font-size: 14px;
        -fx-font-weight: bold;
        -fx-padding: 10 20 10 20;
        -fx-background-radius: 5;
        -fx-cursor: hand;
    """.trimIndent()
}
