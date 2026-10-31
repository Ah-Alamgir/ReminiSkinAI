package com.example.reminiskinai.processor

import androidx.compose.ui.graphics.ImageBitmap
import com.example.reminiskinai.model.AestheticPreset

expect object SkinToneProcessor {
    /**
     * Applies regional Remini-calibrated skin tone grading selectively to dermis/skin regions.
     * Pixels outside the human skin spectrum (background, clothes, hair, sky) are 100% preserved.
     */
    fun applySkinToneGrading(
        source: ImageBitmap,
        preset: AestheticPreset,
        warmth: Float = 1.0f,
        radiance: Float = 1.0f
    ): ImageBitmap

    /**
     * Enhances an original photo with high-frequency detail restoration, contrast curve optimization,
     * micro-texture clarity, and skin dermis smoothing.
     */
    fun enhancePhoto(
        source: ImageBitmap,
        scaleMultiplier: Int = 1
    ): ImageBitmap
}
