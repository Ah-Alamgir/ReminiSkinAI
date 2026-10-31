package com.example.reminiskinai.processor

import androidx.compose.ui.graphics.ImageBitmap
import com.example.reminiskinai.model.AestheticPreset

actual object SkinToneProcessor {

    actual fun applySkinToneGrading(
        source: ImageBitmap,
        preset: AestheticPreset,
        warmth: Float,
        radiance: Float
    ): ImageBitmap {
        return source
    }

    actual fun enhancePhoto(
        source: ImageBitmap,
        scaleMultiplier: Int
    ): ImageBitmap {
        return source
    }
}
