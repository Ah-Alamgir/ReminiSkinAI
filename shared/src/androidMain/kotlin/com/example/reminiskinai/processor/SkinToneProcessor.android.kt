package com.example.reminiskinai.processor

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.reminiskinai.model.AestheticPreset
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

actual object SkinToneProcessor {

    actual fun applySkinToneGrading(
        source: ImageBitmap,
        preset: AestheticPreset,
        warmth: Float,
        radiance: Float,
        smoothness: Float,
        clarity: Float
    ): ImageBitmap {
        val srcBmp = source.asAndroidBitmap()
        val width = srcBmp.width
        val height = srcBmp.height

        val pixels = IntArray(width * height)
        srcBmp.getPixels(pixels, 0, width, 0, 0, width, height)

        val (pRMult, pGMult, pBMult, pRadBoost, pContrast) = when (preset) {
            AestheticPreset.REMINI_NATURAL -> {
                // Remini Natural: Fresh clean tones, rosy-peach dermis, balanced vibrancy
                Tuple5(1.10f, 1.02f, 0.94f, 22f, 1.04f)
            }
            AestheticPreset.BANGLADESHI -> {
                // Bangladeshi: South Asian warm golden wheat glow, rich amber undertones
                Tuple5(1.22f, 1.10f, 0.82f, 30f, 1.06f)
            }
            AestheticPreset.KOREAN -> {
                // Korean: K-Beauty luminous porcelain glass skin, bright peachy highlight, lower yellow
                Tuple5(1.05f, 1.03f, 1.10f, 35f, 1.03f)
            }
            AestheticPreset.AMERICAN -> {
                // American: Sun-kissed golden bronze tan, California warm radiance
                Tuple5(1.24f, 1.06f, 0.82f, 26f, 1.08f)
            }
            AestheticPreset.AFRICAN -> {
                // African: Rich deep melanin bronze luster, radiant velvety glow
                Tuple5(1.26f, 1.06f, 0.80f, 32f, 1.10f)
            }
        }

        val effWarmth = warmth.coerceIn(0.4f, 1.8f)
        val effRMult = pRMult * effWarmth
        val effGMult = pGMult * ((effWarmth - 1.0f) * 0.35f + 1.0f)
        val effBMult = (pBMult / effWarmth).coerceIn(0.55f, 1.40f)
        val radOffset = ((radiance - 1.0f) * pRadBoost).coerceIn(-40f, 60f)

        // Stage 1: Soft Dermis Segmentation across all human skin spectra
        val skinWeights = FloatArray(width * height)
        for (i in pixels.indices) {
            val c = pixels[i]
            val r = (c shr 16) and 0xFF
            val g = (c shr 8) and 0xFF
            val b = c and 0xFF

            // YCrCb color space dermis detection
            val y = 0.299f * r + 0.587f * g + 0.114f * b
            val cr = 128f + 0.500f * r - 0.419f * g - 0.081f * b
            val cb = 128f - 0.169f * r - 0.331f * g + 0.500f * b

            // Generous chromatic bounds covering fair, olive, golden, brown, and deep melanin skin
            if (cr in 122f..182f && cb in 72f..138f && r > b && r > 28 && y > 28) {
                val dCr = (cr - 152f) / 28f
                val dCb = (cb - 106f) / 30f
                val distSq = dCr * dCr + dCb * dCb
                if (distSq < 2.2f) {
                    skinWeights[i] = (1.0f - (distSq / 2.2f)).coerceIn(0f, 1f)
                }
            }
        }

        // Stage 2: Micro-Texture Smoothing (Pore Softening & Blemish Reduction)
        val smoothStrength = smoothness.coerceIn(0f, 1f)
        val tempR = IntArray(width * height)
        val tempG = IntArray(width * height)
        val tempB = IntArray(width * height)

        for (i in pixels.indices) {
            val c = pixels[i]
            tempR[i] = (c shr 16) and 0xFF
            tempG[i] = (c shr 8) and 0xFF
            tempB[i] = c and 0xFF
        }

        if (smoothStrength > 0.05f) {
            for (y in 1 until height - 1) {
                val row = y * width
                val rowAbove = (y - 1) * width
                val rowBelow = (y + 1) * width
                for (x in 1 until width - 1) {
                    val idx = row + x
                    val sw = skinWeights[idx]
                    if (sw > 0.15f) {
                        val cR = tempR[idx]
                        val cG = tempG[idx]
                        val cB = tempB[idx]

                        var sumR = cR * 2.0f
                        var sumG = cG * 2.0f
                        var sumB = cB * 2.0f
                        var count = 2.0f

                        val neighbors = intArrayOf(
                            rowAbove + x,
                            rowBelow + x,
                            row + (x - 1),
                            row + (x + 1)
                        )
                        for (nIdx in neighbors) {
                            if (skinWeights[nIdx] > 0.1f) {
                                val nR = tempR[nIdx]
                                val nG = tempG[nIdx]
                                val nB = tempB[nIdx]
                                val diff = abs(nR - cR) + abs(nG - cG) + abs(nB - cB)
                                if (diff < 50) {
                                    val w = (1.0f - diff / 50f)
                                    sumR += nR * w
                                    sumG += nG * w
                                    sumB += nB * w
                                    count += w
                                }
                            }
                        }
                        val blend = sw * smoothStrength * 0.70f
                        tempR[idx] = (cR * (1f - blend) + (sumR / count) * blend).roundToInt().coerceIn(0, 255)
                        tempG[idx] = (cG * (1f - blend) + (sumG / count) * blend).roundToInt().coerceIn(0, 255)
                        tempB[idx] = (cB * (1f - blend) + (sumB / count) * blend).roundToInt().coerceIn(0, 255)
                    }
                }
            }
        }

        // Stage 3: Regional Skin Tone Grading & Radiance Synthesis
        for (i in pixels.indices) {
            val sw = skinWeights[i]
            if (sw <= 0f) continue

            val c = pixels[i]
            val r = tempR[i]
            val g = tempG[i]
            val b = tempB[i]

            // Dynamic S-curve contrast on skin midtones & highlights
            val yLum = 0.299f * r + 0.587f * g + 0.114f * b
            val contrastFactor = if (yLum > 128f) {
                1.0f + (pContrast - 1.0f) * ((yLum - 128f) / 128f)
            } else {
                1.0f - (pContrast - 1.0f) * ((128f - yLum) / 128f) * 0.5f
            }

            val rGraded = (r * effRMult * contrastFactor + radOffset).coerceIn(0f, 255f)
            val gGraded = (g * effGMult * contrastFactor + radOffset * 0.72f).coerceIn(0f, 255f)
            val bGraded = (b * effBMult * contrastFactor + radOffset * 0.45f).coerceIn(0f, 255f)

            // Feathered alpha blend with untouched original
            val rFinal = (r * (1f - sw) + rGraded * sw).roundToInt().coerceIn(0, 255)
            val gFinal = (g * (1f - sw) + gGraded * sw).roundToInt().coerceIn(0, 255)
            val bFinal = (b * (1f - sw) + bGraded * sw).roundToInt().coerceIn(0, 255)

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

        // Memory-safe scaling: avoid OOM on modern 48MP/108MP phone cameras
        val maxDim = max(originalW, originalH)
        val safeMultiplier = if (maxDim >= 1440) 1 else scaleMultiplier.coerceIn(1, 2)

        val targetW = originalW * safeMultiplier
        val targetH = originalH * safeMultiplier

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

        val sharpenAmount = 0.42f
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
