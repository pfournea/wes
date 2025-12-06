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
    const val PHOTO_PANE_WIDTH_RATIO = 0.6
    const val CATEGORY_PANE_WIDTH_RATIO = 0.4
    const val COLUMN_WIDTH_ESTIMATE = 150.0

    // Drag and drop
    const val DRAG_OPACITY = 0.5
    const val NORMAL_OPACITY = 1.0
}
