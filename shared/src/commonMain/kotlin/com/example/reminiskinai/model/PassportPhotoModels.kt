package com.example.reminiskinai.model

import org.jetbrains.compose.resources.DrawableResource
import reminiskinai.shared.generated.resources.Res
import reminiskinai.shared.generated.resources.men_1_navy_suit
import reminiskinai.shared.generated.resources.men_2_charcoal_suit
import reminiskinai.shared.generated.resources.men_3_grey_blazer
import reminiskinai.shared.generated.resources.men_4_black_formal
import reminiskinai.shared.generated.resources.men_5_brown_open_collar
import reminiskinai.shared.generated.resources.women_1_navy_blazer
import reminiskinai.shared.generated.resources.women_2_black_suit
import reminiskinai.shared.generated.resources.women_3_beige_blazer
import reminiskinai.shared.generated.resources.women_4_burgundy_blazer
import reminiskinai.shared.generated.resources.women_5_grey_blazer

/**
 * Standard Passport & Visa Photo Sizes by Country (calibrated at 300 DPI print standard).
 * Follows biometric ICAO & HivisionIDPhotos geometric specifications.
 */
enum class CountryPassportStandard(
    val countryName: String,
    val flagEmoji: String,
    val specLabel: String,
    val widthMm: Float,
    val heightMm: Float,
    val widthPx300Dpi: Int,
    val heightPx300Dpi: Int,
    val targetHeadHeightRatio: Float,   // Expected head height (crown-to-chin) / photo height
    val targetCrownTopRatio: Float,     // Expected top-of-hair distance from top edge / photo height
    val defaultBgColor: PassportBgColor = PassportBgColor.PURE_WHITE
) {
    US_2X2(
        countryName = "United States (US)",
        flagEmoji = "🇺🇸",
        specLabel = "2×2 in (51×51 mm)",
        widthMm = 50.8f,
        heightMm = 50.8f,
        widthPx300Dpi = 600,
        heightPx300Dpi = 600,
        targetHeadHeightRatio = 0.58f,
        targetCrownTopRatio = 0.12f
    ),
    UK_EU_35X45(
        countryName = "UK / EU / Schengen",
        flagEmoji = "🇪🇺",
        specLabel = "35×45 mm",
        widthMm = 35f,
        heightMm = 45f,
        widthPx300Dpi = 413,
        heightPx300Dpi = 531,
        targetHeadHeightRatio = 0.68f,
        targetCrownTopRatio = 0.10f
    ),
    BANGLADESH_40X50(
        countryName = "Bangladesh Passport",
        flagEmoji = "🇧🇩",
        specLabel = "40×50 mm (Lab 1.5×1.9\")",
        widthMm = 40f,
        heightMm = 50f,
        widthPx300Dpi = 472,
        heightPx300Dpi = 591,
        targetHeadHeightRatio = 0.65f,
        targetCrownTopRatio = 0.10f
    ),
    INDIA_2X2(
        countryName = "India Passport / OCI",
        flagEmoji = "🇮🇳",
        specLabel = "2×2 in (51×51 mm)",
        widthMm = 50.8f,
        heightMm = 50.8f,
        widthPx300Dpi = 600,
        heightPx300Dpi = 600,
        targetHeadHeightRatio = 0.60f,
        targetCrownTopRatio = 0.11f
    ),
    JAPAN_35X45(
        countryName = "Japan Passport / Visa",
        flagEmoji = "🇯🇵",
        specLabel = "35×45 mm",
        widthMm = 35f,
        heightMm = 45f,
        widthPx300Dpi = 413,
        heightPx300Dpi = 531,
        targetHeadHeightRatio = 0.68f,
        targetCrownTopRatio = 0.09f
    ),
    CANADA_50X70(
        countryName = "Canada Passport",
        flagEmoji = "🇨🇦",
        specLabel = "50×70 mm (2×2.75 in)",
        widthMm = 50f,
        heightMm = 70f,
        widthPx300Dpi = 591,
        heightPx300Dpi = 827,
        targetHeadHeightRatio = 0.52f,
        targetCrownTopRatio = 0.12f
    ),
    CHINA_33X48(
        countryName = "China Passport / Visa",
        flagEmoji = "🇨🇳",
        specLabel = "33×48 mm",
        widthMm = 33f,
        heightMm = 48f,
        widthPx300Dpi = 390,
        heightPx300Dpi = 567,
        targetHeadHeightRatio = 0.64f,
        targetCrownTopRatio = 0.10f
    ),
    AUSTRALIA_35X45(
        countryName = "Australia Passport",
        flagEmoji = "🇦🇺",
        specLabel = "35×45 mm",
        widthMm = 35f,
        heightMm = 45f,
        widthPx300Dpi = 413,
        heightPx300Dpi = 531,
        targetHeadHeightRatio = 0.70f,
        targetCrownTopRatio = 0.09f
    ),
    UAE_SAUDI_40X60(
        countryName = "UAE / Saudi / Gulf",
        flagEmoji = "🇦🇪",
        specLabel = "40×60 mm",
        widthMm = 40f,
        heightMm = 60f,
        widthPx300Dpi = 472,
        heightPx300Dpi = 709,
        targetHeadHeightRatio = 0.60f,
        targetCrownTopRatio = 0.11f
    ),
    MALAYSIA_SG_35X50(
        countryName = "Malaysia / Singapore",
        flagEmoji = "🇲🇾",
        specLabel = "35×50 mm",
        widthMm = 35f,
        heightMm = 50f,
        widthPx300Dpi = 413,
        heightPx300Dpi = 591,
        targetHeadHeightRatio = 0.64f,
        targetCrownTopRatio = 0.10f
    );

    val aspectRatio: Float
        get() = widthPx300Dpi.toFloat() / heightPx300Dpi.toFloat()
}

/**
 * Standard Passport & ID Photo Background Colors.
 */
enum class PassportBgColor(
    val title: String,
    val hexCode: String,
    val r: Int,
    val g: Int,
    val b: Int,
    val rBottom: Int = r,
    val gBottom: Int = g,
    val bBottom: Int = b
) {
    PURE_WHITE(
        title = "Pure White",
        hexCode = "#FFFFFF",
        r = 255, g = 255, b = 255
    ),
    STUDIO_IVORY(
        title = "Studio Off-White",
        hexCode = "#F5F6F8",
        r = 248, g = 249, b = 251,
        rBottom = 238, gBottom = 240, bBottom = 245
    ),
    LIGHT_BLUE(
        title = "Passport Sky Blue",
        hexCode = "#CBE6FF",
        r = 212, g = 235, b = 255,
        rBottom = 186, gBottom = 221, bBottom = 252
    ),
    ROYAL_BLUE(
        title = "Official ID Blue",
        hexCode = "#438EDB",
        r = 74, g = 148, b = 224,
        rBottom = 56, gBottom = 128, bBottom = 204
    ),
    NAVY_BLUE(
        title = "Deep Navy Blue",
        hexCode = "#1D4E89",
        r = 35, g = 86, b = 148,
        rBottom = 24, gBottom = 66, bBottom = 120
    ),
    BIOMETRIC_GREY(
        title = "Biometric Grey",
        hexCode = "#E4E6EB",
        r = 232, g = 234, b = 239,
        rBottom = 216, gBottom = 219, bBottom = 226
    ),
    OFFICIAL_RED(
        title = "Official ID Red",
        hexCode = "#D62828",
        r = 218, g = 44, b = 44,
        rBottom = 192, gBottom = 30, bBottom = 30
    ),
    WARM_CREAM(
        title = "Warm Cream",
        hexCode = "#FDF7E7",
        r = 253, g = 247, b = 231,
        rBottom = 245, gBottom = 236, bBottom = 214
    )
}

enum class CoatGenderCategory(val label: String) {
    ALL("All (10)"),
    MEN("Men (5)"),
    WOMEN("Women (5)")
}

/**
 * Metadata for each of the 10 formal coat/suit templates.
 */
data class CoatTemplateItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: CoatGenderCategory,
    val drawable: DrawableResource,
    val badgeColorHex: Long,
    val defaultScaleFactor: Float = 1.0f,
    val defaultCollarYOffsetNorm: Float = 0.0f
)

object PassportCoatCatalog {
    val templates: List<CoatTemplateItem> = listOf(
        // 5 Men's Formal Coats / Suits
        CoatTemplateItem(
            id = "men_1_navy_suit",
            title = "Navy Tie Suit",
            subtitle = "Men · Royal Blue Tie",
            category = CoatGenderCategory.MEN,
            drawable = Res.drawable.men_1_navy_suit,
            badgeColorHex = 0xFF1E3A8A
        ),
        CoatTemplateItem(
            id = "men_2_charcoal_suit",
            title = "Executive Charcoal",
            subtitle = "Men · Crimson Silk Tie",
            category = CoatGenderCategory.MEN,
            drawable = Res.drawable.men_2_charcoal_suit,
            badgeColorHex = 0xFF991B1B
        ),
        CoatTemplateItem(
            id = "men_3_grey_blazer",
            title = "Slate Grey Suit",
            subtitle = "Men · Ice Shirt & Navy Tie",
            category = CoatGenderCategory.MEN,
            drawable = Res.drawable.men_3_grey_blazer,
            badgeColorHex = 0xFF475569
        ),
        CoatTemplateItem(
            id = "men_4_black_formal",
            title = "Classic Jet Black",
            subtitle = "Men · Black Formal Tie",
            category = CoatGenderCategory.MEN,
            drawable = Res.drawable.men_4_black_formal,
            badgeColorHex = 0xFF18181B
        ),
        CoatTemplateItem(
            id = "men_5_brown_open_collar",
            title = "Espresso Blazer",
            subtitle = "Men · Open White Collar",
            category = CoatGenderCategory.MEN,
            drawable = Res.drawable.men_5_brown_open_collar,
            badgeColorHex = 0xFF78350F
        ),

        // 5 Women's Formal Coats / Blazers
        CoatTemplateItem(
            id = "women_1_navy_blazer",
            title = "Navy V-Blazer",
            subtitle = "Women · Silk V-Blouse",
            category = CoatGenderCategory.WOMEN,
            drawable = Res.drawable.women_1_navy_blazer,
            badgeColorHex = 0xFF1D4ED8
        ),
        CoatTemplateItem(
            id = "women_2_black_suit",
            title = "Black Executive",
            subtitle = "Women · Pointed White Collar",
            category = CoatGenderCategory.WOMEN,
            drawable = Res.drawable.women_2_black_suit,
            badgeColorHex = 0xFF27272A
        ),
        CoatTemplateItem(
            id = "women_3_beige_blazer",
            title = "Cream Tailored",
            subtitle = "Women · Soft Beige Blazer",
            category = CoatGenderCategory.WOMEN,
            drawable = Res.drawable.women_3_beige_blazer,
            badgeColorHex = 0xFFB45309
        ),
        CoatTemplateItem(
            id = "women_4_burgundy_blazer",
            title = "Burgundy Formal",
            subtitle = "Women · Maroon Jacket",
            category = CoatGenderCategory.WOMEN,
            drawable = Res.drawable.women_4_burgundy_blazer,
            badgeColorHex = 0xFF881337
        ),
        CoatTemplateItem(
            id = "women_5_grey_blazer",
            title = "Charcoal Lapel",
            subtitle = "Women · Ivory Blouse",
            category = CoatGenderCategory.WOMEN,
            drawable = Res.drawable.women_5_grey_blazer,
            badgeColorHex = 0xFF52525B
        )
    )
}

enum class TransformTargetMode(val title: String) {
    PHOTO("Crop / Rotate Photo"),
    COAT("Fit / Align Coat")
}

enum class ExportLayoutMode(val title: String, val subtitle: String) {
    SINGLE_PASSPORT("Single Passport Photo", "Exact 300 DPI biometric digital & print file"),
    PRINT_SHEET_4X6("4×6\" Print Sheet (Multi-Copy)", "Standard 10×15 cm lab sheet with cut guides")
}

/**
 * Real-time interactive parameters for Passport Photo Studio.
 */
data class PassportEditParams(
    val countryStandard: CountryPassportStandard = CountryPassportStandard.US_2X2,
    val bgColor: PassportBgColor = PassportBgColor.PURE_WHITE,
    val selectedCoatId: String? = null,
    val genderFilter: CoatGenderCategory = CoatGenderCategory.ALL,
    val transformTarget: TransformTargetMode = TransformTargetMode.PHOTO,

    // Photo Crop, Scale & Pan
    val photoScale: Float = 1.0f,
    val photoRotationDeg: Float = 0.0f,
    val photoOffsetXNorm: Float = 0.0f,
    val photoOffsetYNorm: Float = 0.0f,
    val autoLevelEyesEnabled: Boolean = true,

    // Coat Scale & Offset
    val coatScale: Float = 1.0f,
    val coatRotationDeg: Float = 0.0f,
    val coatOffsetXNorm: Float = 0.0f,
    val coatOffsetYNorm: Float = 0.0f,
    val hideOriginalClothesUnderCoat: Boolean = true,

    val studioSkinPolish: Boolean = true,
    val showBiometricGuides: Boolean = false,
    val exportLayoutMode: ExportLayoutMode = ExportLayoutMode.SINGLE_PASSPORT
)

/**
 * Extracted biometric geometry information.
 */
data class PassportBiometricGeometry(
    val leftEyeX: Float = 0.42f,
    val leftEyeY: Float = 0.38f,
    val rightEyeX: Float = 0.58f,
    val rightEyeY: Float = 0.38f,
    val crownTopY: Float = 0.15f,
    val chinBottomY: Float = 0.65f,
    val headCenterX: Float = 0.50f,
    val neckCenterX: Float = 0.50f,
    val neckCollarY: Float = 0.72f,
    val activeModels: List<String> = listOf(
        "Hivision MODNet (AI Background Matte)",
        "BiSeNet 19-Class Portrait Segmenter",
        "MediaPipe Biometric Eye & Face Mesh",
        "Smart Collar Alignment Anchor"
    ),
    val inferenceTimeMs: Long = 142
)
