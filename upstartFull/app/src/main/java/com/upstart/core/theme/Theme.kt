package com.upstart.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Local provider for custom typography.
 * Colors are provided by MaterialTheme.colorScheme naturally.
 */
val LocalUpstartTypography = staticCompositionLocalOf { Typography }

/**
 * Upstart theme object that provides access to theme values.
 * Colors are accessed via MaterialTheme.colorScheme (standard Material 3 approach).
 * Typography is custom and accessed via UpstartTheme.typography.
 */
object UpstartTheme {
    /**
     * Access typography values.
     * Example: UpstartTheme.typography.headlineMedium
     */
    val typography: UpstartTypography
        @Composable
        get() = LocalUpstartTypography.current
}

/**
 * Upstart theme composable that configures Material 3's theme with Upstart branding.
 * Use MaterialTheme.colorScheme to access colors (standard Material 3 approach).
 * Use UpstartTheme.typography to access typography.
 *
 * Example usage:
 * ```
 * UpstartTheme {
 *     Text(
 *         text = "Hello",
 *         color = MaterialTheme.colorScheme.primary,
 *         style = UpstartTheme.typography.headlineSmall
 *     )
 * }
 * ```
 */
@Composable
fun UpstartTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme
    ) {
        CompositionLocalProvider(
            LocalUpstartTypography provides Typography,
            content = content
        )
    }
}
