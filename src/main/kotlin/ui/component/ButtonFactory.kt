package ui.component

import javafx.animation.ScaleTransition
import javafx.scene.control.Button
import javafx.util.Duration
import util.StyleConstants

object ButtonFactory {
    
    fun createGradientButton(
        text: String, 
        gradientStart: String, 
        gradientEnd: String,
        icon: String = ""
    ): Button {
        return Button(if (icon.isNotEmpty()) "$icon  $text" else text).apply {
            val baseStyle = buildGradientButtonStyle(gradientStart, gradientEnd, false)
            val hoverStyle = buildGradientButtonStyle(gradientStart, gradientEnd, true)
            
            style = baseStyle
            
            // Smooth hover animation
            setOnMouseEntered { 
                style = hoverStyle
                animateScale(this, 1.05)
            }
            setOnMouseExited { 
                style = baseStyle
                animateScale(this, 1.0)
            }
            
            // Press animation
            setOnMousePressed {
                animateScale(this, 0.98)
            }
            setOnMouseReleased {
                animateScale(this, 1.05)
            }
        }
    }
    
    fun createUploadButton(): Button = 
        createGradientButton(
            "Upload Zip File", 
            StyleConstants.SUCCESS_GRADIENT_START, 
            StyleConstants.SUCCESS_GRADIENT_END,
            "📁"
        )
    
    fun createSaveButton(): Button = 
        createGradientButton(
            "Save Images", 
            StyleConstants.ACCENT_GRADIENT_START, 
            StyleConstants.ACCENT_GRADIENT_END,
            "💾"
        )
    
    fun createAddCategoryButton(): Button = 
        createGradientButton(
            "Add Category", 
            StyleConstants.PRIMARY_GRADIENT_START, 
            StyleConstants.PRIMARY_GRADIENT_END,
            "➕"
        )
    
    private fun buildGradientButtonStyle(
        gradientStart: String, 
        gradientEnd: String,
        isHovered: Boolean
    ): String {
        val elevation = if (isHovered) StyleConstants.ELEVATION_3 else StyleConstants.ELEVATION_2
        val brightness = if (isHovered) "1.1" else "1.0"
        
        return """
            -fx-background-color: linear-gradient(to right, $gradientStart, $gradientEnd);
            -fx-text-fill: white;
            -fx-font-size: 15px;
            -fx-font-weight: bold;
            -fx-padding: 12 24 12 24;
            -fx-background-radius: 12;
            -fx-cursor: hand;
            -fx-effect: $elevation;
            -fx-brightness: $brightness;
            -fx-alignment: center;
            -fx-content-display: center;
        """.trimIndent()
    }
    
    private fun animateScale(button: Button, targetScale: Double) {
        val scaleTransition = ScaleTransition(Duration.millis(150.0), button).apply {
            toX = targetScale
            toY = targetScale
        }
        scaleTransition.play()
    }
}
