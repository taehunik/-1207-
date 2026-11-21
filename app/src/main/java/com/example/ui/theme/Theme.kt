package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val StudyDarkColorScheme = darkColorScheme(
    primary = NightPrimary,
    onPrimary = NightOnPrimary,
    primaryContainer = NightPrimaryContainer,
    onPrimaryContainer = NightOnPrimaryContainer,
    secondary = NightSecondary,
    onSecondary = NightOnSecondary,
    secondaryContainer = NightSecondaryContainer,
    onSecondaryContainer = NightOnSecondaryContainer,
    tertiary = NightTertiary,
    onTertiary = NightOnTertiary,
    tertiaryContainer = NightTertiaryContainer,
    onTertiaryContainer = NightOnTertiaryContainer,
    background = NightBackground,
    onBackground = NightOnBackground,
    surface = NightSurface,
    onSurface = NightOnSurface,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = NightOnSurfaceVariant,
    outline = NightOutline,
    outlineVariant = NightOutlineVariant,
    error = ErrorDark
)

private val StudyLightColorScheme = lightColorScheme(
    primary = CobaltPrimary,
    onPrimary = CobaltOnPrimary,
    primaryContainer = CobaltPrimaryContainer,
    onPrimaryContainer = CobaltOnPrimaryContainer,
    secondary = EmeraldSecondary,
    onSecondary = EmeraldOnSecondary,
    secondaryContainer = EmeraldSecondaryContainer,
    onSecondaryContainer = EmeraldOnSecondaryContainer,
    tertiary = CoralTertiary,
    onTertiary = CoralOnTertiary,
    tertiaryContainer = CoralTertiaryContainer,
    onTertiaryContainer = CoralOnTertiaryContainer,
    background = PaperBackground,
    onBackground = PaperOnBackground,
    surface = PaperSurface,
    onSurface = PaperOnSurface,
    surfaceVariant = PaperSurfaceVariant,
    onSurfaceVariant = PaperOnSurfaceVariant,
    outline = PaperOutline,
    outlineVariant = PaperOutlineVariant,
    error = ErrorLight
)

val StudyPlannerShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun StudyPlannerTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) StudyDarkColorScheme else StudyLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = StudyPlannerTypography,
        shapes = StudyPlannerShapes,
        content = content
    )
}
