package util

/**
 * Design System Constants for Professional Enterprise UI.
 * 
 * Inspired by modern design systems (Linear, Notion, Figma):
 * - Clean, neutral color palette with purposeful accent colors
 * - Refined typography hierarchy
 * - Subtle, sophisticated shadows
 * - Consistent spacing scale
 */
object StyleConstants {
    
    // ═══════════════════════════════════════════════════════════════════════════
    // COLOR PALETTE - Enterprise Neutral with Blue Accent
    // ═══════════════════════════════════════════════════════════════════════════
    
    // Primary Blue - For primary actions and focus states
    const val PRIMARY_50 = "#eff6ff"
    const val PRIMARY_100 = "#dbeafe"
    const val PRIMARY_200 = "#bfdbfe"
    const val PRIMARY_300 = "#93c5fd"
    const val PRIMARY_400 = "#60a5fa"
    const val PRIMARY_500 = "#3b82f6"  // Main primary
    const val PRIMARY_600 = "#2563eb"  // Primary hover
    const val PRIMARY_700 = "#1d4ed8"
    
    // Neutral Gray - For text, borders, backgrounds
    const val NEUTRAL_50 = "#fafafa"   // Lightest background
    const val NEUTRAL_100 = "#f5f5f5"  // Card background
    const val NEUTRAL_200 = "#e5e5e5"  // Borders, dividers
    const val NEUTRAL_300 = "#d4d4d4"  // Hover borders
    const val NEUTRAL_400 = "#a3a3a3"  // Placeholder text
    const val NEUTRAL_500 = "#737373"  // Secondary text
    const val NEUTRAL_600 = "#525252"  // Body text
    const val NEUTRAL_700 = "#404040"  // Headings
    const val NEUTRAL_800 = "#262626"  // Primary text
    const val NEUTRAL_900 = "#171717"  // Darkest text
    
    // Success Green
    const val SUCCESS_50 = "#f0fdf4"
    const val SUCCESS_500 = "#22c55e"
    const val SUCCESS_600 = "#16a34a"
    
    // Warning Amber
    const val WARNING_50 = "#fffbeb"
    const val WARNING_500 = "#f59e0b"
    const val WARNING_600 = "#d97706"
    
    // Error/Danger Red
    const val DANGER_50 = "#fef2f2"
    const val DANGER_500 = "#ef4444"
    const val DANGER_600 = "#dc2626"
    
    // ═══════════════════════════════════════════════════════════════════════════
    // SEMANTIC COLORS
    // ═══════════════════════════════════════════════════════════════════════════
    
    const val BACKGROUND_PRIMARY = "#ffffff"
    const val BACKGROUND_SECONDARY = NEUTRAL_50
    const val BACKGROUND_TERTIARY = NEUTRAL_100
    
    const val TEXT_PRIMARY = NEUTRAL_800
    const val TEXT_SECONDARY = NEUTRAL_500
    const val TEXT_MUTED = NEUTRAL_400
    
    const val BORDER_DEFAULT = NEUTRAL_200
    const val BORDER_HOVER = NEUTRAL_300
    const val BORDER_FOCUS = PRIMARY_500
    
    // ═══════════════════════════════════════════════════════════════════════════
    // SELECTION & INTERACTION STATES
    // ═══════════════════════════════════════════════════════════════════════════
    
    // Unselected state - Reserve space for the border so selection doesn't shift layout
    const val UNSELECTED_CONTAINER_STYLE = """
        -fx-border-color: transparent;
        -fx-border-width: 3;
        -fx-border-radius: 4;
        -fx-background-color: transparent;
        -fx-background-radius: 4;
        -fx-padding: 3;
    """
    
    // Selected state - Prominent blue border with light blue background on container
    const val SELECTED_CONTAINER_STYLE = """
        -fx-border-color: ${PRIMARY_500};
        -fx-border-width: 3;
        -fx-border-radius: 4;
        -fx-background-color: ${PRIMARY_100};
        -fx-background-radius: 4;
        -fx-padding: 3;
        -fx-effect: dropshadow(gaussian, ${PRIMARY_400}, 10, 0.5, 0, 0);
    """
    
    // Legacy selected style on ImageView (kept for reference)
    const val SELECTED_STYLE = "-fx-effect: dropshadow(gaussian, ${PRIMARY_500}, 8, 0.6, 0, 0), innershadow(gaussian, ${PRIMARY_400}, 6, 0.4, 0, 0);"
    
    // Category drag-over visual
    const val CATEGORY_NORMAL_STYLE = "-fx-border-color: transparent; -fx-border-width: 0; -fx-padding: 5;"
    const val CATEGORY_DRAG_OVER_STYLE = "-fx-border-color: ${PRIMARY_500}; -fx-border-width: 2; -fx-padding: 5; -fx-background-color: ${PRIMARY_50}; -fx-border-radius: 12; -fx-background-radius: 12;"
    
    // ═══════════════════════════════════════════════════════════════════════════
    // TYPOGRAPHY
    // ═══════════════════════════════════════════════════════════════════════════
    
    const val FONT_FAMILY = "'SF Pro Display', 'Segoe UI', 'Roboto', -apple-system, BlinkMacSystemFont, sans-serif"
    
    // Font sizes
    const val FONT_SIZE_XS = 11.0
    const val FONT_SIZE_SM = 12.0
    const val FONT_SIZE_BASE = 14.0
    const val FONT_SIZE_MD = 15.0
    const val FONT_SIZE_LG = 16.0
    const val FONT_SIZE_XL = 18.0
    const val FONT_SIZE_2XL = 20.0
    const val FONT_SIZE_3XL = 24.0
    
    // ═══════════════════════════════════════════════════════════════════════════
    // SPACING SCALE
    // ═══════════════════════════════════════════════════════════════════════════
    
    const val SPACING_XS = 4.0
    const val SPACING_SM = 8.0
    const val SPACING_MD = 12.0
    const val SPACING_BASE = 16.0
    const val SPACING_LG = 20.0
    const val SPACING_XL = 24.0
    const val SPACING_2XL = 32.0
    const val SPACING_3XL = 40.0
    
    // ═══════════════════════════════════════════════════════════════════════════
    // BORDER RADIUS
    // ═══════════════════════════════════════════════════════════════════════════
    
    const val RADIUS_SM = 4.0
    const val RADIUS_MD = 6.0
    const val RADIUS_BASE = 8.0
    const val RADIUS_LG = 12.0
    const val RADIUS_XL = 16.0
    const val RADIUS_FULL = 9999.0
    
    // ═══════════════════════════════════════════════════════════════════════════
    // ELEVATION / SHADOWS - Refined and subtle
    // ═══════════════════════════════════════════════════════════════════════════
    
    const val SHADOW_XS = "dropshadow(gaussian, rgba(0, 0, 0, 0.04), 2, 0.0, 0, 1)"
    const val SHADOW_SM = "dropshadow(gaussian, rgba(0, 0, 0, 0.05), 4, 0.0, 0, 2)"
    const val SHADOW_MD = "dropshadow(gaussian, rgba(0, 0, 0, 0.08), 8, 0.0, 0, 4)"
    const val SHADOW_LG = "dropshadow(gaussian, rgba(0, 0, 0, 0.10), 12, 0.0, 0, 6)"
    const val SHADOW_XL = "dropshadow(gaussian, rgba(0, 0, 0, 0.12), 20, 0.0, 0, 8)"
    
    // Legacy aliases for backward compatibility
    const val ELEVATION_1 = SHADOW_SM
    const val ELEVATION_2 = SHADOW_MD
    const val ELEVATION_3 = SHADOW_LG
    const val ELEVATION_4 = SHADOW_XL
    
    // ═══════════════════════════════════════════════════════════════════════════
    // LAYOUT DIMENSIONS
    // ═══════════════════════════════════════════════════════════════════════════
    
    // Image sizes
    const val PHOTO_GRID_WIDTH = 180.0
    const val PHOTO_CATEGORY_WIDTH = 200.0
    
    // Grid layout
    const val DEFAULT_COLUMNS = 4
    const val GRID_GAP = 16.0
    
    // Split pane
    const val DEFAULT_DIVIDER_POSITION = 0.72
    const val MIN_PHOTO_PANE_WIDTH = 320.0
    const val MIN_CATEGORY_PANE_WIDTH = 280.0
    const val SCROLLBAR_WIDTH_ESTIMATE = 16.0
    
    // Category Card
    const val CATEGORY_CARD_WIDTH = 260.0
    const val CATEGORY_CARD_THUMBNAIL_SIZE = 120.0
    const val CATEGORY_CARD_PADDING = 16.0
    const val CATEGORY_CARD_BORDER_RADIUS = RADIUS_LG
    const val CATEGORY_CARD_BORDER_COLOR = BORDER_DEFAULT
    const val CATEGORY_CARD_HOVER_BORDER_COLOR = BORDER_HOVER
    const val CATEGORY_SELECTED_GRADIENT_START = "rgba(59, 130, 246, 0.08)"
    const val CATEGORY_SELECTED_GRADIENT_END = "rgba(59, 130, 246, 0.12)"
    const val CATEGORY_CARD_SHADOW = SHADOW_SM
    const val CATEGORY_CARD_SHADOW_HOVER = SHADOW_MD
    
    // ═══════════════════════════════════════════════════════════════════════════
    // INTERACTION
    // ═══════════════════════════════════════════════════════════════════════════
    
    const val DRAG_OPACITY = 0.7
    const val NORMAL_OPACITY = 1.0
    
    // Control button sizes
    const val CONTROL_BUTTON_SIZE = 32.0
    const val CONTROL_BUTTON_ICON_SIZE = 14
    
    // Rotation button legacy aliases
    const val ROTATION_BUTTON_SIZE = CONTROL_BUTTON_SIZE
    const val ROTATION_BUTTON_ICON_SIZE = CONTROL_BUTTON_ICON_SIZE
    const val ROTATION_OVERLAY_BACKGROUND = "rgba(0, 0, 0, 0.5)"
    const val ROTATION_BUTTON_GRADIENT_START = PRIMARY_500
    const val ROTATION_BUTTON_GRADIENT_END = PRIMARY_600
    
    // ═══════════════════════════════════════════════════════════════════════════
    // ANIMATION
    // ═══════════════════════════════════════════════════════════════════════════
    
    const val ANIMATION_FAST = 100.0
    const val ANIMATION_NORMAL = 200.0
    const val ANIMATION_SLOW = 300.0
    
    // ═══════════════════════════════════════════════════════════════════════════
    // LEGACY GRADIENT ALIASES (for backward compatibility)
    // ═══════════════════════════════════════════════════════════════════════════
    
    const val PRIMARY_GRADIENT_START = PRIMARY_500
    const val PRIMARY_GRADIENT_END = PRIMARY_600
    const val ACCENT_GRADIENT_START = PRIMARY_400
    const val ACCENT_GRADIENT_END = PRIMARY_500
    const val SUCCESS_GRADIENT_START = SUCCESS_500
    const val SUCCESS_GRADIENT_END = SUCCESS_600
    const val WARNING_GRADIENT_START = DANGER_500
    const val WARNING_GRADIENT_END = DANGER_600
    
    // Legacy background aliases
    const val BACKGROUND_LIGHT = BACKGROUND_SECONDARY
    const val BACKGROUND_CARD = BACKGROUND_PRIMARY
    const val BACKGROUND_OVERLAY = "rgba(255, 255, 255, 0.95)"
}
