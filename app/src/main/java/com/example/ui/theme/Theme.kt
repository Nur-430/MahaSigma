package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MahaSigmaDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = InkBlack,
    primaryContainer = DuskBlue,
    onPrimaryContainer = AlabasterGrey,
    secondary = DustyDenim,
    onSecondary = InkBlack,
    secondaryContainer = PrussianBlue,
    onSecondaryContainer = AlabasterGrey,
    tertiary = NeonEmerald,
    onTertiary = InkBlack,
    background = InkBlack,
    onBackground = AlabasterGrey,
    surface = PrussianBlue,
    onSurface = AlabasterGrey,
    surfaceVariant = SurfaceDarkVariant,
    onSurfaceVariant = DustyDenim,
    outline = DuskBlue,
    outlineVariant = Color(0xFF2E4057),
    error = CrimsonRed,
    onError = Color.White
)

private val MahaSigmaLightColorScheme = lightColorScheme(
    primary = DuskBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E2F0),
    onPrimaryContainer = InkBlack,
    secondary = PrussianBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E8F0),
    onSecondaryContainer = PrussianBlue,
    tertiary = NeonEmerald,
    onTertiary = Color.White,
    background = Color(0xFFF4F6F9),
    onBackground = InkBlack,
    surface = Color.White,
    onSurface = InkBlack,
    surfaceVariant = Color(0xFFEDF2F7),
    onSurfaceVariant = DustyDenim,
    outline = DustyDenim,
    error = CrimsonRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Code-editor dark aesthetic prioritized by default
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) MahaSigmaDarkColorScheme else MahaSigmaLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MahaSigmaTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) = MyApplicationTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)

