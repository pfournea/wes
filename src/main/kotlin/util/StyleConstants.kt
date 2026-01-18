package util

/**
 * Constants for UI styling.
 * Centralizes all style-related constants for consistency.
 * Modern design system with glassmorphism and contemporary aesthetics.
 */
object StyleConstants {
    // Modern Color Palette - Purple/Teal Gradient Theme
    const val PRIMARY_GRADIENT_START = "#667eea"
    const val PRIMARY_GRADIENT_END = "#764ba2"
    const val ACCENT_GRADIENT_START = "#06beb6"
    const val ACCENT_GRADIENT_END = "#48b1bf"
    const val SUCCESS_GRADIENT_START = "#56ab2f"
    const val SUCCESS_GRADIENT_END = "#a8e063"
    const val WARNING_GRADIENT_START = "#f09819"
    const val WARNING_GRADIENT_END = "#ff5858"
    
    // Background colors
    const val BACKGROUND_LIGHT = "#f8f9fa"
    const val BACKGROUND_CARD = "#ffffff"
    const val BACKGROUND_OVERLAY = "rgba(255, 255, 255, 0.85)"
    
    // Selection styles - ONE style for both photos and categories
    const val SELECTED_STYLE = "-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #4c63d2; -fx-border-radius: 12; -fx-border-width: 5; -fx-effect: dropshadow(gaussian, rgba(76, 99, 210, 0.6), 28.0, 0.7, 0, 0);"

    // Category styles - Material elevation
    const val CATEGORY_NORMAL_STYLE = "-fx-border-color: transparent; -fx-border-width: 0; -fx-padding: 5;"
    const val CATEGORY_DRAG_OVER_STYLE = "-fx-border-color: #4c63d2; -fx-border-width: 3; -fx-padding: 5; " +
            "-fx-background-color: linear-gradient(to bottom right, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1)); " +
            "-fx-border-radius: 12; -fx-background-radius: 12;"

    // Image sizes
    const val PHOTO_GRID_WIDTH = 200.0
    const val PHOTO_CATEGORY_WIDTH = 220.0

    // Layout
    const val DEFAULT_COLUMNS = 3
    const val DEFAULT_DIVIDER_POSITION = 0.7  // 70% for photos, 30% for categories
    const val MIN_PHOTO_PANE_WIDTH = 300.0    // Minimum width for photo pane
    const val MIN_CATEGORY_PANE_WIDTH = 250.0 // Minimum width for category pane
    const val SCROLLBAR_WIDTH_ESTIMATE = 18.0 // Estimated scrollbar width
    
    // Category Card - Modern card design with elevation
    const val CATEGORY_CARD_WIDTH = 240.0
    const val CATEGORY_CARD_THUMBNAIL_SIZE = 140.0
    const val CATEGORY_CARD_PADDING = 16.0
    const val CATEGORY_CARD_BORDER_RADIUS = 16.0
    const val CATEGORY_CARD_BORDER_COLOR = "#e0e0e0"
    const val CATEGORY_CARD_HOVER_BORDER_COLOR = "#b0b0b0"
    const val CATEGORY_SELECTED_GRADIENT_START = "rgba(102, 126, 234, 0.15)"
    const val CATEGORY_SELECTED_GRADIENT_END = "rgba(118, 75, 162, 0.15)"
    const val CATEGORY_CARD_SHADOW = "rgba(0, 0, 0, 0.1)"
    const val CATEGORY_CARD_SHADOW_HOVER = "rgba(102, 126, 234, 0.3)"

    // Drag and drop
    const val DRAG_OPACITY = 0.6
    const val NORMAL_OPACITY = 1.0
    
    // Rotation controls - Enhanced modern style
    const val ROTATION_BUTTON_SIZE = 44.0
    const val ROTATION_BUTTON_ICON_SIZE = 18
    const val ROTATION_OVERLAY_BACKGROUND = "rgba(0, 0, 0, 0.6)"
    const val ROTATION_BUTTON_GRADIENT_START = "rgba(102, 126, 234, 0.9)"
    const val ROTATION_BUTTON_GRADIENT_END = "rgba(118, 75, 162, 0.9)"
    
    // Elevation shadows (Material Design inspired)
    const val ELEVATION_1 = "dropshadow(gaussian, rgba(0, 0, 0, 0.08), 4, 0.0, 0, 2)"
    const val ELEVATION_2 = "dropshadow(gaussian, rgba(0, 0, 0, 0.12), 8, 0.0, 0, 4)"
    const val ELEVATION_3 = "dropshadow(gaussian, rgba(0, 0, 0, 0.16), 12, 0.0, 0, 6)"
    const val ELEVATION_4 = "dropshadow(gaussian, rgba(0, 0, 0, 0.20), 16, 0.0, 0, 8)"
}
