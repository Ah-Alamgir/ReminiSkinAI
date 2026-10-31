package com.example.reminiskinai.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =========================================================================
// Luminescent Glass Retouch Design Tokens (from DESIGN.md)
// =========================================================================

// Obsidian Base & Surface Containers
val AuraSurfaceLowest = Color(0xFF0C0E18)
val AuraSurfaceDim = Color(0xFF11131D)
val AuraSurface = Color(0xFF11131D)
val AuraSurfaceContainerLow = Color(0xFF191B26)
val AuraSurfaceContainer = Color(0xFF1D1F2A)
val AuraSurfaceContainerHigh = Color(0xFF282934)
val AuraSurfaceContainerHighest = Color(0xFF32343F)
val AuraSurfaceBright = Color(0xFF373844)

// On-Surface Typography & Content
val AuraOnSurface = Color(0xFFE1E1F0)
val AuraOnSurfaceVariant = Color(0xFFCBC3D7)
val AuraInverseSurface = Color(0xFFE1E1F0)
val AuraInverseOnSurface = Color(0xFF2E303B)

// Outlines & Borders
val AuraOutline = Color(0xFF958EA0)
val AuraOutlineVariant = Color(0xFF494454)
val AuraGlassBorderLight = Color(0x47FFFFFF) // 28% white top specular rim
val AuraGlassBorderSubtle = Color(0x1FFFFFFF) // 12% white base glass border
val AuraGlassBorderFade = Color(0x0AFFFFFF) // 4% white bottom shadow border

// Primary Spectrum: Electric Violet
val AuraPrimary = Color(0xFFD0BCFF)
val AuraPrimaryElectric = Color(0xFF8B5CF6)
val AuraPrimaryContainer = Color(0xFFA078FF)
val AuraOnPrimary = Color(0xFF3C0091)
val AuraOnPrimaryContainer = Color(0xFF340080)
val AuraInversePrimary = Color(0xFF6D3BD7)
val AuraPrimaryFixed = Color(0xFFE9DDFF)
val AuraPrimaryFixedDim = Color(0xFFD0BCFF)

// Secondary Spectrum: Luminous Cyan
val AuraSecondary = Color(0xFF4CD7F6)
val AuraSecondaryLuminous = Color(0xFF06B6D4)
val AuraSecondaryContainer = Color(0xFF03B5D3)
val AuraOnSecondary = Color(0xFF003640)
val AuraOnSecondaryContainer = Color(0xFF00424E)
val AuraSecondaryFixed = Color(0xFFACEDFF)
val AuraSecondaryFixedDim = Color(0xFF4CD7F6)

// Tertiary Spectrum: Hot Magenta & Rose
val AuraTertiary = Color(0xFFFFB0CD)
val AuraTertiaryHotMagenta = Color(0xFFEC4899)
val AuraTertiaryContainer = Color(0xFFF751A1)
val AuraTertiaryFixed = Color(0xFFFFD9E4)
val AuraTertiaryFixedDim = Color(0xFFFFB0CD)
val AuraOnTertiary = Color(0xFF640039)
val AuraOnTertiaryContainer = Color(0xFF570032)

// Error Tokens
val AuraError = Color(0xFFFFB4AB)
val AuraOnError = Color(0xFF690005)
val AuraErrorContainer = Color(0xFF93000A)
val AuraOnErrorContainer = Color(0xFFFFDAD6)

// Glass Backgrounds
val AuraGlassBaseBg = Color(0xA6131627) // rgba(19, 22, 39, 0.65)
val AuraGlassCardBg = Color(0x0DFFFFFF) // rgba(255, 255, 255, 0.05)
val AuraGlassElevatedBg = Color(0x14FFFFFF) // rgba(255, 255, 255, 0.08)
val AuraGlassPillActiveBg = Color(0x2E06B6D4) // rgba(6, 182, 212, 0.18)

// Signature Gradients
val AuraHeroButtonGradient = Brush.linearGradient(
    listOf(
        Color(0xFFA078FF),
        Color(0xFF8B5CF6),
        Color(0xFFEC4899)
    )
)

val AuraExportButtonGradient = Brush.linearGradient(
    listOf(
        Color(0xFF8B5CF6),
        Color(0xFFA078FF),
        Color(0xFFEC4899)
    )
)

val AuraCyanGradient = Brush.linearGradient(
    listOf(
        Color(0xFF06B6D4),
        Color(0xFF4CD7F6)
    )
)

val AuraVioletCyanGradient = Brush.linearGradient(
    listOf(
        Color(0xFFA078FF),
        Color(0xFF03B5D3)
    )
)

val AuraGlassRimGradient = Brush.verticalGradient(
    listOf(
        Color.White.copy(alpha = 0.28f),
        Color.White.copy(alpha = 0.04f)
    )
)

// Legacy alias compatibility
val ReminiBlack = AuraSurfaceLowest
val ReminiSurface = AuraSurfaceContainer
val ReminiCard = AuraSurfaceContainerHigh
val ReminiCrimson = AuraTertiaryContainer
val ReminiRose = AuraTertiaryHotMagenta
val ReminiGold = AuraSecondary
val ReminiTextPrimary = AuraOnSurface
val ReminiTextSecondary = AuraOnSurfaceVariant
val ReminiDivider = AuraOutlineVariant
