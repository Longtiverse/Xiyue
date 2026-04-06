package com.xiyue.app.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Design tokens for Xiyue app
 * 
 * Provides a centralized system for spacing, corner radius, elevation, and animation durations.
 * This ensures visual consistency across the entire application.
 */
object DesignTokens {
    
    /**
     * Spacing system
     * 
     * Use these values for padding, margins, and gaps between elements.
     * Based on 4dp grid system for visual rhythm.
     */
    object Spacing {
        /** Extra small spacing: 4dp - for tight spacing within components */
        val xs = 4.dp
        
        /** Small spacing: 8dp - for compact layouts */
        val sm = 8.dp
        
        /** Medium spacing: 16dp - default spacing for most use cases */
        val md = 16.dp
        
        /** Large spacing: 24dp - for section separation */
        val lg = 24.dp
        
        /** Extra large spacing: 32dp - for major section breaks */
        val xl = 32.dp
        
        /** Extra extra large spacing: 48dp - for page-level spacing */
        val xxl = 48.dp
    }
    
    /**
     * Corner radius system
     * 
     * Use these values for rounded corners on cards, buttons, and containers.
     */
    object CornerRadius {
        /** Small radius: 4dp - for subtle rounding */
        val sm = 4.dp
        
        /** Medium radius: 8dp - default for cards and buttons */
        val md = 8.dp
        
        /** Large radius: 16dp - for prominent elements */
        val lg = 16.dp
        
        /** Extra large radius: 24dp - for highly rounded elements */
        val xl = 24.dp
        
        /** Full radius: 9999dp - for circular elements */
        val full = 9999.dp
    }
    
    /**
     * Elevation system
     * 
     * Use these values for shadow depth on elevated surfaces.
     * Higher values create stronger shadows.
     */
    object Elevation {
        /** No elevation: 0dp - for flat surfaces */
        val none = 0.dp
        
        /** Small elevation: 2dp - for subtle depth */
        val sm = 2.dp
        
        /** Medium elevation: 4dp - default for cards */
        val md = 4.dp
        
        /** Large elevation: 8dp - for floating elements */
        val lg = 8.dp
        
        /** Extra large elevation: 16dp - for modals and dialogs */
        val xl = 16.dp
    }
    
    /**
     * Animation duration system
     * 
     * Use these values for consistent animation timing across the app.
     * All values in milliseconds.
     */
    object Duration {
        /** Fast duration: 150ms - for quick feedback */
        const val fast = 150
        
        /** Normal duration: 300ms - default for most animations */
        const val normal = 300
        
        /** Slow duration: 500ms - for complex transitions */
        const val slow = 500
        
        /** Extra slow duration: 800ms - for dramatic effects */
        const val extraSlow = 800
    }
    
    /**
     * Icon size system
     * 
     * Standard sizes for icons throughout the app.
     */
    object IconSize {
        /** Small icon: 16dp - for inline icons */
        val sm = 16.dp
        
        /** Medium icon: 24dp - default icon size */
        val md = 24.dp
        
        /** Large icon: 32dp - for prominent icons */
        val lg = 32.dp
        
        /** Extra large icon: 48dp - for hero icons */
        val xl = 48.dp
    }
    
    /**
     * Button height system
     * 
     * Standard heights for different button types.
     */
    object ButtonHeight {
        /** Small button: 32dp */
        val sm = 32.dp
        
        /** Medium button: 40dp - default */
        val md = 40.dp
        
        /** Large button: 48dp */
        val lg = 48.dp
        
        /** Extra large button: 56dp - for primary actions */
        val xl = 56.dp
    }
}
