package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AcademicLightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = CardSurface,
    primaryContainer = PrimaryBlueLight,
    onPrimaryContainer = PrimaryBlueDark,

    secondary = AcademicTeal,
    onSecondary = CardSurface,
    secondaryContainer = AcademicTealLight,
    onSecondaryContainer = AcademicTealDark,

    tertiary = SkyBlue,
    onTertiary = TextPrimary,
    tertiaryContainer = SkyBlueLight,
    onTertiaryContainer = PrimaryBlueDark,

    background = AcademicBackground,
    onBackground = TextPrimary,

    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = AcademicBackground,
    onSurfaceVariant = TextSecondary,

    outline = SurfaceBorder,
    outlineVariant = SurfaceBorder,

    error = AcademicError,
    onError = CardSurface,
    errorContainer = AcademicErrorBg,
    onErrorContainer = AcademicError
)

@Composable
fun GrammarFlowTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AcademicLightColorScheme,
        typography = Typography,
        content = content
    )
}
