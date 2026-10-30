package com.example.reminiskinai.ml

import android.graphics.Bitmap

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
 * Default is PURE_WHITE (#FFFFFF) per international biometric requirements.
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
 * Metadata for each of the 10 formal coat/suit templates generated with Nano Banana 2.
 */
data class CoatTemplateItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: CoatGenderCategory,
    val assetPath: String,
    val badgeColorHex: Long,
    val defaultScaleFactor: Float = 1.0f,
    val defaultCollarYOffsetNorm: Float = 0.0f
)

object PassportCoatCatalog {
    val templates: List<CoatTemplateItem> = listOf(
        // 5 Men's Formal Coats / Suits (Generated with Nano Banana 2)
        CoatTemplateItem(
            id = "men_1_navy_suit",
            title = "Navy Tie Suit",
            subtitle = "Men · Royal Blue Tie",
            category = CoatGenderCategory.MEN,
            assetPath = "coats/men_1_navy_suit.png",
            badgeColorHex = 0xFF1E3A8A
        ),
        CoatTemplateItem(
            id = "men_2_charcoal_suit",
            title = "Executive Charcoal",
            subtitle = "Men · Crimson Silk Tie",
            category = CoatGenderCategory.MEN,
            assetPath = "coats/men_2_charcoal_suit.png",
            badgeColorHex = 0xFF991B1B
        ),
        CoatTemplateItem(
            id = "men_3_grey_blazer",
            title = "Slate Grey Suit",
            subtitle = "Men · Ice Shirt & Navy Tie",
            category = CoatGenderCategory.MEN,
            assetPath = "coats/men_3_grey_blazer.png",
            badgeColorHex = 0xFF475569
        ),
        CoatTemplateItem(
            id = "men_4_black_formal",
            title = "Classic Jet Black",
            subtitle = "Men · Black Formal Tie",
            category = CoatGenderCategory.MEN,
            assetPath = "coats/men_4_black_formal.png",
            badgeColorHex = 0xFF18181B
        ),
        CoatTemplateItem(
            id = "men_5_brown_open_collar",
            title = "Espresso Blazer",
            subtitle = "Men · Open White Collar",
            category = CoatGenderCategory.MEN,
            assetPath = "coats/men_5_brown_open_collar.png",
            badgeColorHex = 0xFF78350F
        ),

        // 5 Women's Formal Coats / Blazers (Generated with Nano Banana 2)
        CoatTemplateItem(
            id = "women_1_navy_blazer",
            title = "Navy V-Blazer",
            subtitle = "Women · Silk V-Blouse",
            category = CoatGenderCategory.WOMEN,
            assetPath = "coats/women_1_navy_blazer.png",
            badgeColorHex = 0xFF1D4ED8
        ),
        CoatTemplateItem(
            id = "women_2_black_suit",
            title = "Black Executive",
            subtitle = "Women · Pointed White Collar",
            category = CoatGenderCategory.WOMEN,
            assetPath = "coats/women_2_black_suit.png",
            badgeColorHex = 0xFF27272A
        ),
        CoatTemplateItem(
            id = "women_3_beige_blazer",
            title = "Cream Tailored",
            subtitle = "Women · Soft Beige Blazer",
            category = CoatGenderCategory.WOMEN,
            assetPath = "coats/women_3_beige_blazer.png",
            badgeColorHex = 0xFFB45309
        ),
        CoatTemplateItem(
            id = "women_4_burgundy_blazer",
            title = "Burgundy Formal",
            subtitle = "Women · Maroon Jacket",
            category = CoatGenderCategory.WOMEN,
            assetPath = "coats/women_4_burgundy_blazer.png",
            badgeColorHex = 0xFF881337
        ),
        CoatTemplateItem(
            id = "women_5_grey_blazer",
            title = "Charcoal Lapel",
            subtitle = "Women · Ivory Blouse",
            category = CoatGenderCategory.WOMEN,
            assetPath = "coats/women_5_grey_blazer.png",
            badgeColorHex = 0xFF52525B
        )
    )
}

/**
 * Loaded coat bitmap + automatically detected collar geometry in coat bitmap pixel coordinates.
 */
data class LoadedCoatAsset(
    val item: CoatTemplateItem,
    val bitmap: Bitmap,
    val collarCenterX: Float,       // X center of neck opening in coat bitmap pixels
    val collarJunctionY: Float,     // Y where chin/neck tucks into the shirt collar in coat bitmap pixels
    val collarOpeningWidth: Float,  // Inner width of collar opening in coat bitmap pixels
    val shoulderSpanWidth: Float,   // Full width of shoulders in coat bitmap pixels
    val holeTopY: Float = 0f,       // Top Y of the interior neck opening in coat bitmap pixels
    val holeBottomY: Float = 0f,    // Bottom Y of the V-neck / tie-knot apex in coat bitmap pixels
    val regionMap: ByteArray = ByteArray(0) // 0=outer, 1=neck V-opening, 2=back collar band, 3=front garment
)

enum class TransformTargetMode(val title: String) {
    PHOTO("Crop / Rotate Photo"),
    COAT("Fit / Align Coat")
}

enum class ExportLayoutMode(val title: String, val subtitle: String) {
    SINGLE_PASSPORT("Single Passport Photo", "Exact 300 DPI biometric digital & print file"),
    PRINT_SHEET_4X6("4×6\" Print Sheet (Multi-Copy)", "Standard 10×15 cm lab sheet with cut guides")
}

/**
 * Real-time interactive parameters for the Passport Photo Studio.
 */
data class PassportEditParams(
    val countryStandard: CountryPassportStandard = CountryPassportStandard.US_2X2,
    val bgColor: PassportBgColor = PassportBgColor.PURE_WHITE,
    val selectedCoatId: String? = null, // null = Original Existing Clothes (100% Authentic Fit, No Coat Overlay)
    val genderFilter: CoatGenderCategory = CoatGenderCategory.ALL,
    val transformTarget: TransformTargetMode = TransformTargetMode.PHOTO,

    // Photo / Head Crop, Scale & Rotation adjustments (relative to AI Auto-Fit baseline)
    val photoScale: Float = 1.0f,           // 0.55f .. 1.85f
    val photoRotationDeg: Float = 0.0f,     // -25f .. +25f (added on top of auto-eye-leveling)
    val photoOffsetXNorm: Float = 0.0f,     // -0.45f .. +0.45f (fraction of canvas width)
    val photoOffsetYNorm: Float = 0.0f,     // -0.45f .. +0.45f (fraction of canvas height)
    val autoLevelEyesEnabled: Boolean = true,

    // Coat / Suit Scale, Rotation & X/Y Fit adjustments (relative to AI Neck-Collar anchor)
    val coatScale: Float = 1.0f,            // 0.65f .. 1.50f
    val coatRotationDeg: Float = 0.0f,      // -15f .. +15f
    val coatOffsetXNorm: Float = 0.0f,      // -0.30f .. +0.30f (fraction of canvas width)
    val coatOffsetYNorm: Float = 0.0f,      // -0.30f .. +0.30f (fraction of canvas height)
    val hideOriginalClothesUnderCoat: Boolean = true,

    // Edge & Skin Enhancement options
    val alphaEdgeFeather: Float = 0.55f,    // 0.0f .. 1.0f
    val studioSkinPolish: Boolean = true,   // Subtle studio lighting & skin evening
    val showBiometricGuides: Boolean = false,
    val exportLayoutMode: ExportLayoutMode = ExportLayoutMode.SINGLE_PASSPORT
)

/**
 * Extracted biometric geometry from BiSeNet 19-Class + Hivision MODNet + MediaPipe 6-Class.
 */
data class PassportBiometricGeometry(
    val leftEyeX: Float,
    val leftEyeY: Float,
    val rightEyeX: Float,
    val rightEyeY: Float,
    val noseX: Float,
    val noseY: Float,
    val mouthX: Float,
    val mouthY: Float,
    val crownTopY: Float,           // Top of hair/head in source image pixels
    val chinBottomY: Float,         // Bottom of chin in source image pixels
    val headCenterX: Float,         // Horizontal center of head in source image pixels
    val headWidthPx: Float,         // Temple/ear-to-ear head width in source image pixels
    val neckCenterX: Float,         // Horizontal center of neck base in source image pixels
    val neckCollarY: Float,         // Y coordinate where shirt collar sits on the neck
    val neckWidthPx: Float,         // Width of neck in source image pixels
    val autoEyeTiltDeg: Float,      // Counter-rotation angle in degrees to level the eyes
    val activeModels: List<String>,
    val inferenceTimeMs: Long
)

/**
 * Cached session produced by [PassportPhotoEngine.analyzePortrait] so changing background color,
 * country size, coat selection, or crop/rotate/scale sliders recomposes in ~15-25ms!
 */
class CachedPassportSession(
    val sourceBitmap: Bitmap,
    val width: Int,
    val height: Int,
    val origPixels: IntArray,
    val polishedPixels: IntArray,       // Studio-balanced skin pixels for optional passport polish
    val personAlphaMatte: FloatArray,   // [width * height] Hivision MODNet + BiSeNet + MediaPipe alpha (0..1)
    val clothesMask: FloatArray,        // [width * height] Original clothes probability (0..1) for clean coat swap
    val headAndNeckMask: FloatArray,    // [width * height] Protected hair + face + ears + neck mask (0..1)
    val skinMask: FloatArray,           // [width * height] True skin probability mask (0..1)
    val neckRefRgb: FloatArray,         // [3] Sampled RGB skin tone of user's chin/neck for seamless V-neck fill
    val geometry: PassportBiometricGeometry
)
