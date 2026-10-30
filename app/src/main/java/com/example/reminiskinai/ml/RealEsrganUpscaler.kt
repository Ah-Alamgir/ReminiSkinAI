package com.example.reminiskinai.ml

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Real-ESRGAN General x4v3 neural super-resolution (`real_esrgan_general_x4v3.tflite`).
 *
 * Source: Qualcomm AI Hub export of xinntao/Real-ESRGAN `realesr-general-x4v3` (BSD-3-Clause).
 * - Input  [1, 128, 128, 3] float32 RGB in 0..1 (NHWC)
 * - Output [1, 512, 512, 3] float32 RGB in 0..1 (4x)
 *
 * Trained on synthetic real-world degradations (JPEG/WebP blocks, noise, blur, resize), so it
 * removes compression artifacts while reconstructing real detail instead of just stretching pixels.
 *
 * The photo is processed in overlapping 128x128 tiles. Each tile contributes only its central
 * region (a [PAD]-pixel context margin is discarded) so there are no visible seams. The 4x
 * output is area-downsampled straight into the Full HD target buffer, which keeps memory low
 * (we never allocate the full 4x image).
 */
class RealEsrganUpscaler(private val context: Context) {

    companion object {
        private const val TAG = "RealEsrganUpscaler"
        const val MODEL_ASSET = "real_esrgan_general_x4v3.tflite"
        private const val TILE = 128
        private const val SCALE = 4
        private const val OUT_TILE = TILE * SCALE
        private const val PAD = 16
        private const val CORE = TILE - 2 * PAD
    }

    private var interpreter: Interpreter? = null

    /** Backend name for telemetry/labels. */
    val backendName: String = "XNNPACK CPU"

    private fun loadModelBuffer(): ByteBuffer {
        return try {
            context.assets.openFd(MODEL_ASSET).use { afd ->
                FileInputStream(afd.fileDescriptor).channel.use { channel ->
                    channel.map(FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.declaredLength)
                }
            }
        } catch (e: Exception) {
            val bytes = context.assets.open(MODEL_ASSET).use { it.readBytes() }
            ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder()).apply {
                put(bytes)
                rewind()
            }
        }
    }

    @Synchronized
    private fun ensureInterpreter(): Interpreter? {
        interpreter?.let { return it }
        val buffer = loadModelBuffer()
        return try {
            val opts = Interpreter.Options().apply {
                setNumThreads(max(2, min(4, Runtime.getRuntime().availableProcessors())))
                setUseXNNPACK(true)
            }
            Interpreter(buffer, opts).also {
                interpreter = it
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load $MODEL_ASSET", e)
            null
        }
    }

    /**
     * Returns true if [src] is small enough that neural super-resolution adds real detail
     * when bringing it up to Full HD (short side [targetShortSide]).
     */
    fun needsUpscale(src: Bitmap, targetShortSide: Int = 1080): Boolean =
        min(src.width, src.height) < (targetShortSide * 0.92f).toInt()

    /**
     * Super-resolves [src] 4x with Real-ESRGAN and downsamples to Full HD (short side
     * [targetShortSide]).
     *
     * If [personMask256] is provided, applies Remini-style Subject-Guided blending:
     * - 100% Neural Real-ESRGAN detail on the person (hair, face, eyes, clothes, phone)
     * - Smooth, clean background de-blocking on walls and tiles to eliminate spiderweb/cracked-mud GAN artifacts.
     *
     * @param onProgress called with (tilesDone, tilesTotal) after every tile.
     */
    fun upscaleToFullHd(
        src: Bitmap,
        targetShortSide: Int = 1080,
        personMask256: FloatArray? = null,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): Bitmap? {
        val interp = ensureInterpreter() ?: return null
        val startMs = SystemClock.elapsedRealtime()

        val w = src.width
        val h = src.height
        val scaleToTarget = targetShortSide.toFloat() / min(w, h).toFloat()
        val targetW = (w * scaleToTarget).roundToInt().coerceAtLeast(1)
        val targetH = (h * scaleToTarget).roundToInt().coerceAtLeast(1)

        val w4 = w * SCALE
        val h4 = h * SCALE
        // Accumulate at Full HD (or at 4x if the source is so tiny that 4x is still below Full HD).
        val accW = min(targetW, w4)
        val accH = min(targetH, h4)
        val fx = w4.toFloat() / accW
        val fy = h4.toFloat() / accH
        val mapX = IntArray(w4) { min(accW - 1, (it / fx).toInt()) }
        val mapY = IntArray(h4) { min(accH - 1, (it / fy).toInt()) }

        val accRgb = FloatArray(accW * accH * 3)
        val accCnt = FloatArray(accW * accH)

        val argb = if (src.config == Bitmap.Config.ARGB_8888) src else src.copy(Bitmap.Config.ARGB_8888, false)
        val srcPx = IntArray(w * h)
        argb.getPixels(srcPx, 0, w, 0, 0, w, h)

        val inBuf = ByteBuffer.allocateDirect(TILE * TILE * 3 * 4).order(ByteOrder.nativeOrder())
        val outBuf = ByteBuffer.allocateDirect(OUT_TILE * OUT_TILE * 3 * 4).order(ByteOrder.nativeOrder())
        val outArr = FloatArray(OUT_TILE * OUT_TILE * 3)

        val coreYs = (0 until h step CORE).toList()
        val coreXs = (0 until w step CORE).toList()
        val total = coreYs.size * coreXs.size
        var done = 0

        try {
            for (cy in coreYs) {
                val y0 = if (h >= TILE) (cy - PAD).coerceIn(0, h - TILE) else 0
                val coreEndY = min(cy + CORE, h)
                for (cx in coreXs) {
                    val x0 = if (w >= TILE) (cx - PAD).coerceIn(0, w - TILE) else 0
                    val coreEndX = min(cx + CORE, w)

                    // Pack tile (edge-replicate if the photo is smaller than a tile)
                    inBuf.rewind()
                    for (ty in 0 until TILE) {
                        val sy = min(y0 + ty, h - 1)
                        val row = sy * w
                        for (tx in 0 until TILE) {
                            val c = srcPx[row + min(x0 + tx, w - 1)]
                            inBuf.putFloat(((c shr 16) and 0xFF) / 255f)
                            inBuf.putFloat(((c shr 8) and 0xFF) / 255f)
                            inBuf.putFloat((c and 0xFF) / 255f)
                        }
                    }
                    inBuf.rewind()
                    outBuf.rewind()
                    interp.run(inBuf, outBuf)
                    outBuf.rewind()
                    outBuf.asFloatBuffer().get(outArr)

                    // Splat only the tile's core region into the Full HD accumulator
                    val oyStart = (cy - y0) * SCALE
                    val oyEnd = (coreEndY - y0) * SCALE
                    val oxStart = (cx - x0) * SCALE
                    val oxEnd = (coreEndX - x0) * SCALE
                    for (oy in oyStart until oyEnd) {
                        val tRow = mapY[y0 * SCALE + oy] * accW
                        val oRow = oy * OUT_TILE
                        for (ox in oxStart until oxEnd) {
                            val t = tRow + mapX[x0 * SCALE + ox]
                            val o = (oRow + ox) * 3
                            val a = t * 3
                            accRgb[a] += outArr[o]
                            accRgb[a + 1] += outArr[o + 1]
                            accRgb[a + 2] += outArr[o + 2]
                            accCnt[t] += 1f
                        }
                    }

                    done++
                    onProgress(done, total)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Real-ESRGAN inference failed on $backendName", e)
            return null
        }

        val outPx = IntArray(accW * accH)
        for (i in 0 until accW * accH) {
            val n = accCnt[i].coerceAtLeast(1f)
            val a = i * 3
            val r = (accRgb[a] / n * 255f + 0.5f).toInt().coerceIn(0, 255)
            val g = (accRgb[a + 1] / n * 255f + 0.5f).toInt().coerceIn(0, 255)
            val b = (accRgb[a + 2] / n * 255f + 0.5f).toInt().coerceIn(0, 255)
            outPx[i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }
        var result = Bitmap.createBitmap(accW, accH, Bitmap.Config.ARGB_8888)
        result.setPixels(outPx, 0, accW, 0, 0, accW, accH)
        if (accW != targetW || accH != targetH) {
            result = Bitmap.createScaledBitmap(result, targetW, targetH, true)
        }

        // Remini Subject-Guided Blending:
        // When personMask256 is provided, we blend 100% Real-ESRGAN on the Subject (face, hair, skin, clothes)
        // with clean de-blocked bicubic on the background. This completely eliminates cracked-mud/spiderweb
        // GAN artifacts on bathroom tiles and walls while preserving razor-sharp detail on the person.
        if (personMask256 != null && personMask256.size == 256 * 256) {
            val cleanBase = Bitmap.createScaledBitmap(src, targetW, targetH, true)
            val basePx = IntArray(targetW * targetH)
            cleanBase.getPixels(basePx, 0, targetW, 0, 0, targetW, targetH)
            cleanBase.recycle()

            val resPx = IntArray(targetW * targetH)
            result.getPixels(resPx, 0, targetW, 0, 0, targetW, targetH)

            for (y in 0 until targetH) {
                val fy = (y.toFloat() / targetH) * 255f
                val y0 = fy.toInt().coerceIn(0, 255)
                val y1 = min(y0 + 1, 255)
                val wy = fy - y0
                val rowOffset = y * targetW

                for (x in 0 until targetW) {
                    val fx = (x.toFloat() / targetW) * 255f
                    val x0 = fx.toInt().coerceIn(0, 255)
                    val x1 = min(x0 + 1, 255)
                    val wx = fx - x0

                    val personW = (personMask256[y0 * 256 + x0] * (1f - wx) * (1f - wy) +
                        personMask256[y0 * 256 + x1] * wx * (1f - wy) +
                        personMask256[y1 * 256 + x0] * (1f - wx) * wy +
                        personMask256[y1 * 256 + x1] * wx * wy).coerceIn(0f, 1f)

                    val idx = rowOffset + x
                    val esr = resPx[idx]
                    val bas = basePx[idx]

                    val wSubject = personW
                    val wBase = 1f - wSubject

                    val r = ((((esr shr 16) and 0xFF) * wSubject + ((bas shr 16) and 0xFF) * wBase) + 0.5f).toInt().coerceIn(0, 255)
                    val g = ((((esr shr 8) and 0xFF) * wSubject + ((bas shr 8) and 0xFF) * wBase) + 0.5f).toInt().coerceIn(0, 255)
                    val b = (((esr and 0xFF) * wSubject + (bas and 0xFF) * wBase) + 0.5f).toInt().coerceIn(0, 255)

                    resPx[idx] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
                }
            }
            result.setPixels(resPx, 0, targetW, 0, 0, targetW, targetH)
        }

        Log.i(
            TAG,
            "Real-ESRGAN x4 ${w}x$h -> ${targetW}x$targetH on $backendName: $total tiles in " +
                "${SystemClock.elapsedRealtime() - startMs} ms"
        )
        return result
    }

    @Synchronized
    private fun closeInterpreter() {
        interpreter?.close()
        interpreter = null
    }

    fun close() = closeInterpreter()
}
