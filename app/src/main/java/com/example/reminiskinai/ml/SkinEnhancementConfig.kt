package com.example.reminiskinai.ml

import android.graphics.Bitmap

/**
 * Regional & Aesthetic skin-tone presets calibrated from Remini's natural portrait color grading
 * and Zero-DCE rational curve parameters: C_out = C_in + alpha * C_in * (1 - C_in),
 * paired with region-harmonized natural lip color curves (keeping G ≈ B for natural rosy/coral lips).
 */
enum class AestheticPreset(
    val title: String,
    val subtitle: String,
    val alphaR: Float,
    val alphaG: Float,
    val alphaB: Float,
    val warmthBoost: Float,
    val contrastLift: Float,
    val lipAlphaR: Float,
    val lipAlphaG: Float,
    val lipAlphaB: Float
) {
    REMINI_NATURAL(
        title = "Remini Natural",
        subtitle = "Original Remini natural skin & rosy lip balance",
        alphaR = 0.585f,
        alphaG = 0.355f,
        alphaB = 0.250f,
        warmthBoost = 1.00f,
        contrastLift = 1.00f,
        lipAlphaR = 0.680f,
        lipAlphaG = 0.200f,
        lipAlphaB = 0.280f
    ),
    BANGLADESHI(
        title = "Bangladeshi",
        subtitle = "South Asian warm golden-wheat glow & natural rose lips",
        alphaR = 0.610f,
        alphaG = 0.370f,
        alphaB = 0.245f,
        warmthBoost = 1.06f,
        contrastLift = 1.02f,
        lipAlphaR = 0.700f,
        lipAlphaG = 0.190f,
        lipAlphaB = 0.270f
    ),
    KOREAN(
        title = "Korean",
        subtitle = "K-Beauty luminous peach glass skin & coral-pink lips",
        alphaR = 0.575f,
        alphaG = 0.435f,
        alphaB = 0.380f,
        warmthBoost = 0.86f,
        contrastLift = 0.98f,
        lipAlphaR = 0.730f,
        lipAlphaG = 0.180f,
        lipAlphaB = 0.310f
    ),
    AMERICAN(
        title = "American",
        subtitle = "Sun-kissed warm neutral tan & berry-rose lips",
        alphaR = 0.620f,
        alphaG = 0.365f,
        alphaB = 0.225f,
        warmthBoost = 1.12f,
        contrastLift = 1.04f,
        lipAlphaR = 0.660f,
        lipAlphaG = 0.200f,
        lipAlphaB = 0.265f
    ),
    AFRICAN(
        title = "African",
        subtitle = "Rich melanin-preserving bronze glow & warm berry lips",
        alphaR = 0.545f,
        alphaG = 0.315f,
        alphaB = 0.185f,
        warmthBoost = 1.16f,
        contrastLift = 1.06f,
        lipAlphaR = 0.620f,
        lipAlphaG = 0.180f,
        lipAlphaB = 0.250f
    )
}

/**
 * User-selectable Lip Shade presets that adapt to the user's natural 3D lip shading & gloss.
 */
enum class LipShadePreset(
    val title: String,
    val swatchHex: Long,
    val targetR: Float,
    val targetG: Float,
    val targetB: Float,
    val alphaR: Float,
    val alphaG: Float,
    val alphaB: Float,
    val tintMix: Float,
    val keepCoolGBBalance: Boolean
) {
    NATURAL(
        title = "Natural Match",
        swatchHex = 0xFFE06D78,
        targetR = 172f,
        targetG = 94f,
        targetB = 98f,
        alphaR = 0.68f,
        alphaG = 0.20f,
        alphaB = 0.28f,
        tintMix = 0.00f,
        keepCoolGBBalance = true
    ),
    ROSE_PETAL(
        title = "Rose Petal",
        swatchHex = 0xFFD95872,
        targetR = 188f,
        targetG = 82f,
        targetB = 108f,
        alphaR = 0.72f,
        alphaG = 0.15f,
        alphaB = 0.30f,
        tintMix = 0.58f,
        keepCoolGBBalance = true
    ),
    PEACH_CORAL(
        title = "Peach Coral",
        swatchHex = 0xFFE66A56,
        targetR = 202f,
        targetG = 96f,
        targetB = 80f,
        alphaR = 0.76f,
        alphaG = 0.25f,
        alphaB = 0.16f,
        tintMix = 0.58f,
        keepCoolGBBalance = false
    ),
    BERRY_PLUM(
        title = "Berry Plum",
        swatchHex = 0xFFA6365E,
        targetR = 148f,
        targetG = 54f,
        targetB = 90f,
        alphaR = 0.46f,
        alphaG = -0.05f,
        alphaB = 0.24f,
        tintMix = 0.64f,
        keepCoolGBBalance = true
    ),
    CLASSIC_RED(
        title = "Classic Red",
        swatchHex = 0xFFC82838,
        targetR = 188f,
        targetG = 46f,
        targetB = 58f,
        alphaR = 0.75f,
        alphaG = -0.08f,
        alphaB = -0.02f,
        tintMix = 0.66f,
        keepCoolGBBalance = true
    ),
    WARM_NUDE(
        title = "Warm Nude",
        swatchHex = 0xFFB86658,
        targetR = 168f,
        targetG = 96f,
        targetB = 86f,
        alphaR = 0.56f,
        alphaG = 0.22f,
        alphaB = 0.18f,
        tintMix = 0.54f,
        keepCoolGBBalance = false
    )
}

/**
 * User-selectable Hair Color Shade presets using strand-preserving YCrCb luminance/chroma curves.
 */
enum class HairShadePreset(
    val title: String,
    val swatchHex: Long,
    val lumaLift: Float,
    val midtoneLift: Float,
    val crShift: Float,
    val cbShift: Float,
    val baseChromaKeep: Float
) {
    ORIGINAL(
        title = "Original",
        swatchHex = 0xFF2B2624,
        lumaLift = 0.00f,
        midtoneLift = 0.00f,
        crShift = 0.0f,
        cbShift = 0.0f,
        baseChromaKeep = 1.0f
    ),
    JET_BLACK(
        title = "Jet Black",
        swatchHex = 0xFF141418,
        lumaLift = -0.28f,
        midtoneLift = -0.12f,
        crShift = -2.5f,
        cbShift = 2.5f,
        baseChromaKeep = 0.35f
    ),
    ESPRESSO_BROWN(
        title = "Espresso",
        swatchHex = 0xFF3B2418,
        lumaLift = 0.14f,
        midtoneLift = 0.22f,
        crShift = 9.5f,
        cbShift = -10.5f,
        baseChromaKeep = 0.80f
    ),
    CHESTNUT_WARM(
        title = "Chestnut",
        swatchHex = 0xFF6E3822,
        lumaLift = 0.24f,
        midtoneLift = 0.36f,
        crShift = 17.5f,
        cbShift = -17.5f,
        baseChromaKeep = 0.75f
    ),
    AUBURN_COPPER(
        title = "Auburn",
        swatchHex = 0xFF8E3320,
        lumaLift = 0.26f,
        midtoneLift = 0.40f,
        crShift = 26.0f,
        cbShift = -20.0f,
        baseChromaKeep = 0.70f
    ),
    BURGUNDY_WINE(
        title = "Burgundy",
        swatchHex = 0xFF5E1D34,
        lumaLift = 0.18f,
        midtoneLift = 0.28f,
        crShift = 22.0f,
        cbShift = 5.0f,
        baseChromaKeep = 0.65f
    ),
    GOLDEN_BLONDE(
        title = "Honey Gold",
        swatchHex = 0xFFB58338,
        lumaLift = 0.38f,
        midtoneLift = 0.52f,
        crShift = 13.5f,
        cbShift = -26.0f,
        baseChromaKeep = 0.65f
    ),
    ASH_SILVER(
        title = "Ash Silver",
        swatchHex = 0xFF7A8088,
        lumaLift = 0.30f,
        midtoneLift = 0.42f,
        crShift = -4.5f,
        cbShift = 5.5f,
        baseChromaKeep = 0.22f
    )
}

/**
 * User-controllable parameters for real-time recomposition after the 5 neural network models run.
 * Can be saved by the user as their default setting and automatically applied next time.
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
    val skinSmoothness: Float = 0.85f,     // 0.0f .. 1.0f (GPEN GAN + YCrCb multi-scale surface polish)
    val lipColorIntensity: Float = 1.00f,  // 0.0f .. 1.5f (Adaptive natural lip color & shade intensity)
    val hairColorIntensity: Float = 0.85f, // 0.0f .. 1.2f (Strand-preserving neural hair shade intensity)
    val skinRadiance: Float = 1.00f,       // 0.0f .. 1.5f (Zero-DCE + rational curve intensity)
    val skinWarmth: Float = 1.00f,         // 0.5f .. 1.5f (Chromaticity balance & cast removal)
    val facialClarity: Float = 0.82f       // 0.0f .. 1.2f (GPEN + high-frequency 1080p HD eye/brow/lip clarity)
)

/**
 * Live progress state emitted while the 5 on-device AI models + 1080p HD upscaler execute.
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
    val faceSkinPercent: Float,
    val bodySkinPercent: Float,
    val beforeSkinRgb: IntArray,
    val afterSkinRgb: IntArray,
    val outputWidth: Int = 1080,
    val outputHeight: Int = 1440,
    val inferenceTimeMs: Long,
    val activeModels: List<String>
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
 * Built-in sample item representing the user's Remini before/after screenshots.
 */
data class SamplePhotoItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val beforeAssetPath: String,
    val reminiRefAssetPath: String? = null
)

/**
 * Holds cached intermediate neural network feature maps at 1080p HD resolution for instant (<50ms) slider adjustments.
 */
class CachedSkinSession(
    val originalBitmap: Bitmap,
    val reminiRefBitmap: Bitmap?,
    val width: Int,
    val height: Int,
    val origPixels: IntArray,
    val faceSkinMask: FloatArray,     // [width * height] in 0..1 (fused BiSeNet + MediaPipe face dermis)
    val bodySkinMask: FloatArray,     // [width * height] in 0..1 (MediaPipe body skin: neck, arms, hands)
    val detailMask: FloatArray,       // [width * height] in 0..1 (BiSeNet eyes, brows)
    val lipMask: FloatArray,          // [width * height] in 0..1 (BiSeNet + FFHQ chromaticity lips)
    val hairMask: FloatArray,         // [width * height] in 0..1 (Fused BiSeNet class 17 + MediaPipe class 1 hair)
    val dceLightField: FloatArray,    // [width * height] in 0..1 (Zero-DCE CNN spatial luminance gain)
    val vdsrResidualY: FloatArray,    // [width * height] in -0.5..0.5 (High-frequency feature detail delta)
    val gpenFaceR: FloatArray,        // [width * height] GPEN-BFR-256 GAN restored 1080p face R (0..255)
    val gpenFaceG: FloatArray,        // [width * height] GPEN-BFR-256 GAN restored 1080p face G (0..255)
    val gpenFaceB: FloatArray,        // [width * height] GPEN-BFR-256 GAN restored 1080p face B (0..255)
    val gpenFaceMask: FloatArray,     // [width * height] in 0..1 aligned FFHQ face blend mask
    val smoothLowFreqR: FloatArray,   // [width * height] YCrCb multi-scale smoothed dermis R (0..255)
    val smoothLowFreqG: FloatArray,   // [width * height] YCrCb multi-scale smoothed dermis G (0..255)
    val smoothLowFreqB: FloatArray,   // [width * height] YCrCb multi-scale smoothed dermis B (0..255)
    val baseSkinMedianRgb: FloatArray,// [R, G, B] median of detected skin before editing (0..255)
    val baseLipMedianRgb: FloatArray, // [R, G, B] median of detected lips before editing (0..255)
    val baseInferenceTimeMs: Long,
    val activeModels: List<String>
)
