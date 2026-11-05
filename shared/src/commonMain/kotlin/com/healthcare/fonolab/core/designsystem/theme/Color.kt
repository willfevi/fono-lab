package com.healthcare.fonolab.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Mockup Modern Pastel & Obsidian Pill Palette
val MockupDarkPill = Color(0xFF13151B)
val MockupBackground = Color(0xFFF7F7FA)
val MockupSurface = Color(0xFFFFFFFF)
val MockupBorder = Color(0xFFECEBF2)

// Mockup Pastel Block Tones
val MockupPurpleHero = Color(0xFFBEB0F6)
val MockupPurpleHeroEnd = Color(0xFFAEA0F0)
val MockupYellowCard = Color(0xFFFDC058)
val MockupYellowCardLight = Color(0xFFFFF4DE)
val MockupBlueCard = Color(0xFFA3C7F7)
val MockupBlueCardLight = Color(0xFFE8F1FD)
val MockupGreenCard = Color(0xFFC3EFC9)
val MockupGreenCardLight = Color(0xFFEEFAEF)
val MockupCyanCard = Color(0xFFBAE6FD)
val MockupCyanCardLight = Color(0xFFE5F5FE)
val MockupPeachCard = Color(0xFFFED7AA)
val MockupPeachCardLight = Color(0xFFFFF2E2)
val MockupPinkCard = Color(0xFFF472B6)
val MockupPinkCardLight = Color(0xFFFCE7F1)

// High-contrast clean text colors matching mockup
val MockupTextDark = Color(0xFF15171E)
val MockupTextMuted = Color(0xFF7A7D8D)
val MockupTextSubtle = Color(0xFFA5A8B6)

// Digital Health Lilac & Pure White Palette (Preserving existing references)
val FonoPrimary = Color(0xFF8B6FE8)        // Base Lilac
val FonoPrimaryDark = Color(0xFF7052CC)    // Deep Lilac
val FonoPrimaryLight = Color(0xFFF0EBFF)   // Soft Lilac container
val FonoPrimarySoft = Color(0xFFF7F4FF)    // Subtlest lilac tint
val FonoOnPrimary = Color(0xFFFFFFFF)

// Gradient for primary CTA buttons
val FonoPrimaryGradientStart = Color(0xFFA18AF5)
val FonoPrimaryGradientEnd = Color(0xFF7859D9)
val FonoPrimaryGradient = Brush.horizontalGradient(
    colors = listOf(FonoPrimaryGradientStart, FonoPrimaryGradientEnd)
)

val FonoSecondary = Color(0xFF2EA58D)      // Balanced clinical teal for success/done
val FonoOnSecondary = Color(0xFFFFFFFF)
val FonoSecondaryContainer = Color(0xFFE4F7F2)
val FonoOnSecondaryContainer = Color(0xFF0F5A4B)

val FonoTertiary = Color(0xFF5E7CE2)       // Serene slate blue for accents
val FonoOnTertiary = Color(0xFFFFFFFF)
val FonoTertiaryContainer = Color(0xFFEBF0FE)
val FonoOnTertiaryContainer = Color(0xFF1E3574)

val FonoBackground = MockupBackground
val FonoSurface = MockupSurface
val FonoSurfaceVariant = Color(0xFFF2F0F7)
val FonoBorder = MockupBorder

val FonoTextPrimary = MockupTextDark
val FonoTextSecondary = MockupTextMuted
val FonoTextTertiary = MockupTextSubtle

// Semantic Feedback
val FonoSuccess = Color(0xFF238652)
val FonoSuccessContainer = Color(0xFFE6F5EC)
val FonoWarning = Color(0xFFD97706)
val FonoWarningContainer = Color(0xFFFEF3C7)
val FonoError = Color(0xFFDC2626)
val FonoErrorContainer = Color(0xFFFEE2E2)
val FonoOnErrorContainer = Color(0xFF7F1D1D)

val LightColorScheme = lightColorScheme(
    primary = FonoPrimary,
    onPrimary = FonoOnPrimary,
    primaryContainer = FonoPrimaryLight,
    onPrimaryContainer = FonoPrimaryDark,
    secondary = FonoSecondary,
    onSecondary = FonoOnSecondary,
    secondaryContainer = FonoSecondaryContainer,
    onSecondaryContainer = FonoOnSecondaryContainer,
    tertiary = FonoTertiary,
    onTertiary = FonoOnTertiary,
    tertiaryContainer = FonoTertiaryContainer,
    onTertiaryContainer = FonoOnTertiaryContainer,
    background = MockupBackground,
    onBackground = MockupTextDark,
    surface = MockupSurface,
    onSurface = MockupTextDark,
    surfaceVariant = FonoSurfaceVariant,
    onSurfaceVariant = MockupTextMuted,
    outline = MockupBorder,
    error = FonoError,
    errorContainer = FonoErrorContainer,
    onErrorContainer = FonoOnErrorContainer
)

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA48CF4),
    onPrimary = Color(0xFF2A166A),
    primaryContainer = Color(0xFF452B8C),
    onPrimaryContainer = Color(0xFFEDE7FF),
    secondary = Color(0xFF5CD2B8),
    onSecondary = Color(0xFF063A30),
    secondaryContainer = Color(0xFF165A4B),
    onSecondaryContainer = Color(0xFFC7F8ED),
    tertiary = Color(0xFF96B3FE),
    onTertiary = Color(0xFF142C67),
    background = Color(0xFF14131A),
    onBackground = Color(0xFFE9E7F0),
    surface = Color(0xFF1C1A24),
    onSurface = Color(0xFFE9E7F0),
    surfaceVariant = Color(0xFF2A2735),
    onSurfaceVariant = Color(0xFFA9A6B8),
    outline = Color(0xFF383547),
    error = Color(0xFFF87171)
)
