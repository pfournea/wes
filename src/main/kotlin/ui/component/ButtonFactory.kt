package ui.component

import javafx.animation.ScaleTransition
import javafx.scene.control.Button
import javafx.util.Duration
import util.Icons
import util.StyleConstants

/**
 * Factory for creating professionally styled buttons.
 * Uses a clean, enterprise design language.
 */
object ButtonFactory {
    
    /**
     * Creates a primary action button (filled background).
     */
    fun createPrimaryButton(text: String, icon: String = ""): Button {
        return Button(formatButtonText(text, icon)).apply {
            val baseStyle = buildPrimaryButtonStyle(false)
            val hoverStyle = buildPrimaryButtonStyle(true)
            
            style = baseStyle
            
            setOnMouseEntered { 
                style = hoverStyle
                animateScale(this, 1.02)
            }
            setOnMouseExited { 
                style = baseStyle
                animateScale(this, 1.0)
            }
            
            setOnMousePressed { animateScale(this, 0.98) }
            setOnMouseReleased { animateScale(this, 1.02) }
        }
    }
    
    /**
     * Creates a secondary action button (outlined).
     */
    fun createSecondaryButton(text: String, icon: String = ""): Button {
        return Button(formatButtonText(text, icon)).apply {
            val baseStyle = buildSecondaryButtonStyle(false)
            val hoverStyle = buildSecondaryButtonStyle(true)
            
            style = baseStyle
            
            setOnMouseEntered { 
                style = hoverStyle
                animateScale(this, 1.02)
            }
            setOnMouseExited { 
                style = baseStyle
                animateScale(this, 1.0)
            }
            
            setOnMousePressed { animateScale(this, 0.98) }
            setOnMouseReleased { animateScale(this, 1.02) }
        }
    }
    
    /**
     * Creates a danger/destructive action button.
     */
    fun createDangerButton(text: String, icon: String = ""): Button {
        return Button(formatButtonText(text, icon)).apply {
            val baseStyle = buildDangerButtonStyle(false)
            val hoverStyle = buildDangerButtonStyle(true)
            
            style = baseStyle
            
            setOnMouseEntered { 
                style = hoverStyle
                animateScale(this, 1.02)
            }
            setOnMouseExited { 
                style = baseStyle
                animateScale(this, 1.0)
            }
            
            setOnMousePressed { animateScale(this, 0.98) }
            setOnMouseReleased { animateScale(this, 1.02) }
        }
    }
    
    // Pre-configured buttons for common actions
    
    fun createUploadButton(): Button = 
        createPrimaryButton("Upload Zip", Icons.UPLOAD)
    
    fun createSaveButton(): Button = 
        createSecondaryButton("Export", Icons.DOWNLOAD)
    
    fun createAddCategoryButton(): Button = 
        createPrimaryButton("Add Category", Icons.ADD)
    
    // Legacy method for backward compatibility
    fun createGradientButton(
        text: String, 
        gradientStart: String, 
        gradientEnd: String,
        icon: String = ""
    ): Button = createPrimaryButton(text, icon)
    
    private fun formatButtonText(text: String, icon: String): String {
        return if (icon.isNotEmpty()) "$icon  $text" else text
    }
    
    private fun buildPrimaryButtonStyle(isHovered: Boolean): String {
        val bgColor = if (isHovered) StyleConstants.PRIMARY_600 else StyleConstants.PRIMARY_500
        val shadow = if (isHovered) StyleConstants.SHADOW_MD else StyleConstants.SHADOW_SM
        
        return """
            -fx-background-color: $bgColor;
            -fx-text-fill: white;
            -fx-font-size: ${StyleConstants.FONT_SIZE_BASE}px;
            -fx-font-weight: 600;
            -fx-padding: 10 20 10 20;
            -fx-background-radius: ${StyleConstants.RADIUS_BASE};
            -fx-cursor: hand;
            -fx-effect: $shadow;
        """.trimIndent()
    }
    
    private fun buildSecondaryButtonStyle(isHovered: Boolean): String {
        val bgColor = if (isHovered) StyleConstants.NEUTRAL_100 else "transparent"
        val borderColor = if (isHovered) StyleConstants.NEUTRAL_300 else StyleConstants.NEUTRAL_200
        
        return """
            -fx-background-color: $bgColor;
            -fx-text-fill: ${StyleConstants.TEXT_PRIMARY};
            -fx-font-size: ${StyleConstants.FONT_SIZE_BASE}px;
            -fx-font-weight: 600;
            -fx-padding: 10 20 10 20;
            -fx-background-radius: ${StyleConstants.RADIUS_BASE};
            -fx-border-color: $borderColor;
            -fx-border-radius: ${StyleConstants.RADIUS_BASE};
            -fx-border-width: 1;
            -fx-cursor: hand;
        """.trimIndent()
    }
    
    private fun buildDangerButtonStyle(isHovered: Boolean): String {
        val bgColor = if (isHovered) StyleConstants.DANGER_600 else StyleConstants.DANGER_500
        val shadow = if (isHovered) StyleConstants.SHADOW_MD else StyleConstants.SHADOW_SM
        
        return """
            -fx-background-color: $bgColor;
            -fx-text-fill: white;
            -fx-font-size: ${StyleConstants.FONT_SIZE_BASE}px;
            -fx-font-weight: 600;
            -fx-padding: 10 20 10 20;
            -fx-background-radius: ${StyleConstants.RADIUS_BASE};
            -fx-cursor: hand;
            -fx-effect: $shadow;
        """.trimIndent()
    }
    
    private fun animateScale(button: Button, targetScale: Double) {
        val scaleTransition = ScaleTransition(Duration.millis(StyleConstants.ANIMATION_FAST), button).apply {
            toX = targetScale
            toY = targetScale
        }
        scaleTransition.play()
    }
}
