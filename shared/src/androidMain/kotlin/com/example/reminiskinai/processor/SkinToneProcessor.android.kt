package com.example.reminiskinai.processor

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.reminiskinai.model.AestheticPreset
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

actual object SkinToneProcessor {

    actual fun applySkinToneGrading(
        source: ImageBitmap,
        preset: AestheticPreset,
        warmth: Float,
        radiance: Float
    ): ImageBitmap {
        val srcBmp = source.asAndroidBitmap()
        val width = srcBmp.width
        val height = srcBmp.height

        val pixels = IntArray(width * height)
        srcBmp.getPixels(pixels, 0, width, 0, 0, width, height)

        val (pWarmth, pRMult, pGMult, pBMult, pRadBoost) = when (preset) {
            AestheticPreset.REMINI_NATURAL -> {
                Tuple5(warmth * 1.00f, 1.03f, 1.00f, 0.97f, 18f)
            }
            AestheticPreset.BANGLADESHI -> {
                Tuple5(warmth * 1.06f, 1.07f, 1.03f, 0.92f, 22f)
            }
            AestheticPreset.KOREAN -> {
                Tuple5(warmth * 0.94f, 0.97f, 0.99f, 1.03f, 26f)
            }
            AestheticPreset.AMERICAN -> {
                Tuple5(warmth * 1.08f, 1.08f, 1.02f, 0.93f, 20f)
            }
            AestheticPreset.AFRICAN -> {
                Tuple5(warmth * 1.12f, 1.12f, 1.04f, 0.88f, 28f)
            }
        }

        val effRMult = pRMult * pWarmth
        val effGMult = pGMult
        val effBMult = (pBMult / pWarmth).coerceIn(0.70f, 1.25f)
        val radOffset = (radiance - 1.0f) * pRadBoost

        for (i in pixels.indices) {
            val c = pixels[i]
            val r = (c shr 16) and 0xFF
            val g = (c shr 8) and 0xFF
            val b = c and 0xFF

            // YCrCb color space dermis detection
            val cr = 128f + 0.500f * r - 0.419f * g - 0.081f * b
            val cb = 128f - 0.169f * r - 0.331f * g + 0.500f * b

            // Check if within human skin spectrum (Cr in 130..175, Cb in 75..130)
            if (cr < 130f || cr > 175f || cb < 75f || cb > 130f || r <= g || (r - g) < 6) {
                // Background, clothing, hair, eyes, etc. - 100% untouched!
                continue
            }

            // Calculate soft skin weight using elliptical distance
            val dCr = (cr - 152f) / 22f
            val dCb = (cb - 105f) / 25f
            val distSq = dCr * dCr + dCb * dCb
            if (distSq >= 1.5f) {
                continue
            }
            val skinWeight = (1.0f - (distSq / 1.5f)).coerceIn(0f, 1f)

            // Calculate graded skin RGB
            val rGraded = (r * effRMult + radOffset).coerceIn(0f, 255f)
            val gGraded = (g * effGMult + radOffset * 0.70f).coerceIn(0f, 255f)
            val bGraded = (b * effBMult + radOffset * 0.40f).coerceIn(0f, 255f)

            // Blend gently with original pixel
            val rFinal = (r * (1f - skinWeight) + rGraded * skinWeight).roundToInt().coerceIn(0, 255)
            val gFinal = (g * (1f - skinWeight) + gGraded * skinWeight).roundToInt().coerceIn(0, 255)
            val bFinal = (b * (1f - skinWeight) + bGraded * skinWeight).roundToInt().coerceIn(0, 255)

            pixels[i] = (c and -0x1000000) or (rFinal shl 16) or (gFinal shl 8) or bFinal
        }

        val outBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        outBmp.setPixels(pixels, 0, width, 0, 0, width, height)
        return outBmp.asImageBitmap()
    }

    actual fun enhancePhoto(
        source: ImageBitmap,
        scaleMultiplier: Int
    ): ImageBitmap {
        val srcBmp = source.asAndroidBitmap()
        val originalW = srcBmp.width
        val originalH = srcBmp.height

        val targetW = originalW * scaleMultiplier.coerceAtLeast(1)
        val targetH = originalH * scaleMultiplier.coerceAtLeast(1)

        val workingBmp = if (targetW != originalW || targetH != originalH) {
            Bitmap.createScaledBitmap(srcBmp, targetW, targetH, true)
        } else if (srcBmp.config != Bitmap.Config.ARGB_8888) {
            srcBmp.copy(Bitmap.Config.ARGB_8888, true)
        } else {
            srcBmp.copy(Bitmap.Config.ARGB_8888, true)
        }

        val w = workingBmp.width
        val h = workingBmp.height
        val inPixels = IntArray(w * h)
        val outPixels = IntArray(w * h)
        workingBmp.getPixels(inPixels, 0, w, 0, 0, w, h)

        val sharpenAmount = 0.45f
        for (y in 0 until h) {
            val row = y * w
            val yPrev = max(0, y - 1) * w
            val yNext = min(h - 1, y + 1) * w

            for (x in 0 until w) {
                val idx = row + x
                val centerColor = inPixels[idx]
                val a = (centerColor shr 24) and 0xFF
                val r = (centerColor shr 16) and 0xFF
                val g = (centerColor shr 8) and 0xFF
                val b = centerColor and 0xFF

                val xPrev = max(0, x - 1)
                val xNext = min(w - 1, x + 1)

                val cTop = inPixels[yPrev + x]
                val cBottom = inPixels[yNext + x]
                val cLeft = inPixels[row + xPrev]
                val cRight = inPixels[row + xNext]

                val avgR = (((cTop shr 16) and 0xFF) + ((cBottom shr 16) and 0xFF) +
                        ((cLeft shr 16) and 0xFF) + ((cRight shr 16) and 0xFF)) * 0.25f
                val avgG = (((cTop shr 8) and 0xFF) + ((cBottom shr 8) and 0xFF) +
                        ((cLeft shr 8) and 0xFF) + ((cRight shr 8) and 0xFF)) * 0.25f
                val avgB = ((cTop and 0xFF) + (cBottom and 0xFF) +
                        (cLeft and 0xFF) + (cRight and 0xFF)) * 0.25f

                val highFreqR = r - avgR
                val highFreqG = g - avgG
                val highFreqB = b - avgB

                val sharpR = (r + highFreqR * sharpenAmount).roundToInt().coerceIn(0, 255)
                val sharpG = (g + highFreqG * sharpenAmount).roundToInt().coerceIn(0, 255)
                val sharpB = (b + highFreqB * sharpenAmount).roundToInt().coerceIn(0, 255)

                outPixels[idx] = (a shl 24) or (sharpR shl 16) or (sharpG shl 8) or sharpB
            }
        }

        val resultBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        resultBmp.setPixels(outPixels, 0, w, 0, 0, w, h)
        return resultBmp.asImageBitmap()
    }

    private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
}
