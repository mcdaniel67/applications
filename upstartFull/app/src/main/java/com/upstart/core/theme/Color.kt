package com.upstart.core.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Upstart brand colors used throughout the app.
 * These are the raw color values that feed into Material 3's ColorScheme.
 */
object UpstartColors {
    // Brand Colors
    val PrimaryTeal = Color(0xFF00807B)
    val SecondaryTeal = Color(0xFF00B1AC)
    val LightTealBackground = Color(0xFFEFFAFB)

    // Accent Colors
    val Gold = Color(0xFFE6B658)
    val DarkGold = Color(0xFFE2AB0A)
    val BrightYellow = Color(0xFFFFCC33)

    // Neutral Colors
    val DarkCharcoal = Color(0xFF222929)
    val MediumGray = Color(0xFF757575)
    val White = Color(0xFFFFFFFF)

    // Functional Colors
    val TextDark = Color(0xFF013F39)
    val ErrorRed = Color(0xFFDC3545)
    val ErrorLight = Color(0xFFFF6B6B)

    // Surface Colors
    val SurfaceLight = Color(0xFFFFFFFF)
    val SurfaceDark = Color(0xFF2C3333)
    val SurfaceVariantLight = Color(0xFFF5F5F5)
    val SurfaceVariantDark = Color(0xFF3A4040)
}

/**
 * Light color scheme using Material 3's lightColorScheme builder.
 * This integrates naturally with Material 3 components.
 */
val LightColorScheme = lightColorScheme(
    primary = UpstartColors.PrimaryTeal,
    onPrimary = UpstartColors.White,
    secondary = UpstartColors.SecondaryTeal,
    onSecondary = UpstartColors.White,
    tertiary = UpstartColors.Gold,
    onTertiary = UpstartColors.DarkCharcoal,
    background = UpstartColors.SurfaceLight,
    onBackground = UpstartColors.DarkCharcoal,
    surface = UpstartColors.SurfaceLight,
    onSurface = UpstartColors.DarkCharcoal,
    surfaceVariant = UpstartColors.SurfaceVariantLight,
    onSurfaceVariant = UpstartColors.MediumGray,
    error = UpstartColors.ErrorRed,
    onError = UpstartColors.White
)

/**
 * Dark color scheme using Material 3's darkColorScheme builder.
 * This integrates naturally with Material 3 components.
 */
val DarkColorScheme = darkColorScheme(
    primary = UpstartColors.SecondaryTeal,
    onPrimary = UpstartColors.DarkCharcoal,
    secondary = UpstartColors.PrimaryTeal,
    onSecondary = UpstartColors.White,
    tertiary = UpstartColors.BrightYellow,
    onTertiary = UpstartColors.DarkCharcoal,
    background = UpstartColors.DarkCharcoal,
    onBackground = UpstartColors.White,
    surface = UpstartColors.SurfaceDark,
    onSurface = UpstartColors.White,
    surfaceVariant = UpstartColors.SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFCCCCCC),
    error = UpstartColors.ErrorLight,
    onError = UpstartColors.DarkCharcoal
)
