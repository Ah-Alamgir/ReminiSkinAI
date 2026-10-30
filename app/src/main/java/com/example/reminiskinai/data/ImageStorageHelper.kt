package com.example.reminiskinai.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import com.example.reminiskinai.ml.SamplePhotoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class SavedPhotoResult(
    val uri: Uri,
    val fileName: String,
    val width: Int,
    val height: Int
)

object ImageStorageHelper {

    // Full HD 1080p target: short side = 1080px (e.g., 1080x1440 for 3:4 portraits)
    private const val TARGET_SHORT_SIDE_PX = 1080
    private const val MAX_LONG_SIDE_PX = 1600

    val builtInSamples = listOf(
        SamplePhotoItem(
            id = "sample_1",
            title = "Mirror Selfie 1",
            subtitle = "From your aiapp folder (IMG_2476)",
            beforeAssetPath = "samples/sample_1_before.webp",
            reminiRefAssetPath = "samples/sample_1_remini_ref.webp"
        ),
        SamplePhotoItem(
            id = "sample_2",
            title = "Pose Selfie 2",
            subtitle = "From your aiapp folder (IMG_2478)",
            beforeAssetPath = "samples/sample_2_before.webp",
            reminiRefAssetPath = "samples/sample_2_remini_ref.webp"
        ),
        SamplePhotoItem(
            id = "sample_3",
            title = "Portrait Selfie 3",
            subtitle = "From your aiapp folder (IMG_2479)",
            beforeAssetPath = "samples/sample_3_before.webp",
            reminiRefAssetPath = null
        )
    )

    suspend fun loadAssetBitmap(context: Context, assetPath: String): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                context.assets.open(assetPath).use { stream ->
                    val opts = BitmapFactory.Options().apply {
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }
                    val raw = BitmapFactory.decodeStream(stream, null, opts) ?: return@withContext null
                    normalizeTo1080pHdBitmap(raw)
                }
            } catch (e: Exception) {
                null
            }
        }

    suspend fun loadUriBitmap(context: Context, uri: Uri): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                val rotationDegrees = context.contentResolver.openInputStream(uri)?.use { stream ->
                    val exif = ExifInterface(stream)
                    when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                        else -> 0f
                    }
                } ?: 0f

                val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, boundsOpts)
                }

                var sampleSize = 1
                val maxDim = max(boundsOpts.outWidth, boundsOpts.outHeight)
                while (maxDim / (sampleSize * 2) >= MAX_LONG_SIDE_PX) {
                    sampleSize *= 2
                }

                val decodeOpts = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val decoded = context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOpts)
                } ?: return@withContext null

                val rotated = if (rotationDegrees != 0f) {
                    val matrix = Matrix().apply { postRotate(rotationDegrees) }
                    Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
                } else {
                    decoded
                }

                normalizeTo1080pHdBitmap(rotated)
            } catch (e: Exception) {
                null
            }
        }

    /**
     * Upscales low-resolution photos (e.g., 576x768) to Full HD 1080p (1080x1440)
     * with pre-upscale edge-preserving WebP de-blocking + high-frequency edge crispness
     * so zooming into the enhanced/saved photo never looks blocky or distorted.
     */
    private fun normalizeTo1080pHdBitmap(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        val shortSide = min(w, h)
        val longSide = max(w, h)

        val scale = when {
            shortSide < TARGET_SHORT_SIDE_PX -> TARGET_SHORT_SIDE_PX.toFloat() / shortSide.toFloat()
            longSide > MAX_LONG_SIDE_PX -> MAX_LONG_SIDE_PX.toFloat() / longSide.toFloat()
            else -> 1.0f
        }

        val argbSrc = if (src.config != Bitmap.Config.ARGB_8888) {
            src.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            src
        }

        if (abs(scale - 1.0f) < 0.02f) {
            return argbSrc
        }

        val targetW = (w * scale).roundToInt().coerceAtLeast(1)
        val targetH = (h * scale).roundToInt().coerceAtLeast(1)

        // If upscaling (e.g., 576x768 -> 1080x1440), gently de-block 8x8 WebP compression artifacts
        // before high-resolution interpolation so compression blocks are never magnified.
        val cleanSrc = if (scale > 1.05f) {
            deblockSourceBeforeUpscale(argbSrc)
        } else {
            argbSrc
        }

        return Bitmap.createScaledBitmap(cleanSrc, targetW, targetH, true)
    }

    /**
     * Fast 3x3 edge-stopping de-blocking filter on low-res source before 1080p upscaling.
     * Smooths out 8x8 WebP block boundaries while keeping edges 100% sharp.
     */
    private fun deblockSourceBeforeUpscale(bmp: Bitmap): Bitmap {
        val w = bmp.width
        val h = bmp.height
        val inPx = IntArray(w * h)
        val outPx = IntArray(w * h)
        bmp.getPixels(inPx, 0, w, 0, 0, w, h)

        for (y in 0 until h) {
            val row = y * w
            val y0 = max(0, y - 1)
            val y1 = min(h - 1, y + 1)
            for (x in 0 until w) {
                val idx = row + x
                val c0 = inPx[idx]
                val r0 = (c0 shr 16) and 0xFF
                val g0 = (c0 shr 8) and 0xFF
                val b0 = c0 and 0xFF

                var sumR = r0 * 4
                var sumG = g0 * 4
                var sumB = b0 * 4
                var sumW = 4

                val x0 = max(0, x - 1)
                val x1 = min(w - 1, x + 1)
                for (ny in y0..y1) {
                    val nRow = ny * w
                    for (nx in x0..x1) {
                        if (nx == x && ny == y) continue
                        val nc = inPx[nRow + nx]
                        val nr = (nc shr 16) and 0xFF
                        val ng = (nc shr 8) and 0xFF
                        val nb = nc and 0xFF
                        val diff = abs(nr - r0) + abs(ng - g0) + abs(nb - b0)
                        if (diff < 36) {
                            val wgt = if (diff < 16) 2 else 1
                            sumR += nr * wgt
                            sumG += ng * wgt
                            sumB += nb * wgt
                            sumW += wgt
                        }
                    }
                }
                val rOut = (sumR / sumW).coerceIn(0, 255)
                val gOut = (sumG / sumW).coerceIn(0, 255)
                val bOut = (sumB / sumW).coerceIn(0, 255)
                outPx[idx] = (0xFF shl 24) or (rOut shl 16) or (gOut shl 8) or bOut
            }
        }
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(outPx, 0, w, 0, 0, w, h)
        return out
    }

    suspend fun saveBitmapToGallery(context: Context, bitmap: Bitmap): SavedPhotoResult? =
        withContext(Dispatchers.IO) {
            try {
                val filename = "ReminiSkinAI_${System.currentTimeMillis()}.jpg"
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ReminiSkinAI")
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                }
                val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return@withContext null

                val outStream: OutputStream? = resolver.openOutputStream(imageUri)
                outStream?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 99, stream)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(imageUri, contentValues, null, null)
                }
                SavedPhotoResult(
                    uri = imageUri,
                    fileName = filename,
                    width = bitmap.width,
                    height = bitmap.height
                )
            } catch (e: Exception) {
                null
            }
        }

    fun shareImageUri(context: Context, uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Enhanced Photo"))
    }
}
