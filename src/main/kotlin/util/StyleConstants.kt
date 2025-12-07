package util

/**
 * Constants for UI styling.
 * Centralizes all style-related constants for consistency.
 */
object StyleConstants {
    // Selection styles
    const val SELECTED_BORDER_COLOR = "#0077ff"
    const val SELECTED_BORDER_WIDTH = 8
    const val SELECTED_SHADOW_RADIUS = 18.0
    const val SELECTED_SHADOW_SPREAD = 0.7
    const val SELECTED_STYLE = "-fx-border-color: $SELECTED_BORDER_COLOR; " +
            "-fx-border-width: $SELECTED_BORDER_WIDTH; " +
            "-fx-effect: dropshadow(gaussian, $SELECTED_BORDER_COLOR, $SELECTED_SHADOW_RADIUS, $SELECTED_SHADOW_SPREAD, 0, 0);"

    // Category styles
    const val CATEGORY_NORMAL_STYLE = "-fx-border-color: black; -fx-border-width: 2; -fx-padding: 5;"
    const val CATEGORY_DRAG_OVER_STYLE = "-fx-border-color: blue; -fx-border-width: 3; -fx-padding: 5; -fx-background-color: lightblue;"

    // Image sizes
    const val PHOTO_GRID_WIDTH = 200.0
    const val PHOTO_CATEGORY_WIDTH = 220.0

    // Layout
    const val DEFAULT_COLUMNS = 3
    const val DEFAULT_DIVIDER_POSITION = 0.7  // 70% for photos, 30% for categories
    const val MIN_PHOTO_PANE_WIDTH = 300.0    // Minimum width for photo pane
    const val MIN_CATEGORY_PANE_WIDTH = 250.0 // Minimum width for category pane
    const val SCROLLBAR_WIDTH_ESTIMATE = 18.0 // Estimated scrollbar width
    
    // Category Card
    const val CATEGORY_CARD_WIDTH = 220.0
    const val CATEGORY_CARD_THUMBNAIL_SIZE = 120.0
    const val CATEGORY_CARD_PADDING = 10.0
    const val CATEGORY_CARD_BORDER_COLOR = "#cccccc"
    const val CATEGORY_CARD_HOVER_BORDER_COLOR = "#999999"
    const val CATEGORY_SELECTED_BACKGROUND = "#e3f2fd"

    // Drag and drop
    const val DRAG_OPACITY = 0.5
    const val NORMAL_OPACITY = 1.0
}
