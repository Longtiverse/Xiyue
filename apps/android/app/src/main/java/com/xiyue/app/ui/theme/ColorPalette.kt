package com.xiyue.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Extended color palette for Xiyue app
 * 
 * Provides additional semantic colors beyond Material Theme defaults.
 * These colors are used for specific UI elements and states.
 */
object ColorPalette {
    
    /**
     * Light theme colors
     */
    object Light {
        // Primary colors
        val primary = Color(0xFF6750A4)
        val onPrimary = Color(0xFFFFFFFF)
        val primaryContainer = Color(0xFFEADDFF)
        val onPrimaryContainer = Color(0xFF21005D)
        
        // Secondary colors
        val secondary = Color(0xFF625B71)
        val onSecondary = Color(0xFFFFFFFF)
        val secondaryContainer = Color(0xFFE8DEF8)
        val onSecondaryContainer = Color(0xFF1D192B)
        
        // Tertiary colors
        val tertiary = Color(0xFF7D5260)
        val onTertiary = Color(0xFFFFFFFF)
        val tertiaryContainer = Color(0xFFFFD8E4)
        val onTertiaryContainer = Color(0xFF31111D)
        
        // Error colors
        val error = Color(0xFFB3261E)
        val onError = Color(0xFFFFFFFF)
        val errorContainer = Color(0xFFF9DEDC)
        val onErrorContainer = Color(0xFF410E0B)
        
        // Background colors
        val background = Color(0xFFFFFBFE)
        val onBackground = Color(0xFF1C1B1F)
        
        // Surface colors
        val surface = Color(0xFFFFFBFE)
        val onSurface = Color(0xFF1C1B1F)
        val surfaceVariant = Color(0xFFE7E0EC)
        val onSurfaceVariant = Color(0xFF49454F)
        
        // Outline colors
        val outline = Color(0xFF79747E)
        val outlineVariant = Color(0xFFCAC4D0)
        
        // Custom semantic colors
        val success = Color(0xFF4CAF50)
        val onSuccess = Color(0xFFFFFFFF)
        val successContainer = Color(0xFFC8E6C9)
        val onSuccessContainer = Color(0xFF1B5E20)
        
        val warning = Color(0xFFFFA726)
        val onWarning = Color(0xFF000000)
        val warningContainer = Color(0xFFFFE0B2)
        val onWarningContainer = Color(0xFFE65100)
        
        val info = Color(0xFF2196F3)
        val onInfo = Color(0xFFFFFFFF)
        val infoContainer = Color(0xFFBBDEFB)
        val onInfoContainer = Color(0xFF0D47A1)
        
        // Music-specific colors
        val activeNote = Color(0xFF6750A4)
        val inactiveNote = Color(0xFFE7E0EC)
        val keyboardWhiteKey = Color(0xFFFFFFFF)
        val keyboardBlackKey = Color(0xFF1C1B1F)
    }
    
    /**
     * Dark theme colors
     */
    object Dark {
        // Primary colors
        val primary = Color(0xFFD0BCFF)
        val onPrimary = Color(0xFF381E72)
        val primaryContainer = Color(0xFF4F378B)
        val onPrimaryContainer = Color(0xFFEADDFF)
        
        // Secondary colors
        val secondary = Color(0xFFCCC2DC)
        val onSecondary = Color(0xFF332D41)
        val secondaryContainer = Color(0xFF4A4458)
        val onSecondaryContainer = Color(0xFFE8DEF8)
        
        // Tertiary colors
        val tertiary = Color(0xFFEFB8C8)
        val onTertiary = Color(0xFF492532)
        val tertiaryContainer = Color(0xFF633B48)
        val onTertiaryContainer = Color(0xFFFFD8E4)
        
        // Error colors
        val error = Color(0xFFF2B8B5)
        val onError = Color(0xFF601410)
        val errorContainer = Color(0xFF8C1D18)
        val onErrorContainer = Color(0xFFF9DEDC)
        
        // Background colors
        val background = Color(0xFF1C1B1F)
        val onBackground = Color(0xFFE6E1E5)
        
        // Surface colors
        val surface = Color(0xFF1C1B1F)
        val onSurface = Color(0xFFE6E1E5)
        val surfaceVariant = Color(0xFF49454F)
        val onSurfaceVariant = Color(0xFFCAC4D0)
        
        // Outline colors
        val outline = Color(0xFF938F99)
        val outlineVariant = Color(0xFF49454F)
        
        // Custom semantic colors
        val success = Color(0xFF81C784)
        val onSuccess = Color(0xFF1B5E20)
        val successContainer = Color(0xFF2E7D32)
        val onSuccessContainer = Color(0xFFC8E6C9)
        
        val warning = Color(0xFFFFB74D)
        val onWarning = Color(0xFFE65100)
        val warningContainer = Color(0xFFF57C00)
        val onWarningContainer = Color(0xFFFFE0B2)
        
        val info = Color(0xFF64B5F6)
        val onInfo = Color(0xFF0D47A1)
        val infoContainer = Color(0xFF1976D2)
        val onInfoContainer = Color(0xFFBBDEFB)
        
        // Music-specific colors
        val activeNote = Color(0xFFD0BCFF)
        val inactiveNote = Color(0xFF49454F)
        val keyboardWhiteKey = Color(0xFFE6E1E5)
        val keyboardBlackKey = Color(0xFF1C1B1F)
    }
    
    /**
     * Gradient colors for visual effects
     */
    object Gradients {
        val primaryGradient = listOf(
            Color(0xFF6750A4),
            Color(0xFF7D5260)
        )
        
        val secondaryGradient = listOf(
            Color(0xFF625B71),
            Color(0xFF7D5260)
        )
        
        val playbackGradient = listOf(
            Color(0xFF6750A4),
            Color(0xFF4F378B),
            Color(0xFF381E72)
        )
    }
    
    /**
     * Opacity values for consistent transparency
     */
    object Alpha {
        const val disabled = 0.38f
        const val medium = 0.60f
        const val high = 0.87f
        const val full = 1.0f
    }
}
