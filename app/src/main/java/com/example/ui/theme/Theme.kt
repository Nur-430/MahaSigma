package com.example.ui.theme
 
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class AppThemeMode(val title: String, val subtitle: String) {
    SYSTEM("Ikuti Sistem", "Menyesuaikan otomatis dengan tema perangkat"),
    LIGHT("Mode Terang", "Latar bersih cerah, teks tajam & kontras tinggi"),
    DARK("Mode Gelap", "Tampilan Code-Editor yang teduh & hemat baterai")
}

data class MahaSigmaThemeColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val border: Color,
    val borderSubtle: Color,
    val accentPrimary: Color,
    val isDark: Boolean
)

val LocalMahaSigmaColors = staticCompositionLocalOf {
    MahaSigmaThemeColors(
        background = InkBlack,
        surface = PrussianBlue,
        surfaceVariant = SurfaceDarkVariant,
        textPrimary = AlabasterGrey,
        textSecondary = DustyDenim,
        border = DuskBlue,
        borderSubtle = Color(0xFF2E4057),
        accentPrimary = ElectricCyan,
        isDark = true
    )
}

object MahaTheme {
    val colors: MahaSigmaThemeColors
        @Composable
        get() = LocalMahaSigmaColors.current
}

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
    primary = DeepOceanBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = SlateTextPrimary,
    secondary = DuskBlue,
    onSecondary = Color.White,
    secondaryContainer = SlateCardVariant,
    onSecondaryContainer = SlateTextPrimary,
    tertiary = NeonEmerald,
    onTertiary = Color.White,
    background = SlateLightBg,
    onBackground = SlateTextPrimary,
    surface = WhiteSurface,
    onSurface = SlateTextPrimary,
    surfaceVariant = SlateCardVariant,
    onSurfaceVariant = SlateTextSecondary,
    outline = SlateBorder,
    outlineVariant = SlateBorderSubtle,
    error = CrimsonRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) MahaSigmaDarkColorScheme else MahaSigmaLightColorScheme

    val customColors = if (darkTheme) {
        MahaSigmaThemeColors(
            background = InkBlack,
            surface = PrussianBlue,
            surfaceVariant = SurfaceDarkVariant,
            textPrimary = AlabasterGrey,
            textSecondary = DustyDenim,
            border = DuskBlue,
            borderSubtle = Color(0xFF2E4057),
            accentPrimary = ElectricCyan,
            isDark = true
        )
    } else {
        MahaSigmaThemeColors(
            background = SlateLightBg,
            surface = WhiteSurface,
            surfaceVariant = SlateCardVariant,
            textPrimary = SlateTextPrimary,
            textSecondary = SlateTextSecondary,
            border = SlateBorder,
            borderSubtle = SlateBorderSubtle,
            accentPrimary = DeepOceanBlue,
            isDark = false
        )
    }

    CompositionLocalProvider(LocalMahaSigmaColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun MahaSigmaTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) = MyApplicationTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)


