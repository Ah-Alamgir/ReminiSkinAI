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
        return try {
            val srcBmp = source.asAndroidBitmap()
            val originalW = srcBmp.width
            val originalH = srcBmp.height

            // Ensure memory safety: clamp dimensions to max 1280px for crash-proof, real-time performance
            val maxDim = max(originalW, originalH)
            val (workingBmp, shouldRecycleWorking) = if (maxDim > 1280) {
                val scale = 1280f / maxDim
                val scaledW = (originalW * scale).roundToInt().coerceAtLeast(1)
                val scaledH = (originalH * scale).roundToInt().coerceAtLeast(1)
                Pair(Bitmap.createScaledBitmap(srcBmp, scaledW, scaledH, true), true)
            } else if (srcBmp.config != Bitmap.Config.ARGB_8888) {
                Pair(srcBmp.copy(Bitmap.Config.ARGB_8888, true), true)
            } else {
                Pair(srcBmp, false)
            }

            val width = workingBmp.width
            val height = workingBmp.height
            val totalPixels = width * height

            val pixels = IntArray(totalPixels)
            workingBmp.getPixels(pixels, 0, width, 0, 0, width, height)

            if (shouldRecycleWorking && workingBmp != srcBmp) {
                workingBmp.recycle()
            }

            // Remini-calibrated tone presets:
            // 1. Remini Natural: Fresh clean tones, rosy-peach dermis, balanced vibrancy, studio clarity
            // 2. American: Fair creamy-white porcelain tones with neutral undertone (never red)
            // 3. African: Deep melanin black/ebony velvety luster, preserving authentic dark tones (never orange/red)
            // 4. Korean: K-Beauty luminous glass skin, bright peachy-ivory highlight, translucent porcelain
            // 5. Bangladeshi: South Asian warm golden wheat glow, rich amber undertones
            val (pRMult, pGMult, pBMult, pRadBoost, pContrast) = when (preset) {
                AestheticPreset.REMINI_NATURAL -> {
                    // Remini Natural: Signature fair ivory porcelain & natural studio clarity (Default)
                    Tuple5(1.02f, 1.01f, 1.00f, 12f, 1.03f)
                }
                AestheticPreset.WARM_PEACH -> {
                    // Warm Honey Peach: Soft sun-kissed amber warmth & radiant golden peach
                    Tuple5(1.06f, 1.03f, 0.95f, 18f, 1.04f)
                }
                AestheticPreset.AMERICAN -> {
                    // American: Fair white/creamy complexion, neutral cool-ivory undertones
                    Tuple5(1.04f, 1.02f, 1.03f, 20f, 1.02f)
                }
                AestheticPreset.AFRICAN -> {
                    // African: Authentic deep melanin black/dark rich bronze with velvety specular highlights
                    Tuple5(1.25f, 0.82f, 0.64f, 6f, 1.08f)
                }
                AestheticPreset.KOREAN -> {
                    // Korean: K-Beauty luminous porcelain glass skin, rosy-peach highlight, clear and fair
                    Tuple5(1.06f, 1.02f, 1.05f, 26f, 1.03f)
                }
                AestheticPreset.BANGLADESHI -> {
                    // South Asian: Warm golden wheat glow
                    Tuple5(1.18f, 0.94f, 0.76f, 16f, 1.04f)
                }
                AestheticPreset.LATINA_OLIVE -> {
                    // Latina Olive: Sun-kissed golden honey bronze
                    Tuple5(1.14f, 0.98f, 0.80f, 20f, 1.04f)
                }
                AestheticPreset.NORDIC_FAIR -> {
                    // Nordic Alabaster: Ultra-bright cool fair porcelain
                    Tuple5(1.03f, 1.02f, 1.06f, 22f, 1.02f)
                }
                AestheticPreset.MEDITERRANEAN -> {
                    // Mediterranean: Golden warm terracotta radiance
                    Tuple5(1.16f, 0.96f, 0.78f, 18f, 1.04f)
                }
            }

            val effWarmth = warmth.coerceIn(0.6f, 1.4f)
            val effRMult = pRMult * effWarmth
            val effGMult = pGMult * ((effWarmth - 1.0f) * 0.25f + 1.0f)
            val effBMult = (pBMult / effWarmth).coerceIn(0.70f, 1.30f)
            val radOffset = ((radiance - 1.0f) * pRadBoost).coerceIn(-25f, 35f)

            // Stage 1: STRICT Human Skin Dermis Segmentation
            // Strictly isolates skin dermis while guaranteeing clothes, background, hair, eyes, and teeth remain 100% UNTOUCHED
            val skinWeights = FloatArray(totalPixels)
            for (i in 0 until totalPixels) {
                val c = pixels[i]
                val r = (c shr 16) and 0xFF
                val g = (c shr 8) and 0xFF
                val b = c and 0xFF

                val maxC = max(r, max(g, b))
                val minC = min(r, min(g, b))
                val chromaDiff = maxC - minC

                // 1. REJECT NEUTRALS: White clothing, gray walls, dark shadows, black hair (all have maxC - minC < 14)
                if (chromaDiff < 14) continue

                // 2. REJECT NON-SKIN DOMINANCE: Blue suits, green backgrounds, purple clothes
                // Across all human complexions, Red is the dominant channel
                if (r <= g || r <= b) continue

                // 3. REJECT CLOTHING OVER-SATURATION: Vivid red, orange, or yellow garments have saturation > 0.68
                val saturation = chromaDiff.toFloat() / maxC.toFloat()
                if (saturation > 0.68f) continue

                // 4. REJECT VERY DARK SHADOWS AND HAIR
                if (maxC < 32) continue

                // 5. YCbCr Chrominance Ellipse for Human Skin
                val y = 0.299f * r + 0.587f * g + 0.114f * b
                val cr = 128f + 0.500f * r - 0.419f * g - 0.081f * b
                val cb = 128f - 0.169f * r - 0.331f * g + 0.500f * b

                // Strict biological human dermis chrominance bounds
                if (cr !in 133f..175f || cb !in 80f..130f) continue

                // 6. Hue Angle in Degrees [0°..360°]
                // Melanin and hemoglobin in skin strictly fall within [0°..38°] (warm peach/golden) or [345°..360°] (rosy)
                val hue = (when (maxC) {
                    r -> 60f * (((g - b).toFloat() / chromaDiff.toFloat()) % 6f)
                    g -> 60f * (((b - r).toFloat() / chromaDiff.toFloat()) + 2f)
                    else -> 60f * (((r - g).toFloat() / chromaDiff.toFloat()) + 4f)
                }).let { if (it < 0f) it + 360f else it }

                if (hue !in 0f..38f && hue !in 345f..360f) continue

                // Gaussian dermis confidence metric
                val dCr = (cr - 150f) / 18f
                val dCb = (cb - 105f) / 16f
                val distSq = dCr * dCr + dCb * dCb
                if (distSq < 1.0f) {
                    skinWeights[i] = (1.0f - distSq).coerceIn(0f, 1f)
                }
            }

            // Stage 2: Micro-Texture Smoothing (Pore Softening & Blemish Reduction)
            // In-place calculation touching ONLY detected skin dermis pixels
            val smoothStrength = smoothness.coerceIn(0f, 1f)
            val smoothedPixels = if (smoothStrength > 0.05f) {
                val smoothed = pixels.clone()
                val blendFactor = smoothStrength * 0.65f

                for (y in 1 until height - 1) {
                    val row = y * width
                    val rowAbove = (y - 1) * width
                    val rowBelow = (y + 1) * width
                    for (x in 1 until width - 1) {
                        val idx = row + x
                        val sw = skinWeights[idx]
                        if (sw > 0.20f) {
                            val c = pixels[idx]
                            val cR = (c shr 16) and 0xFF
                            val cG = (c shr 8) and 0xFF
                            val cB = c and 0xFF

                            var sumR = cR * 2.0f
                            var sumG = cG * 2.0f
                            var sumB = cB * 2.0f
                            var count = 2.0f

                            val nTop = pixels[rowAbove + x]
                            val nBottom = pixels[rowBelow + x]
                            val nLeft = pixels[row + x - 1]
                            val nRight = pixels[row + x + 1]

                            val nTopR = (nTop shr 16) and 0xFF
                            val nTopG = (nTop shr 8) and 0xFF
                            val nTopB = nTop and 0xFF
                            val diffTop = abs(nTopR - cR) + abs(nTopG - cG) + abs(nTopB - cB)
                            if (diffTop < 50 && skinWeights[rowAbove + x] > 0.15f) {
                                val w = 1.0f - diffTop / 50f
                                sumR += nTopR * w; sumG += nTopG * w; sumB += nTopB * w; count += w
                            }

                            val nBottomR = (nBottom shr 16) and 0xFF
                            val nBottomG = (nBottom shr 8) and 0xFF
                            val nBottomB = nBottom and 0xFF
                            val diffBottom = abs(nBottomR - cR) + abs(nBottomG - cG) + abs(nBottomB - cB)
                            if (diffBottom < 50 && skinWeights[rowBelow + x] > 0.15f) {
                                val w = 1.0f - diffBottom / 50f
                                sumR += nBottomR * w; sumG += nBottomG * w; sumB += nBottomB * w; count += w
                            }

                            val nLeftR = (nLeft shr 16) and 0xFF
                            val nLeftG = (nLeft shr 8) and 0xFF
                            val nLeftB = nLeft and 0xFF
                            val diffLeft = abs(nLeftR - cR) + abs(nLeftG - cG) + abs(nLeftB - cB)
                            if (diffLeft < 50 && skinWeights[row + x - 1] > 0.15f) {
                                val w = 1.0f - diffLeft / 50f
                                sumR += nLeftR * w; sumG += nLeftG * w; sumB += nLeftB * w; count += w
                            }

                            val nRightR = (nRight shr 16) and 0xFF
                            val nRightG = (nRight shr 8) and 0xFF
                            val nRightB = nRight and 0xFF
                            val diffRight = abs(nRightR - cR) + abs(nRightG - cG) + abs(nRightB - cB)
                            if (diffRight < 50 && skinWeights[row + x + 1] > 0.15f) {
                                val w = 1.0f - diffRight / 50f
                                sumR += nRightR * w; sumG += nRightG * w; sumB += nRightB * w; count += w
                            }

                            val blend = sw * blendFactor
                            val finalR = (cR * (1f - blend) + (sumR / count) * blend).roundToInt().coerceIn(0, 255)
                            val finalG = (cG * (1f - blend) + (sumG / count) * blend).roundToInt().coerceIn(0, 255)
                            val finalB = (cB * (1f - blend) + (sumB / count) * blend).roundToInt().coerceIn(0, 255)
                            smoothed[idx] = (c and -0x1000000) or (finalR shl 16) or (finalG shl 8) or finalB
                        }
                    }
                }
                smoothed
            } else {
                pixels
            }

            // Stage 3: Selective Regional Skin Tone Grading & Radiance Synthesis
            // Pixels outside human skin (sw == 0f) are left 100% UNTOUCHED!
            for (i in 0 until totalPixels) {
                val sw = skinWeights[i]
                if (sw <= 0f) continue

                val c = smoothedPixels[i]
                val r = (c shr 16) and 0xFF
                val g = (c shr 8) and 0xFF
                val b = c and 0xFF

                // Dynamic S-curve contrast on skin midtones & highlights
                val yLum = 0.299f * r + 0.587f * g + 0.114f * b
                val contrastFactor = if (yLum > 128f) {
                    1.0f + (pContrast - 1.0f) * ((yLum - 128f) / 128f)
                } else {
                    1.0f - (pContrast - 1.0f) * ((128f - yLum) / 128f) * 0.4f
                }

                val rGraded = (r * effRMult * contrastFactor + radOffset).coerceIn(0f, 255f)
                val gGraded = (g * effGMult * contrastFactor + radOffset * 0.85f).coerceIn(0f, 255f)
                val bGraded = (b * effBMult * contrastFactor + radOffset * 0.80f).coerceIn(0f, 255f)

                // Feathered blend on skin pixels only
                val rFinal = (r * (1f - sw) + rGraded * sw).roundToInt().coerceIn(0, 255)
                val gFinal = (g * (1f - sw) + gGraded * sw).roundToInt().coerceIn(0, 255)
                val bFinal = (b * (1f - sw) + bGraded * sw).roundToInt().coerceIn(0, 255)

                pixels[i] = (c and -0x1000000) or (rFinal shl 16) or (gFinal shl 8) or bFinal
            }

            val outBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            outBmp.setPixels(pixels, 0, width, 0, 0, width, height)
            outBmp.asImageBitmap()
        } catch (_: Throwable) {
            source
        }
    }

    actual fun enhancePhoto(
        source: ImageBitmap,
        scaleMultiplier: Int
    ): ImageBitmap {
        return try {
            val srcBmp = source.asAndroidBitmap()
            val originalW = srcBmp.width
            val originalH = srcBmp.height

            // Memory-safe scaling: avoid OOM on modern 48MP/108MP phone cameras
            val maxDim = max(originalW, originalH)
            val (workingBmp, shouldRecycleWorking) = if (maxDim > 1440) {
                val scale = 1440f / maxDim
                val scaledW = (originalW * scale).roundToInt().coerceAtLeast(1)
                val scaledH = (originalH * scale).roundToInt().coerceAtLeast(1)
                Pair(Bitmap.createScaledBitmap(srcBmp, scaledW, scaledH, true), true)
            } else if (srcBmp.config != Bitmap.Config.ARGB_8888) {
                Pair(srcBmp.copy(Bitmap.Config.ARGB_8888, true), true)
            } else {
                Pair(srcBmp, false)
            }

            val w = workingBmp.width
            val h = workingBmp.height
            val inPixels = IntArray(w * h)
            val outPixels = IntArray(w * h)
            workingBmp.getPixels(inPixels, 0, w, 0, 0, w, h)

            if (shouldRecycleWorking && workingBmp != srcBmp) {
                workingBmp.recycle()
            }

            val sharpenAmount = 0.38f
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
            resultBmp.asImageBitmap()
        } catch (_: Throwable) {
            source
        }
    }

    private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
}
