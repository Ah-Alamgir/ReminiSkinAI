package com.example.reminiskinai.model

import org.jetbrains.compose.resources.DrawableResource

/**
 * Regional & Aesthetic skin-tone presets calibrated from Remini's natural portrait color grading.
 */
enum class AestheticPreset(
    val title: String,
    val subtitle: String,
    val swatchColor: Long,
    val warmthBoost: Float,
    val contrastLift: Float,
    val saturationBoost: Float
) {
    REMINI_NATURAL(
        title = "Remini Natural",
        subtitle = "Signature studio clarity & healthy rosy-peach glow (Default)",
        swatchColor = 0xFFF5D0C0,
        warmthBoost = 1.00f,
        contrastLift = 1.03f,
        saturationBoost = 1.00f
    ),
    AMERICAN(
        title = "American Fair White",
        subtitle = "Fair creamy-white porcelain with neutral undertones",
        swatchColor = 0xFFFCEFE4,
        warmthBoost = 1.00f,
        contrastLift = 1.02f,
        saturationBoost = 1.00f
    ),
    KOREAN(
        title = "Korean Glass Skin",
        subtitle = "K-Beauty translucent ivory & luminous peach highlight",
        swatchColor = 0xFFFFF1EB,
        warmthBoost = 0.96f,
        contrastLift = 1.02f,
        saturationBoost = 1.00f
    ),
    AFRICAN(
        title = "African Deep Melanin",
        subtitle = "Authentic rich black/ebony with velvety luster",
        swatchColor = 0xFF4A3026,
        warmthBoost = 1.00f,
        contrastLift = 1.06f,
        saturationBoost = 1.02f
    ),
    BANGLADESHI(
        title = "South Asian Golden",
        subtitle = "Warm golden-wheat glow & rich amber undertones",
        swatchColor = 0xFFC68A5E,
        warmthBoost = 1.05f,
        contrastLift = 1.03f,
        saturationBoost = 1.02f
    ),
    LATINA_OLIVE(
        title = "Latina Olive Bronze",
        subtitle = "Sun-kissed warm olive & radiant honey bronze",
        swatchColor = 0xFFD29B6C,
        warmthBoost = 1.06f,
        contrastLift = 1.04f,
        saturationBoost = 1.04f
    ),
    NORDIC_FAIR(
        title = "Nordic Alabaster",
        subtitle = "Ultra-bright cool alabaster & crystal clarity",
        swatchColor = 0xFFFFF5F0,
        warmthBoost = 0.98f,
        contrastLift = 1.02f,
        saturationBoost = 1.00f
    ),
    MEDITERRANEAN(
        title = "Mediterranean Sun",
        subtitle = "Golden warm terracotta & sunlit radiance",
        swatchColor = 0xFFDC9E75,
        warmthBoost = 1.07f,
        contrastLift = 1.04f,
        saturationBoost = 1.03f
    )
}

/**
 * User-selectable Lip Shade presets.
 */
enum class LipShadePreset(
    val title: String,
    val swatchHex: Long,
    val tintR: Float,
    val tintG: Float,
    val tintB: Float,
    val tintStrength: Float
) {
    NATURAL(
        title = "Natural Match",
        swatchHex = 0xFFE06D78,
        tintR = 0.92f,
        tintG = 0.55f,
        tintB = 0.58f,
        tintStrength = 0.00f
    ),
    ROSE_PETAL(
        title = "Rose Petal",
        swatchHex = 0xFFD95872,
        tintR = 0.95f,
        tintG = 0.45f,
        tintB = 0.58f,
        tintStrength = 0.35f
    ),
    PEACH_CORAL(
        title = "Peach Coral",
        swatchHex = 0xFFE66A56,
        tintR = 1.00f,
        tintG = 0.52f,
        tintB = 0.42f,
        tintStrength = 0.35f
    ),
    BERRY_PLUM(
        title = "Berry Plum",
        swatchHex = 0xFFA6365E,
        tintR = 0.78f,
        tintG = 0.28f,
        tintB = 0.48f,
        tintStrength = 0.40f
    ),
    CLASSIC_RED(
        title = "Classic Red",
        swatchHex = 0xFFC82838,
        tintR = 0.98f,
        tintG = 0.22f,
        tintB = 0.28f,
        tintStrength = 0.45f
    ),
    WARM_NUDE(
        title = "Warm Nude",
        swatchHex = 0xFFB86658,
        tintR = 0.86f,
        tintG = 0.56f,
        tintB = 0.50f,
        tintStrength = 0.30f
    )
}

/**
 * User-selectable Hair Color Shade presets.
 */
enum class HairShadePreset(
    val title: String,
    val swatchHex: Long,
    val lumaLift: Float,
    val tintR: Float,
    val tintG: Float,
    val tintB: Float
) {
    ORIGINAL(
        title = "Original",
        swatchHex = 0xFF2B2624,
        lumaLift = 0.00f,
        tintR = 1.0f,
        tintG = 1.0f,
        tintB = 1.0f
    ),
    JET_BLACK(
        title = "Jet Black",
        swatchHex = 0xFF141418,
        lumaLift = -0.25f,
        tintR = 0.85f,
        tintG = 0.85f,
        tintB = 0.92f
    ),
    ESPRESSO_BROWN(
        title = "Espresso",
        swatchHex = 0xFF3B2418,
        lumaLift = 0.12f,
        tintR = 1.15f,
        tintG = 0.95f,
        tintB = 0.82f
    ),
    CHESTNUT_WARM(
        title = "Chestnut",
        swatchHex = 0xFF6E3822,
        lumaLift = 0.20f,
        tintR = 1.25f,
        tintG = 0.90f,
        tintB = 0.75f
    ),
    AUBURN_COPPER(
        title = "Auburn",
        swatchHex = 0xFF8E3320,
        lumaLift = 0.22f,
        tintR = 1.35f,
        tintG = 0.82f,
        tintB = 0.70f
    ),
    BURGUNDY_WINE(
        title = "Burgundy",
        swatchHex = 0xFF5E1D34,
        lumaLift = 0.15f,
        tintR = 1.20f,
        tintG = 0.70f,
        tintB = 0.95f
    ),
    GOLDEN_BLONDE(
        title = "Honey Gold",
        swatchHex = 0xFFB58338,
        lumaLift = 0.35f,
        tintR = 1.30f,
        tintG = 1.15f,
        tintB = 0.75f
    ),
    ASH_SILVER(
        title = "Ash Silver",
        swatchHex = 0xFF7A8088,
        lumaLift = 0.28f,
        tintR = 0.95f,
        tintG = 1.00f,
        tintB = 1.10f
    )
}

/**
 * User-controllable parameters for skin editing & recomposition.
 */
data class SkinEditParams(
    val skinColorEnabled: Boolean = true,
    val faceRetouchEnabled: Boolean = true,
    val faceEnhancerEnabled: Boolean = true,
    val bodySkinSyncEnabled: Boolean = true,
    val showSkinMaskOverlay: Boolean = false,
    val compareReminiRef: Boolean = false,
    val preset: AestheticPreset = AestheticPreset.REMINI_NATURAL,
    val lipShade: LipShadePreset = LipShadePreset.NATURAL,
    val hairShade: HairShadePreset = HairShadePreset.ORIGINAL,
    val skinSmoothness: Float = 0.85f,     // 0.0f .. 1.0f
    val lipColorIntensity: Float = 1.00f,  // 0.0f .. 1.5f
    val hairColorIntensity: Float = 0.85f, // 0.0f .. 1.2f
    val skinRadiance: Float = 1.00f,       // 0.0f .. 1.5f
    val skinWarmth: Float = 1.00f,         // 0.5f .. 1.5f
    val facialClarity: Float = 0.82f       // 0.0f .. 1.2f
)

/**
 * Live progress state emitted while the 5 on-device AI models execute.
 */
data class PipelineStageProgress(
    val stepIndex: Int,
    val totalSteps: Int = 5,
    val modelName: String,
    val stageDescription: String,
    val progressFraction: Float
)

/**
 * Quantitative telemetry of the detected skin regions, resolution, and color balance shift.
 */
data class SkinAnalysisTelemetry(
    val faceSkinPercent: Float = 16.4f,
    val bodySkinPercent: Float = 18.2f,
    val beforeSkinRgb: IntArray = intArrayOf(218, 178, 162),
    val afterSkinRgb: IntArray = intArrayOf(226, 186, 171),
    val outputWidth: Int = 1080,
    val outputHeight: Int = 1440,
    val inferenceTimeMs: Long = 186,
    val activeModels: List<String> = listOf(
        "Real-ESRGAN x4v3 (1080p Upscaler)",
        "BiSeNet 19-Class Face & Skin Segmenter",
        "MediaPipe Selfie Multi-Class Mask",
        "GPEN-BFR-256 Face Restoration GAN",
        "Zero-DCE + YCrCb Rational Retouch"
    )
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SkinAnalysisTelemetry) return false
        return faceSkinPercent == other.faceSkinPercent &&
                bodySkinPercent == other.bodySkinPercent &&
                beforeSkinRgb.contentEquals(other.beforeSkinRgb) &&
                afterSkinRgb.contentEquals(other.afterSkinRgb) &&
                outputWidth == other.outputWidth &&
                outputHeight == other.outputHeight &&
                inferenceTimeMs == other.inferenceTimeMs &&
                activeModels == other.activeModels
    }

    override fun hashCode(): Int {
        var result = faceSkinPercent.hashCode()
        result = 31 * result + bodySkinPercent.hashCode()
        result = 31 * result + beforeSkinRgb.contentHashCode()
        result = 31 * result + afterSkinRgb.contentHashCode()
        result = 31 * result + outputWidth
        result = 31 * result + outputHeight
        result = 31 * result + inferenceTimeMs.hashCode()
        result = 31 * result + activeModels.hashCode()
        return result
    }
}

/**
 * Built-in sample item representing portrait presets.
 */
data class SamplePhotoItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val beforeDrawable: DrawableResource,
    val reminiRefDrawable: DrawableResource? = null
)
