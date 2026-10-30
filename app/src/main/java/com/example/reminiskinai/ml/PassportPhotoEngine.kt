package com.example.reminiskinai.ml

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.channels.FileChannel
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * On-Device Biometric Passport Photo AI Engine.
 *
 * Combines 4 pre-trained neural networks from GitHub & Hugging Face:
 * 1. BRIA RMBG-1.4 Neural Matting (`rmbg_14_quantized.onnx`, 1024×1024 ONNX)
 *    - Source: `briaai/RMBG-1.4` (Hugging Face) quantized ONNX.
 *    - Runs at 1024×1024 for studio-clean hair, shoulder, and torso alpha matting.
 *
 * 2. MediaPipe 6-Class Multiclass Selfie Segmenter (`selfie_multiclass_256x256.tflite`, 256×256 TFLite)
 *    - Separates Background(0), Hair(1), Body-Skin(2), Face-Skin(3), Clothes(4), Others/Objects(5).
 *    - Rejects background walls/mirrors and handheld objects (e.g. phones in mirror selfies) while isolating
 *      true hair, face skin, and neck skin.
 *
 * 3. BiSeNet 19-Class CelebAMask-HQ Face & Neck Parser (`faceparsing.tflite`, 512×512 TFLite)
 *    - Extracts 19 facial & upper-torso classes (skin, eyes, brows, nose, lips, ears, neck, hair, cloth).
 *    - Computes sub-pixel eye centroids for auto-leveling head tilt, crown-to-chin head height for
 *      country-standard biometric scaling, and exact chin/neck anchor coordinates for coat fitting.
 *
 * 4. GPEN-BFR-256 Blind Face Restoration GAN (`gpen_bfr_256.onnx`, 256×256 ONNX)
 *    - Reconstructs crisp, studio-sharp facial features (eyes, brows, lips, skin) when cropping into
 *      distant or compressed selfies for a high-DPI passport print.
 */
class PassportPhotoEngine(private val context: Context) {

    companion object {
        private const val TAG = "PassportPhotoEngine"

        private const val MODEL_RMBG_14 = "rmbg_14_quantized.onnx"
        private const val MODEL_SELFIE_MULTICLASS = "selfie_multiclass_256x256.tflite"
        private const val MODEL_BISENET_FACE_PARSE = "faceparsing.tflite"
        private const val MODEL_GPEN_BFR_256 = "gpen_bfr_256.onnx"

        private const val REGION_OUTER_TRANSPARENT: Byte = 0
        private const val REGION_NECK_HOLE: Byte = 1
        private const val REGION_BACK_COLLAR: Byte = 2
        private const val REGION_FRONT_GARMENT: Byte = 3
    }

    private var ortEnv: OrtEnvironment? = null
    private var rmbgSession: OrtSession? = null
    private var gpenSession: OrtSession? = null
    private var selfieInterpreter: Interpreter? = null
    private var bisenetInterpreter: Interpreter? = null

    private val loadedCoatsCache = mutableMapOf<String, LoadedCoatAsset>()

    private fun loadModelBuffer(assetName: String): ByteBuffer {
        return try {
            context.assets.openFd(assetName).use { afd ->
                FileInputStream(afd.fileDescriptor).channel.use { channel ->
                    channel.map(FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.declaredLength)
                }
            }
        } catch (e: Exception) {
            val bytes = context.assets.open(assetName).use { it.readBytes() }
            ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder()).apply {
                put(bytes)
                rewind()
            }
        }
    }

    private fun createInterpreter(assetName: String): Interpreter? {
        return try {
            val buffer = loadModelBuffer(assetName)
            val options = Interpreter.Options().apply {
                setNumThreads(4)
                setUseXNNPACK(true)
            }
            Interpreter(buffer, options)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load TFLite interpreter for $assetName", e)
            null
        }
    }

    private fun createOrtSession(assetName: String, minBytes: Long): OrtSession? {
        return try {
            val env = ortEnv ?: OrtEnvironment.getEnvironment().also { ortEnv = it }
            val cachedModelFile = File(context.cacheDir, assetName)
            if (!cachedModelFile.exists() || cachedModelFile.length() < minBytes) {
                context.assets.open(assetName).use { input ->
                    FileOutputStream(cachedModelFile).use { output ->
                        input.copyTo(output, bufferSize = 256 * 1024)
                    }
                }
            }
            val opts = OrtSession.SessionOptions().apply {
                setIntraOpNumThreads(4)
                setOptimizationLevel(OrtSession.SessionOptions.OptLevel.BASIC_OPT)
            }
            env.createSession(cachedModelFile.absolutePath, opts)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load ONNX session for $assetName", e)
            null
        }
    }

    @Synchronized
    private fun ensureModelsLoaded() {
        if (rmbgSession == null) {
            rmbgSession = createOrtSession(MODEL_RMBG_14, 30_000_000L)
        }
        if (selfieInterpreter == null) {
            selfieInterpreter = createInterpreter(MODEL_SELFIE_MULTICLASS)
        }
        if (bisenetInterpreter == null) {
            bisenetInterpreter = createInterpreter(MODEL_BISENET_FACE_PARSE)
        }
        if (gpenSession == null) {
            gpenSession = createOrtSession(MODEL_GPEN_BFR_256, 70_000_000L)
        }
    }

    /**
     * Loads and calibrates all 10 Nano Banana 2 coat templates from `assets/coats/`.
     */
    suspend fun loadCoatAssets(): List<LoadedCoatAsset> = withContext(Dispatchers.IO) {
        PassportCoatCatalog.templates.mapNotNull { item ->
            getOrLoadCoatAsset(item)
        }
    }

    @Synchronized
    fun getOrLoadCoatAsset(item: CoatTemplateItem): LoadedCoatAsset? {
        loadedCoatsCache[item.id]?.let { return it }
        return try {
            val rawBmp = context.assets.open(item.assetPath).use { stream ->
                val opts = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                BitmapFactory.decodeStream(stream, null, opts)
            } ?: return null

            val calibrated = calibrateCoatBitmap(item, rawBmp)
            loadedCoatsCache[item.id] = calibrated
            calibrated
        } catch (e: Exception) {
            Log.w(TAG, "Coat asset not found or failed to decode: ${item.assetPath} (${e.message})")
            null
        }
    }

    /**
     * Calibrates the coat bitmap, builds its 3-layer collar region map (`REGION_NECK_HOLE`,
     * `REGION_BACK_COLLAR`, `REGION_FRONT_GARMENT`), and locates its exact collar anchor.
     */
    private fun calibrateCoatBitmap(item: CoatTemplateItem, src: Bitmap): LoadedCoatAsset {
        val w = src.width
        val h = src.height
        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)

        // Remove any residual chroma-key green if present
        for (i in pixels.indices) {
            val c = pixels[i]
            val a = (c ushr 24) and 0xFF
            if (a == 0) continue
            val r = (c shr 16) and 0xFF
            var g = (c shr 8) and 0xFF
            val b = c and 0xFF
            val maxRB = max(r, b)
            val greenExcess = g - maxRB
            if (greenExcess > 18 && g > 40) {
                val alphaFactor = ((48f - greenExcess) / 30f).coerceIn(0f, 1f)
                val newA = (a * alphaFactor).roundToInt().coerceIn(0, 255)
                g = min(g, (r + b) / 2 + 4)
                pixels[i] = (newA shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        // Trim transparent padding on left, right, top, and bottom
        var minX = w
        var maxX = 0
        var minY = h
        var maxY = 0
        for (y in 0 until h) {
            val rowOff = y * w
            for (x in 0 until w) {
                val a = (pixels[rowOff + x] ushr 24) and 0xFF
                if (a > 40) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }

        val cropL = if (maxX > minX) minX else 0
        val cropR = if (maxX > minX) maxX else w - 1
        val cropT = if (maxY > minY) minY else 0
        val cropB = if (maxY > minY) maxY else h - 1
        val cw = (cropR - cropL + 1).coerceAtLeast(64)
        val ch = (cropB - cropT + 1).coerceAtLeast(64)

        val croppedPixels = IntArray(cw * ch)
        for (y in 0 until ch) {
            System.arraycopy(pixels, (cropT + y) * w + cropL, croppedPixels, y * cw, cw)
        }

        val cleanBmp = Bitmap.createBitmap(cw, ch, Bitmap.Config.ARGB_8888)
        cleanBmp.setPixels(croppedPixels, 0, cw, 0, 0, cw, ch)

        val cx = cw / 2
        var topCenterOpaqueY = -1
        for (y in 0 until (ch * 0.65f).toInt()) {
            val a = (croppedPixels[y * cw + cx] ushr 24) and 0xFF
            if (a > 120) {
                topCenterOpaqueY = y
                break
            }
        }

        // Check if there is an opaque back collar band crossing the top center (y < 5% of height)
        val hasBackCollarBand = topCenterOpaqueY in 0..(ch * 0.05f).toInt()
        val holeStartY: Int
        val holeEndY: Int

        if (hasBackCollarBand) {
            var hs = (ch * 0.06f).toInt()
            for (y in topCenterOpaqueY until (ch * 0.55f).toInt()) {
                val a = (croppedPixels[y * cw + cx] ushr 24) and 0xFF
                if (a < 80) {
                    hs = y
                    break
                }
            }
            holeStartY = hs
            var he = (ch * 0.28f).toInt()
            for (y in (holeStartY + 4) until (ch * 0.68f).toInt()) {
                val a = (croppedPixels[y * cw + cx] ushr 24) and 0xFF
                if (a > 180) {
                    var solid = true
                    for (k in 1..6) {
                        if (y + k < ch && ((croppedPixels[(y + k) * cw + cx] ushr 24) and 0xFF) < 150) {
                            solid = false
                            break
                        }
                    }
                    if (solid) {
                        he = y
                        break
                    }
                }
            }
            holeEndY = he
        } else {
            // Open-neck suit/blazer template (Nano Banana V3 with open neckline)
            // Center is open right from the top down to the tie knot / V-neck apex (topCenterOpaqueY)
            holeStartY = (ch * 0.02f).toInt()
            holeEndY = if (topCenterOpaqueY > holeStartY) topCenterOpaqueY else (ch * 0.16f).toInt()
        }

        // Build 3-layer region map for studio collar & V-neck compositing
        val regionMap = ByteArray(cw * ch)
        for (i in croppedPixels.indices) {
            val a = (croppedPixels[i] ushr 24) and 0xFF
            if (a > 12) {
                regionMap[i] = REGION_FRONT_GARMENT
            } else {
                regionMap[i] = REGION_OUTER_TRANSPARENT
            }
        }

        var topHoleLeftX = (cw * 0.34f).toInt()
        var topHoleRightX = (cw * 0.66f).toInt()
        var foundTopHoleWalls = false

        for (y in holeStartY..holeEndY.coerceAtMost(ch - 1)) {
            val rowOff = y * cw
            var leftWall = -1
            for (x in cx downTo (cw * 0.14f).toInt()) {
                val a = (croppedPixels[rowOff + x] ushr 24) and 0xFF
                if (a > 190) {
                    leftWall = x
                    break
                }
            }
            var rightWall = -1
            for (x in cx until (cw * 0.86f).toInt()) {
                val a = (croppedPixels[rowOff + x] ushr 24) and 0xFF
                if (a > 190) {
                    rightWall = x
                    break
                }
            }
            if (leftWall > 0 && rightWall > leftWall + 6) {
                if (!foundTopHoleWalls && y >= holeStartY + 2) {
                    topHoleLeftX = leftWall
                    topHoleRightX = rightWall
                    foundTopHoleWalls = true
                }
                // Include a 2px overlap into the inner collar wall so there is zero hairline gap
                val fillL = (leftWall - 2).coerceAtLeast(0)
                val fillR = (rightWall + 2).coerceAtMost(cw - 1)
                for (x in fillL..fillR) {
                    val a = (croppedPixels[rowOff + x] ushr 24) and 0xFF
                    if (a < 245) {
                        regionMap[rowOff + x] = REGION_NECK_HOLE
                    }
                }
            }
        }

        // Also mark the entire open throat slot (from y=0 down to holeStartY) between topHoleLeftX..topHoleRightX
        // as REGION_NECK_HOLE so the user's real neck seamlessly fills through without any background bleed
        val throatL = (topHoleLeftX + 2).coerceAtLeast(0)
        val throatR = (topHoleRightX - 2).coerceAtMost(cw - 1)
        if (throatR > throatL + 8) {
            for (y in 0 until holeStartY) {
                val rowOff = y * cw
                for (x in throatL..throatR) {
                    if (regionMap[rowOff + x] == REGION_OUTER_TRANSPARENT) {
                        regionMap[rowOff + x] = REGION_NECK_HOLE
                    }
                }
            }
        }

        // Open the ghost-mannequin back-collar bridge (0..holeStartY between topHoleLeftX..topHoleRightX)
        // so the user's real neck tucks seamlessly into the collar without a white band crossing the throat
        if (hasBackCollarBand) {
            val innerL = topHoleLeftX + 2
            val innerR = topHoleRightX - 2
            if (innerR > innerL + 8) {
                for (y in 0..(holeStartY + 4).coerceAtMost(ch - 1)) {
                    val rowOff = y * cw
                    for (x in innerL..innerR) {
                        val distEdge = min(x - innerL, innerR - x).toFloat()
                        val edgeKeep = (1f - (distEdge / 6f)).coerceIn(0f, 1f)
                        val c = croppedPixels[rowOff + x]
                        val curA = (c ushr 24) and 0xFF
                        val newA = (curA * edgeKeep).roundToInt().coerceIn(0, 255)
                        croppedPixels[rowOff + x] = (newA shl 24) or (c and 0x00FFFFFF)
                        if (newA < 40) {
                            regionMap[rowOff + x] = REGION_OUTER_TRANSPARENT
                        } else {
                            regionMap[rowOff + x] = REGION_BACK_COLLAR
                        }
                    }
                }
                cleanBmp.setPixels(croppedPixels, 0, cw, 0, 0, cw, ch)
            }
        }

        // Measure collar opening width where both left and right collar wings exist
        var collarOpeningWidth = (topHoleRightX - topHoleLeftX).toFloat()
        if (collarOpeningWidth < 80f) {
            // Robust scan down the top 30% of the coat for the collar wing separation
            for (y in 0 until (ch * 0.35f).toInt()) {
                val rowOff = y * cw
                var lw = -1
                for (x in cx downTo 0) {
                    if (((croppedPixels[rowOff + x] ushr 24) and 0xFF) > 150) {
                        lw = x
                        break
                    }
                }
                var rw = -1
                for (x in cx until cw) {
                    if (((croppedPixels[rowOff + x] ushr 24) and 0xFF) > 150) {
                        rw = x
                        break
                    }
                }
                if (lw > 0 && rw > lw + 70 && ((croppedPixels[rowOff + cx] ushr 24) and 0xFF) < 100) {
                    collarOpeningWidth = (rw - lw).toFloat()
                    break
                }
            }
        }
        collarOpeningWidth = collarOpeningWidth.coerceIn(cw * 0.18f, cw * 0.40f)

        // Collar wing anchor: collar wing tips sit naturally below the chin line.
        // We anchor to holeStartY (or top opaque wing row) so the coat never rides up over the chin/jaw.
        val collarAnchorY = holeStartY.toFloat().coerceAtLeast(10f)

        return LoadedCoatAsset(
            item = item,
            bitmap = cleanBmp,
            collarCenterX = cw * 0.5f,
            collarJunctionY = collarAnchorY,
            collarOpeningWidth = collarOpeningWidth,
            shoulderSpanWidth = cw.toFloat(),
            holeTopY = holeStartY.toFloat(),
            holeBottomY = holeEndY.toFloat(),
            regionMap = regionMap
        )
    }

    /**
     * Runs the 4-model neural passport pipeline on [sourceBitmap] and caches all masks & biometric landmarks.
     */
    suspend fun analyzePortrait(
        sourceBitmap: Bitmap,
        onStageProgress: (PipelineStageProgress) -> Unit
    ): CachedPassportSession = withContext(Dispatchers.Default) {
        val startMs = SystemClock.elapsedRealtime()
        val width = sourceBitmap.width
        val height = sourceBitmap.height
        val numPixels = width * height

        val origPixels = IntArray(numPixels)
        sourceBitmap.getPixels(origPixels, 0, width, 0, 0, width, height)

        val activeModels = mutableListOf<String>()
        ensureModelsLoaded()

        // =========================================================================
        // STAGE 1: MediaPipe 6-Class Multiclass Segmenter (256x256 TFLite)
        // =========================================================================
        onStageProgress(
            PipelineStageProgress(
                stepIndex = 1,
                totalSteps = 4,
                modelName = "MediaPipe 6-Class Segmenter (256×256)",
                stageDescription = "Locating portrait ROI & isolating hair, skin, clothes & background...",
                progressFraction = 0.22f
            )
        )

        val mpPerson256 = FloatArray(256 * 256)
        val mpClothes256 = FloatArray(256 * 256)
        val mpHeadNeck256 = FloatArray(256 * 256)
        val mpHeadHair256 = FloatArray(256 * 256)
        val mpFaceSkin256 = FloatArray(256 * 256)
        val mpBodySkin256 = FloatArray(256 * 256)

        val faceBBoxNorm = runMediaPipeMulticlassForPassport(
            sourceBitmap = sourceBitmap,
            outPerson256 = mpPerson256,
            outClothes256 = mpClothes256,
            outHeadNeck256 = mpHeadNeck256,
            outHeadHair256 = mpHeadHair256,
            outFaceSkin256 = mpFaceSkin256,
            outBodySkin256 = mpBodySkin256
        )
        if (selfieInterpreter != null) {
            activeModels.add("MediaPipe 6-Class 256×256 (TFLite)")
        }

        // Compute BiSeNet 512x512 face+neck crop
        val faceCropRect = computePassportFaceNeckCrop(faceBBoxNorm, width, height)

        // =========================================================================
        // STAGE 2: RMBG-1.4 SOTA Neural Matting (1024x1024 ONNX)
        // =========================================================================
        onStageProgress(
            PipelineStageProgress(
                stepIndex = 2,
                totalSteps = 4,
                modelName = "RMBG-1.4 High-Res Matting (1024×1024 ONNX)",
                stageDescription = "Extracting studio-clean hair strands, neck & shoulder alpha matte...",
                progressFraction = 0.50f
            )
        )

        val rmbgMatte = FloatArray(numPixels)
        val rmbgSuccess = runRmbgMatting1024(sourceBitmap, width, height, rmbgMatte)
        if (rmbgSuccess) {
            activeModels.add("RMBG-1.4 SOTA 1024×1024 (ONNX)")
        }

        // =========================================================================
        // STAGE 3: BiSeNet 19-Class Face & Neck Parser (512x512 TFLite)
        // =========================================================================
        onStageProgress(
            PipelineStageProgress(
                stepIndex = 3,
                totalSteps = 4,
                modelName = "BiSeNet 19-Class Face & Neck Parser (512×512)",
                stageDescription = "Measuring biometric eye tilt, crown, chin & neck collar anchor...",
                progressFraction = 0.76f
            )
        )

        val bisenetPerson512 = FloatArray(512 * 512)
        val bisenetHeadNeck512 = FloatArray(512 * 512)
        val bisenetCloth512 = FloatArray(512 * 512)
        val bisenetSkin512 = FloatArray(512 * 512)

        val parsedGeom = runBiSeNetPassportParsing(
            sourceBitmap = sourceBitmap,
            cropRect = faceCropRect,
            fullWidth = width,
            fullHeight = height,
            mpPerson256 = mpPerson256,
            mpHeadHair256 = mpHeadHair256,
            mpFaceSkin256 = mpFaceSkin256,
            outPerson512 = bisenetPerson512,
            outHeadNeck512 = bisenetHeadNeck512,
            outCloth512 = bisenetCloth512,
            outSkin512 = bisenetSkin512
        )
        if (bisenetInterpreter != null) {
            activeModels.add("BiSeNet 19-Class 512×512 (TFLite)")
        }

        // =========================================================================
        // STAGE 4: Fuse Alpha Matte + GPEN Face Restoration + Studio Skin Polish
        // =========================================================================
        onStageProgress(
            PipelineStageProgress(
                stepIndex = 4,
                totalSteps = 4,
                modelName = "GPEN-BFR-256 & Biometric Coat Alignment",
                stageDescription = "Restoring crisp facial details, removing background & fitting coat...",
                progressFraction = 0.94f
            )
        )

        val personAlphaMatte = FloatArray(numPixels)
        val clothesMask = FloatArray(numPixels)
        val headAndNeckMask = FloatArray(numPixels)
        val skinMask = FloatArray(numPixels)

        fusePassportMasks(
            width = width,
            height = height,
            origPixels = origPixels,
            faceCropRect = faceCropRect,
            rmbgSuccess = rmbgSuccess,
            rmbgMatte = rmbgMatte,
            mpPerson256 = mpPerson256,
            mpClothes256 = mpClothes256,
            mpHeadNeck256 = mpHeadNeck256,
            mpHeadHair256 = mpHeadHair256,
            mpFaceSkin256 = mpFaceSkin256,
            mpBodySkin256 = mpBodySkin256,
            bisenetPerson512 = bisenetPerson512,
            bisenetHeadNeck512 = bisenetHeadNeck512,
            bisenetCloth512 = bisenetCloth512,
            bisenetSkin512 = bisenetSkin512,
            chinBottomY = parsedGeom.chinBottomY,
            neckCenterX = parsedGeom.neckCenterX,
            neckWidthPx = parsedGeom.neckWidthPx,
            outPersonAlpha = personAlphaMatte,
            outClothesMask = clothesMask,
            outHeadAndNeckMask = headAndNeckMask,
            outSkinMask = skinMask
        )

        val gpenUsed = BooleanArray(1)
        val polishedPixels = computeStudioPolishedForeground(
            width = width,
            height = height,
            origPixels = origPixels,
            faceCropRect = faceCropRect,
            geom = parsedGeom,
            personAlpha = personAlphaMatte,
            skinMask = skinMask,
            outGpenUsed = gpenUsed
        )
        if (gpenUsed[0]) {
            activeModels.add("GPEN-BFR-256 Face GAN (ONNX)")
        }

        val neckRefRgb = sampleUserNeckSkinReferenceRgb(
            width = width,
            height = height,
            pixels = polishedPixels,
            skinMask = skinMask,
            geom = parsedGeom
        )

        val elapsedMs = SystemClock.elapsedRealtime() - startMs
        val finalGeom = parsedGeom.copy(
            activeModels = activeModels,
            inferenceTimeMs = elapsedMs
        )

        CachedPassportSession(
            sourceBitmap = sourceBitmap,
            width = width,
            height = height,
            origPixels = origPixels,
            polishedPixels = polishedPixels,
            personAlphaMatte = personAlphaMatte,
            clothesMask = clothesMask,
            headAndNeckMask = headAndNeckMask,
            skinMask = skinMask,
            neckRefRgb = neckRefRgb,
            geometry = finalGeom
        )
    }

    /**
     * Renders the composite Passport Photo with:
     * 1. Selected Country Standard aspect ratio & biometric head-size framing
     * 2. Selected Background Color (Pure White default, or Studio Ivory / Blue / Grey / Red)
     * 3. Interactive Crop / Rotate / Scale of the Portrait (plus automatic eye-tilt leveling)
     * 4. Selected Nano Banana 2 Coat / Suit overlay aligned to the user's chin, neck & shoulders
     */
    suspend fun renderPassportPhoto(
        session: CachedPassportSession,
        params: PassportEditParams,
        outputScaleMultiplier: Int = 2
    ): Bitmap = withContext(Dispatchers.Default) {
        val std = params.countryStandard
        val dstW = (std.widthPx300Dpi * outputScaleMultiplier).coerceIn(300, 1600)
        val dstH = (std.heightPx300Dpi * outputScaleMultiplier).coerceIn(300, 1800)
        val numDst = dstW * dstH

        val bg = params.bgColor
        val outPixels = IntArray(numDst)

        // 1. Fill Passport Background (supports pure flat white or subtle studio vertical gradient)
        for (y in 0 until dstH) {
            val t = y.toFloat() / (dstH - 1).coerceAtLeast(1)
            val br = (bg.r + (bg.rBottom - bg.r) * t).roundToInt().coerceIn(0, 255)
            val bgG = (bg.g + (bg.gBottom - bg.g) * t).roundToInt().coerceIn(0, 255)
            val bb = (bg.b + (bg.bBottom - bg.b) * t).roundToInt().coerceIn(0, 255)
            val bgColorInt = (0xFF shl 24) or (br shl 16) or (bgG shl 8) or bb
            val rowOff = y * dstW
            for (x in 0 until dstW) {
                outPixels[rowOff + x] = bgColorInt
            }
        }

        // 2. Compute Biometric Affine Transform mapping Source Head -> Destination Passport Frame
        val geom = session.geometry
        val srcHeadHeight = (geom.chinBottomY - geom.crownTopY).coerceAtLeast(session.height * 0.12f)
        val srcHeadCenterX = geom.headCenterX
        val srcHeadCenterY = (geom.crownTopY + geom.chinBottomY) * 0.5f

        val dstTargetHeadHeight = dstH * std.targetHeadHeightRatio
        val dstTargetCrownY = dstH * std.targetCrownTopRatio
        val dstBaseCenterX = dstW * 0.5f
        val dstBaseCenterY = dstTargetCrownY + dstTargetHeadHeight * 0.5f

        val baseScale = dstTargetHeadHeight / srcHeadHeight
        val effectiveScale = (baseScale * params.photoScale).coerceIn(0.15f, 8.0f)
        val invScale = 1.0f / effectiveScale

        val totalRotDeg = (if (params.autoLevelEyesEnabled) geom.autoEyeTiltDeg else 0f) + params.photoRotationDeg
        val rotRad = Math.toRadians(totalRotDeg.toDouble())
        val cosA = cos(rotRad).toFloat()
        val sinA = sin(rotRad).toFloat()

        val dstHeadCenterX = dstBaseCenterX + params.photoOffsetXNorm * dstW
        val dstHeadCenterY = dstBaseCenterY + params.photoOffsetYNorm * dstH

        val srcPixels = if (params.studioSkinPolish) session.polishedPixels else session.origPixels
        val personAlpha = session.personAlphaMatte
        val clothesMask = session.clothesMask
        val headNeckMask = session.headAndNeckMask
        val skinMask = session.skinMask
        val srcW = session.width
        val srcH = session.height

        val hasCoat = params.selectedCoatId != null
        val hideOldClothes = hasCoat && params.hideOriginalClothesUnderCoat

        // Destination coordinates of Chin Bottom and Neck-Collar Anchor
        val dstChinY = dstHeadCenterY + effectiveScale * (geom.chinBottomY - srcHeadCenterY)
        val dstHeadHeight = srcHeadHeight * effectiveScale
        val dstHeadWidth = geom.headWidthPx * effectiveScale
        val dstNeckWidth = geom.neckWidthPx * effectiveScale

        val dxNeckSrc = geom.neckCenterX - srcHeadCenterX
        val dyNeckSrc = geom.neckCollarY - srcHeadCenterY
        val dstNeckAnchorX = dstHeadCenterX + effectiveScale * (cosA * dxNeckSrc - sinA * dyNeckSrc)
        // Collar wing top anchor: sits naturally just below the chin (not overlapping the jawline)
        val dstNeckAnchorY = dstChinY + dstHeadHeight * 0.04f

        val alphaContrast = 1.15f + (1.0f - params.alphaEdgeFeather.coerceIn(0f, 1f)) * 1.45f

        // 3. Render Segmented Portrait onto the Passport Background
        for (y in 0 until dstH) {
            val dy = y - dstHeadCenterY
            val rowOff = y * dstW
            for (x in 0 until dstW) {
                val dx = x - dstHeadCenterX
                val srcX = srcHeadCenterX + invScale * (cosA * dx + sinA * dy)
                val srcY = srcHeadCenterY + invScale * (-sinA * dx + cosA * dy)

                if (srcX >= 0f && srcX < srcW - 1 && srcY >= 0f && srcY < srcH - 1) {
                    var alpha = sampleBilinearFloat(personAlpha, srcW, srcH, srcX, srcY)
                    if (alpha > 0.02f) {
                        if (hideOldClothes) {
                            val pCloth = sampleBilinearFloat(clothesMask, srcW, srcH, srcX, srcY)
                            val pHeadNeck = sampleBilinearFloat(headNeckMask, srcW, srcH, srcX, srcY)
                            val pSkin = sampleBilinearFloat(skinMask, srcW, srcH, srcX, srcY)

                            // 1) Suppress original clothing, collar tags, and phone under the coat
                            if (y > dstChinY) {
                                val distFromNeckX = abs(x - dstNeckAnchorX)
                                val inNeckCollar = distFromNeckX <= dstNeckWidth * 0.58f
                                if (inNeckCollar) {
                                    // Preserve real neck skin down into collar opening
                                    val neckFade = ((y - (dstChinY + dstHeadHeight * 0.22f)) / (dstHeadHeight * 0.08f)).coerceIn(0f, 1f)
                                    alpha *= (1f - pCloth).coerceIn(0f, 1f) * (1f - neckFade)
                                } else {
                                    // Outside the neck opening, suppress old casual clothes
                                    val sideFade = ((distFromNeckX - dstNeckWidth * 0.58f) / (dstNeckWidth * 0.15f)).coerceIn(0f, 1f)
                                    alpha *= (1f - sideFade)
                                }
                            } else if (y > dstChinY - dstHeadHeight * 0.20f) {
                                // Cheeks & jawline: suppress old clothes/collars that ride up
                                alpha *= (1f - pCloth).coerceIn(0f, 1f)
                            }
                        }

                        if (alpha > 0.02f) {
                            val curvedAlpha = ((alpha - 0.5f) * alphaContrast + 0.5f).coerceIn(0f, 1f)
                            if (curvedAlpha > 0.01f) {
                                val fgRgb = sampleBilinearRgb(srcPixels, srcW, srcH, srcX, srcY)
                                val bgC = outPixels[rowOff + x]
                                val bgR = (bgC shr 16) and 0xFF
                                val bgG = (bgC shr 8) and 0xFF
                                val bgB = bgC and 0xFF

                                val outR = (fgRgb[0] * curvedAlpha + bgR * (1f - curvedAlpha)).roundToInt().coerceIn(0, 255)
                                val outG = (fgRgb[1] * curvedAlpha + bgG * (1f - curvedAlpha)).roundToInt().coerceIn(0, 255)
                                val outB = (fgRgb[2] * curvedAlpha + bgB * (1f - curvedAlpha)).roundToInt().coerceIn(0, 255)
                                outPixels[rowOff + x] = (0xFF shl 24) or (outR shl 16) or (outG shl 8) or outB
                            }
                        }
                    }
                }
            }
        }

        val outBitmap = Bitmap.createBitmap(dstW, dstH, Bitmap.Config.ARGB_8888)
        outBitmap.setPixels(outPixels, 0, dstW, 0, 0, dstW, dstH)

        // 4. Composite Selected Nano Banana 2 Coat / Suit Overlay aligned to Chin, Neck & Shoulders
        if (params.selectedCoatId != null) {
            val coatItem = PassportCoatCatalog.templates.find { it.id == params.selectedCoatId }
            val coatAsset = coatItem?.let { getOrLoadCoatAsset(it) }
            if (coatAsset != null) {
                compositeAlignedCoatOnCanvas(
                    canvasBitmap = outBitmap,
                    session = session,
                    coatAsset = coatAsset,
                    params = params,
                    dstW = dstW,
                    dstH = dstH,
                    dstHeadCenterX = dstHeadCenterX,
                    dstHeadCenterY = dstHeadCenterY,
                    dstNeckAnchorX = dstNeckAnchorX,
                    dstNeckAnchorY = dstNeckAnchorY,
                    dstChinY = dstChinY,
                    dstHeadHeight = dstHeadHeight,
                    effectivePhotoScale = effectiveScale,
                    cosPhoto = cosA,
                    sinPhoto = sinA
                )
            }
        }

        // 5. Optional Biometric Compliance Guides Overlay (Crown, Eye Line, Chin, Center Axis)
        if (params.showBiometricGuides) {
            drawBiometricComplianceGuides(
                canvasBitmap = outBitmap,
                std = std,
                dstW = dstW,
                dstH = dstH
            )
        }

        outBitmap
    }

    /**
     * Aligns and composites the Nano Banana 2 coat onto the passport canvas using our 3-layer
     * anatomical collar model:
     * - `REGION_BACK_COLLAR`: sits behind the user's chin and real neck skin.
     * - `REGION_NECK_HOLE`: inside the shirt's V-neck opening; seamlessly fills any missing/occluded
     *   lower-neck skin with the user's sampled `neckRefRgb` skin tone so old clothes/phones never show
     *   and there is never a white hole inside the collar.
     * - `REGION_FRONT_GARMENT`: front collar lapels, tie knot, shirt & suit jacket sit crisply in front.
     */
    private fun compositeAlignedCoatOnCanvas(
        canvasBitmap: Bitmap,
        session: CachedPassportSession,
        coatAsset: LoadedCoatAsset,
        params: PassportEditParams,
        dstW: Int,
        dstH: Int,
        dstHeadCenterX: Float,
        dstHeadCenterY: Float,
        dstNeckAnchorX: Float,
        dstNeckAnchorY: Float,
        dstChinY: Float,
        dstHeadHeight: Float,
        effectivePhotoScale: Float,
        cosPhoto: Float,
        sinPhoto: Float
    ) {
        val geom = session.geometry
        val dstHeadWidth = geom.headWidthPx * effectivePhotoScale
        val dstNeckWidth = geom.neckWidthPx * effectivePhotoScale

        // Realistic anatomical coat scaling:
        // Match the collar opening to the neck width and jacket shoulders proportional to the head width
        val scaleByNeck = (dstNeckWidth * 1.15f) / coatAsset.collarOpeningWidth.coerceAtLeast(32f)
        val scaleByHead = (dstHeadWidth * 3.35f) / coatAsset.shoulderSpanWidth.coerceAtLeast(64f)
        val baseCoatScale = 0.50f * scaleByNeck + 0.50f * scaleByHead
        val finalCoatScale = (baseCoatScale * coatAsset.item.defaultScaleFactor * params.coatScale)
            .coerceIn(0.20f, 6.0f)

        val targetCollarX = dstNeckAnchorX + params.coatOffsetXNorm * dstW
        val targetCollarY = dstNeckAnchorY + params.coatOffsetYNorm * dstH

        val coatRotDeg = (if (params.autoLevelEyesEnabled) geom.autoEyeTiltDeg * 0.5f else 0f) +
            params.photoRotationDeg + params.coatRotationDeg

        val matrix = Matrix().apply {
            postTranslate(-coatAsset.collarCenterX, -coatAsset.collarJunctionY)
            postScale(finalCoatScale, finalCoatScale)
            postRotate(coatRotDeg)
            postTranslate(targetCollarX, targetCollarY)
        }

        val invCoatMatrix = Matrix()
        matrix.invert(invCoatMatrix)
        val invVals = FloatArray(9)
        invCoatMatrix.getValues(invVals)
        val m00 = invVals[Matrix.MSCALE_X]
        val m01 = invVals[Matrix.MSKEW_X]
        val m02 = invVals[Matrix.MTRANS_X]
        val m10 = invVals[Matrix.MSKEW_Y]
        val m11 = invVals[Matrix.MSCALE_Y]
        val m12 = invVals[Matrix.MTRANS_Y]

        // Render coat bitmap to an intermediate layer
        val coatLayer = Bitmap.createBitmap(dstW, dstH, Bitmap.Config.ARGB_8888)
        val coatCanvas = Canvas(coatLayer)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        coatCanvas.drawBitmap(coatAsset.bitmap, matrix, paint)

        val coatPixels = IntArray(dstW * dstH)
        coatLayer.getPixels(coatPixels, 0, dstW, 0, 0, dstW, dstH)
        extendCoatToBottomEdgeIfNeeded(coatPixels, dstW, dstH)

        val basePixels = IntArray(dstW * dstH)
        canvasBitmap.getPixels(basePixels, 0, dstW, 0, 0, dstW, dstH)

        val invPhotoScale = 1.0f / effectivePhotoScale
        val srcPixels = if (params.studioSkinPolish) session.polishedPixels else session.origPixels
        val srcW = session.width
        val srcH = session.height
        val srcHeadCenterX = geom.headCenterX
        val srcHeadCenterY = (geom.crownTopY + geom.chinBottomY) * 0.5f

        val srcHeadHeight = (geom.chinBottomY - geom.crownTopY).coerceAtLeast(session.height * 0.12f)
        val cw = coatAsset.bitmap.width
        val ch = coatAsset.bitmap.height
        val regionMap = coatAsset.regionMap
        val holeTopY = coatAsset.holeTopY
        val holeSpanY = (coatAsset.holeBottomY - coatAsset.holeTopY).coerceAtLeast(16f)
        val collarHalfW = (coatAsset.collarOpeningWidth * 0.5f).coerceAtLeast(16f)

        val refR = session.neckRefRgb[0]
        val refG = session.neckRefRgb[1]
        val refB = session.neckRefRgb[2]

        for (y in 0 until dstH) {
            val rowOff = y * dstW
            for (x in 0 until dstW) {
                val idx = rowOff + x

                // Map destination (x, y) into coat bitmap coordinates (coatX, coatY)
                val coatXf = m00 * x + m01 * y + m02
                val coatYf = m10 * x + m11 * y + m12
                val coatXi = coatXf.roundToInt()
                val coatYi = coatYf.roundToInt()

                val region: Byte = if (coatXi in 0 until cw && coatYi in 0 until ch && regionMap.isNotEmpty()) {
                    regionMap[coatYi * cw + coatXi]
                } else {
                    REGION_OUTER_TRANSPARENT
                }

                val cCoat = coatPixels[idx]
                val coatA = ((cCoat ushr 24) and 0xFF) / 255f
                val coatR = (cCoat shr 16) and 0xFF
                val coatG = (cCoat shr 8) and 0xFF
                val coatB = cCoat and 0xFF

                if (region == REGION_NECK_HOLE) {
                    // Inside the shirt's V-neck / collar opening:
                    // Directly pass through the user's continuous real neck skin down into the collar opening
                    val dx = x - dstHeadCenterX
                    val dy = y - dstHeadCenterY
                    val srcX = srcHeadCenterX + invPhotoScale * (cosPhoto * dx + sinPhoto * dy)
                    val srcY = srcHeadCenterY + invPhotoScale * (-sinPhoto * dx + cosPhoto * dy)

                    var neckR = refR
                    var neckG = refG
                    var neckB = refB

                    if (srcX in 0f..(srcW - 1f) && srcY in 0f..(srcH - 1f)) {
                        val rgb = sampleBilinearRgb(srcPixels, srcW, srcH, srcX, srcY)
                        neckR = rgb[0]
                        neckG = rgb[1]
                        neckB = rgb[2]
                    }

                    // Soft anatomical shading inside the collar
                    val uNorm = ((coatXf - coatAsset.collarCenterX) / collarHalfW).coerceIn(-1f, 1f)
                    val vNorm = ((coatYf - holeTopY) / holeSpanY).coerceIn(0f, 1f)
                    val underChinShadow = (1f - vNorm).let { it * it } * 0.06f
                    val lateralCollarShadow = (uNorm * uNorm) * 0.05f
                    val shadeFactor = (0.98f - underChinShadow - lateralCollarShadow).coerceIn(0.85f, 1.0f)

                    val shadedNeckR = neckR * shadeFactor
                    val shadedNeckG = neckG * shadeFactor
                    val shadedNeckB = neckB * shadeFactor

                    // Blend any anti-aliased inner collar edge pixel on top of the real neck skin
                    val outR = (coatR * coatA + shadedNeckR * (1f - coatA)).roundToInt().coerceIn(0, 255)
                    val outG = (coatG * coatA + shadedNeckG * (1f - coatA)).roundToInt().coerceIn(0, 255)
                    val outB = (coatB * coatA + shadedNeckB * (1f - coatA)).roundToInt().coerceIn(0, 255)
                    basePixels[idx] = (0xFF shl 24) or (outR shl 16) or (outG shl 8) or outB
                    continue
                }

                if (coatA <= 0.005f) continue

                if (region == REGION_BACK_COLLAR) {
                    // The back collar band sits BEHIND the user's chin and real neck skin
                    val dx = x - dstHeadCenterX
                    val dy = y - dstHeadCenterY
                    val srcX = srcHeadCenterX + invPhotoScale * (cosPhoto * dx + sinPhoto * dy)
                    val srcY = srcHeadCenterY + invPhotoScale * (-sinPhoto * dx + cosPhoto * dy)

                    var frontSkinKeep = 0f
                    var fgR = 0f
                    var fgG = 0f
                    var fgB = 0f
                    if (srcX >= 0f && srcX < srcW - 1 && srcY >= 0f && srcY < srcH - 1) {
                        val pSkin = sampleBilinearFloat(session.skinMask, srcW, srcH, srcX, srcY)
                        val pHeadNeck = sampleBilinearFloat(session.headAndNeckMask, srcW, srcH, srcX, srcY)
                        val pCloth = sampleBilinearFloat(session.clothesMask, srcW, srcH, srcX, srcY)
                        val distFromNeckX = abs(x - dstNeckAnchorX)
                        val inNeckCol = ((dstNeckWidth * 0.50f - distFromNeckX) / (dstNeckWidth * 0.08f))
                            .coerceIn(0f, 1f)
                        val aboveChin = ((dstChinY - y) / (dstHeadHeight * 0.04f)).coerceIn(0f, 1f)
                        val spatialKeep = max(aboveChin, inNeckCol)
                        frontSkinKeep = (pSkin * pHeadNeck * (1f - pCloth) * spatialKeep * 1.4f).coerceIn(0f, 1f)
                        if (frontSkinKeep > 0.01f) {
                            val rgb = sampleBilinearRgb(srcPixels, srcW, srcH, srcX, srcY)
                            fgR = rgb[0]
                            fgG = rgb[1]
                            fgB = rgb[2]
                        }
                    }

                    val effectiveCoatAlpha = (coatA * (1f - frontSkinKeep)).coerceIn(0f, 1f)
                    val baseC = basePixels[idx]
                    val baseR = if (frontSkinKeep > 0.05f) fgR else ((baseC shr 16) and 0xFF).toFloat()
                    val baseG = if (frontSkinKeep > 0.05f) fgG else ((baseC shr 8) and 0xFF).toFloat()
                    val baseB = if (frontSkinKeep > 0.05f) fgB else (baseC and 0xFF).toFloat()

                    val outR = (coatR * effectiveCoatAlpha + baseR * (1f - effectiveCoatAlpha)).roundToInt().coerceIn(0, 255)
                    val outG = (coatG * effectiveCoatAlpha + baseG * (1f - effectiveCoatAlpha)).roundToInt().coerceIn(0, 255)
                    val outB = (coatB * effectiveCoatAlpha + baseB * (1f - effectiveCoatAlpha)).roundToInt().coerceIn(0, 255)
                    basePixels[idx] = (0xFF shl 24) or (outR shl 16) or (outG shl 8) or outB
                    continue
                }

                // REGION_FRONT_GARMENT: Front collar lapels, tie, shirt & suit jacket
                // Keep the user's chin tip in front only if the coat overlaps above `dstChinY`
                var chinTipKeep = 0f
                if (y < dstChinY) {
                    val dx = x - dstHeadCenterX
                    val dy = y - dstHeadCenterY
                    val srcX = srcHeadCenterX + invPhotoScale * (cosPhoto * dx + sinPhoto * dy)
                    val srcY = srcHeadCenterY + invPhotoScale * (-sinPhoto * dx + cosPhoto * dy)
                    if (srcX >= 0f && srcX < srcW - 1 && srcY >= 0f && srcY < srcH - 1 &&
                        abs(x - dstHeadCenterX) < dstHeadWidth * 0.36f
                    ) {
                        val pSkin = sampleBilinearFloat(session.skinMask, srcW, srcH, srcX, srcY)
                        val chinBand = ((dstChinY - y) / (dstHeadHeight * 0.04f)).coerceIn(0f, 1f)
                        chinTipKeep = (pSkin * chinBand).coerceIn(0f, 1f)
                    }
                }

                val effectiveCoatAlpha = (coatA * (1f - chinTipKeep)).coerceIn(0f, 1f)
                if (effectiveCoatAlpha > 0.005f) {
                    val baseC = basePixels[idx]
                    val baseR = (baseC shr 16) and 0xFF
                    val baseG = (baseC shr 8) and 0xFF
                    val baseB = baseC and 0xFF

                    val outR = (coatR * effectiveCoatAlpha + baseR * (1f - effectiveCoatAlpha)).roundToInt().coerceIn(0, 255)
                    val outG = (coatG * effectiveCoatAlpha + baseG * (1f - effectiveCoatAlpha)).roundToInt().coerceIn(0, 255)
                    val outB = (coatB * effectiveCoatAlpha + baseB * (1f - effectiveCoatAlpha)).roundToInt().coerceIn(0, 255)
                    basePixels[idx] = (0xFF shl 24) or (outR shl 16) or (outG shl 8) or outB
                }
            }
        }

        canvasBitmap.setPixels(basePixels, 0, dstW, 0, 0, dstW, dstH)
    }

    private fun extendCoatToBottomEdgeIfNeeded(coatPixels: IntArray, w: Int, h: Int) {
        var lastGoodRow = -1
        for (y in h - 1 downTo (h / 2)) {
            var count = 0
            val rowOff = y * w
            for (x in 0 until w) {
                if (((coatPixels[rowOff + x] ushr 24) and 0xFF) > 180) count++
            }
            if (count > w * 0.38f) {
                lastGoodRow = y
                break
            }
        }
        if (lastGoodRow in (h / 2) until (h - 1)) {
            val srcRowOff = lastGoodRow * w
            for (y in (lastGoodRow + 1) until h) {
                System.arraycopy(coatPixels, srcRowOff, coatPixels, y * w, w)
            }
        }
    }

    /**
     * Generates a print-ready bitmap for export:
     * - [ExportLayoutMode.SINGLE_PASSPORT]: High-resolution exact aspect ratio passport photo.
     * - [ExportLayoutMode.PRINT_SHEET_4X6]: Standard 4×6 inch (1800×1200 px at 300 DPI) multi-photo sheet with cut lines.
     */
    suspend fun renderExportBitmap(
        session: CachedPassportSession,
        params: PassportEditParams
    ): Bitmap = withContext(Dispatchers.Default) {
        val cleanParams = params.copy(showBiometricGuides = false)
        val singleHighRes = renderPassportPhoto(session, cleanParams, outputScaleMultiplier = 2)
        val std = params.countryStandard

        val single300Dpi = Bitmap.createScaledBitmap(
            singleHighRes,
            std.widthPx300Dpi,
            std.heightPx300Dpi,
            true
        )

        if (params.exportLayoutMode == ExportLayoutMode.SINGLE_PASSPORT) {
            return@withContext singleHighRes
        }

        // Build a 4x6 inch (1800 x 1200 px at 300 DPI) multi-copy print sheet
        val sheetW = 1800
        val sheetH = 1200
        val sheetBmp = Bitmap.createBitmap(sheetW, sheetH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(sheetBmp)
        canvas.drawColor(Color.WHITE)

        val photoW = std.widthPx300Dpi
        val photoH = std.heightPx300Dpi
        val gap = 36

        val cols = ((sheetW - 80) / (photoW + gap)).coerceIn(2, 4)
        val rows = ((sheetH - 100) / (photoH + gap)).coerceIn(1, 2)

        val totalGridW = cols * photoW + (cols - 1) * gap
        val totalGridH = rows * photoH + (rows - 1) * gap
        val startX = (sheetW - totalGridW) / 2f
        val startY = (sheetH - totalGridH) / 2f

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.argb(140, 160, 165, 175)
            strokeWidth = 2f
        }
        val cropMarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.argb(180, 110, 115, 125)
            strokeWidth = 2f
        }

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val left = startX + c * (photoW + gap)
                val top = startY + r * (photoH + gap)
                canvas.drawBitmap(single300Dpi, left, top, null)
                canvas.drawRect(left, top, left + photoW, top + photoH, borderPaint)

                val m = 14f
                canvas.drawLine(left - m, top, left + m, top, cropMarkPaint)
                canvas.drawLine(left, top - m, left, top + m, cropMarkPaint)
                canvas.drawLine(left + photoW - m, top, left + photoW + m, top, cropMarkPaint)
                canvas.drawLine(left + photoW, top - m, left + photoW, top + m, cropMarkPaint)
                canvas.drawLine(left - m, top + photoH, left + m, top + photoH, cropMarkPaint)
                canvas.drawLine(left, top + photoH - m, left, top + photoH + m, cropMarkPaint)
                canvas.drawLine(left + photoW - m, top + photoH, left + photoW + m, top + photoH, cropMarkPaint)
                canvas.drawLine(left + photoW, top + photoH - m, left + photoW, top + photoH + m, cropMarkPaint)
            }
        }

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(110, 115, 125)
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "${std.countryName} · ${std.specLabel} · ${cols * rows} Photos on 4×6\" (300 DPI Print Sheet)",
            sheetW / 2f,
            sheetH - 26f,
            labelPaint
        )

        sheetBmp
    }

    private fun drawBiometricComplianceGuides(
        canvasBitmap: Bitmap,
        std: CountryPassportStandard,
        dstW: Int,
        dstH: Int
    ) {
        val canvas = Canvas(canvasBitmap)
        val dashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.argb(190, 16, 185, 129)
            strokeWidth = 2.5f
            pathEffect = DashPathEffect(floatArrayOf(12f, 8f), 0f)
        }
        val ovalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = Color.argb(165, 56, 189, 248)
            strokeWidth = 2.5f
            pathEffect = DashPathEffect(floatArrayOf(14f, 8f), 0f)
        }

        val crownY = dstH * std.targetCrownTopRatio
        val chinY = crownY + dstH * std.targetHeadHeightRatio
        val eyeLineY = crownY + (chinY - crownY) * 0.46f
        val centerX = dstW * 0.5f

        canvas.drawLine(centerX, 0f, centerX, dstH.toFloat(), dashPaint)
        canvas.drawLine(dstW * 0.12f, crownY, dstW * 0.88f, crownY, dashPaint)
        canvas.drawLine(dstW * 0.08f, eyeLineY, dstW * 0.92f, eyeLineY, dashPaint)
        canvas.drawLine(dstW * 0.18f, chinY, dstW * 0.82f, chinY, dashPaint)

        val headH = chinY - crownY
        val headW = headH * 0.72f
        canvas.drawOval(
            RectF(centerX - headW * 0.5f, crownY, centerX + headW * 0.5f, chinY),
            ovalPaint
        )
    }

    // =========================================================================
    // MODEL 1: MediaPipe 6-Class Multiclass Segmenter (256x256 TFLite)
    // =========================================================================
    private fun runMediaPipeMulticlassForPassport(
        sourceBitmap: Bitmap,
        outPerson256: FloatArray,
        outClothes256: FloatArray,
        outHeadNeck256: FloatArray,
        outHeadHair256: FloatArray,
        outFaceSkin256: FloatArray,
        outBodySkin256: FloatArray
    ): RectF {
        val scaled = Bitmap.createScaledBitmap(sourceBitmap, 256, 256, true)
        val pixels = IntArray(256 * 256)
        scaled.getPixels(pixels, 0, 256, 0, 0, 256, 256)

        val interpreter = selfieInterpreter
        if (interpreter != null) {
            try {
                val inBuf = ByteBuffer.allocateDirect(256 * 256 * 3 * 4).order(ByteOrder.nativeOrder())
                for (c in pixels) {
                    inBuf.putFloat(((c shr 16) and 0xFF) / 255f)
                    inBuf.putFloat(((c shr 8) and 0xFF) / 255f)
                    inBuf.putFloat((c and 0xFF) / 255f)
                }
                inBuf.rewind()

                val outBuf = ByteBuffer.allocateDirect(256 * 256 * 6 * 4).order(ByteOrder.nativeOrder())
                interpreter.run(inBuf, outBuf)
                outBuf.rewind()

                val probs = FloatArray(6)
                var minX = 256f
                var minY = 256f
                var maxX = 0f
                var maxY = 0f
                var sumFaceWeight = 0f

                for (idx in 0 until 256 * 256) {
                    var needsSoftmax = false
                    var sum = 0f
                    for (k in 0 until 6) {
                        val v = outBuf.float
                        probs[k] = v
                        if (v < -0.001f || v > 1.001f) needsSoftmax = true
                        sum += v
                    }
                    if (needsSoftmax || abs(sum - 1f) > 0.1f) {
                        var maxLogit = probs[0]
                        for (k in 1 until 6) if (probs[k] > maxLogit) maxLogit = probs[k]
                        var expSum = 0f
                        for (k in 0 until 6) {
                            probs[k] = exp((probs[k] - maxLogit).coerceIn(-20f, 20f))
                            expSum += probs[k]
                        }
                        for (k in 0 until 6) probs[k] /= expSum.coerceAtLeast(1e-6f)
                    }

                    // Classes: 0=background, 1=hair, 2=body-skin, 3=face-skin, 4=clothes, 5=others(phone/accessories)
                    val pHair = probs[1]
                    val pBody = probs[2]
                    val pFace = probs[3]
                    val pCloth = probs[4]
                    val pOthers = probs[5]

                    // Explicitly exclude class 0 (background) AND class 5 (handheld phones/objects) from person mask!
                    outPerson256[idx] = (pHair + pFace + pBody + pCloth).coerceIn(0f, 1f)
                    // Treat both casual clothes (4) and handheld objects/phones (5) as removable clutter under the suit
                    outClothes256[idx] = (pCloth + pOthers).coerceIn(0f, 1f)
                    outHeadNeck256[idx] = (pHair + pFace + pBody).coerceIn(0f, 1f)
                    outHeadHair256[idx] = (pHair + pFace).coerceIn(0f, 1f)
                    outFaceSkin256[idx] = pFace.coerceIn(0f, 1f)
                    outBodySkin256[idx] = pBody.coerceIn(0f, 1f)

                    if (pFace > 0.25f) {
                        val x = (idx % 256).toFloat()
                        val y = (idx / 256).toFloat()
                        if (x < minX) minX = x
                        if (x > maxX) maxX = x
                        if (y < minY) minY = y
                        if (y > maxY) maxY = y
                        sumFaceWeight += pFace
                    }
                }

                if (sumFaceWeight > 10f && maxX > minX && maxY > minY) {
                    return RectF(minX / 256f, minY / 256f, maxX / 256f, maxY / 256f)
                }
            } catch (e: Exception) {
                Log.e(TAG, "MediaPipe Multiclass passport inference error", e)
            }
        }

        return RectF(0.25f, 0.15f, 0.75f, 0.55f)
    }

    private fun computePassportFaceNeckCrop(faceBBoxNorm: RectF, width: Int, height: Int): Rect {
        val cx = ((faceBBoxNorm.left + faceBBoxNorm.right) * 0.5f * width)
        val cy = ((faceBBoxNorm.top + faceBBoxNorm.bottom) * 0.5f * height) + faceBBoxNorm.height() * height * 0.08f
        val boxW = (faceBBoxNorm.width() * width).coerceAtLeast(width * 0.18f)
        val boxH = (faceBBoxNorm.height() * height).coerceAtLeast(height * 0.18f)
        val side = (max(boxW, boxH) * 2.05f).roundToInt().coerceIn(128, min(width, height))
        val half = side / 2
        val left = (cx.roundToInt() - half).coerceIn(0, max(0, width - side))
        val top = (cy.roundToInt() - half).coerceIn(0, max(0, height - side))
        return Rect(left, top, (left + side).coerceAtMost(width), (top + side).coerceAtMost(height))
    }

    private fun computePassportModnetCrop(faceBBoxNorm: RectF, width: Int, height: Int): Rect {
        val cx = ((faceBBoxNorm.left + faceBBoxNorm.right) * 0.5f * width)
        val cy = ((faceBBoxNorm.top + faceBBoxNorm.bottom) * 0.5f * height) + faceBBoxNorm.height() * height * 0.35f
        val boxW = (faceBBoxNorm.width() * width).coerceAtLeast(width * 0.20f)
        val boxH = (faceBBoxNorm.height() * height).coerceAtLeast(height * 0.20f)
        val side = (max(boxW, boxH) * 3.10f).roundToInt().coerceIn(256, max(width, height))
        val half = side / 2
        val left = (cx.roundToInt() - half).coerceIn(0, max(0, width - side))
        val top = (cy.roundToInt() - half).coerceIn(0, max(0, height - side))
        return Rect(left, top, (left + side).coerceAtMost(width), (top + side).coerceAtMost(height))
    }

    // =========================================================================
    // MODEL 2: RMBG-1.4 SOTA Neural Matting (1024x1024 ONNX)
    // =========================================================================
    private fun runRmbgMatting1024(
        sourceBitmap: Bitmap,
        fullWidth: Int,
        fullHeight: Int,
        outMatte: FloatArray
    ): Boolean {
        val session = rmbgSession ?: return false
        val env = ortEnv ?: return false

        return try {
            val scaledBmp = Bitmap.createScaledBitmap(sourceBitmap, 1024, 1024, true)
            val pixels = IntArray(1024 * 1024)
            scaledBmp.getPixels(pixels, 0, 1024, 0, 0, 1024, 1024)

            val hw = 1024 * 1024
            val inFloats = FloatArray(3 * hw)
            // Official BRIA RMBG-1.4 normalization: (val / 255.0 - 0.5) / 1.0 = (val / 255.0 - 0.5)
            for (i in 0 until hw) {
                val c = pixels[i]
                val r = (c shr 16) and 0xFF
                val g = (c shr 8) and 0xFF
                val b = c and 0xFF
                inFloats[i] = r / 255.0f - 0.5f
                inFloats[hw + i] = g / 255.0f - 0.5f
                inFloats[2 * hw + i] = b / 255.0f - 0.5f
            }

            val inputName = session.inputNames.first()
            val rawMatte1024 = FloatArray(hw)
            OnnxTensor.createTensor(env, FloatBuffer.wrap(inFloats), longArrayOf(1, 3, 1024, 1024)).use { inputTensor ->
                session.run(mapOf(inputName to inputTensor)).use { results ->
                    val outTensor = results[0] as OnnxTensor
                    val fb = outTensor.floatBuffer
                    fb.rewind()
                    val count = min(hw, fb.remaining())
                    var minVal = Float.MAX_VALUE
                    var maxVal = -Float.MAX_VALUE
                    for (i in 0 until count) {
                        val v = fb.get(i)
                        rawMatte1024[i] = v
                        if (v < minVal) minVal = v
                        if (v > maxVal) maxVal = v
                    }
                    val range = (maxVal - minVal).coerceAtLeast(1e-6f)
                    for (i in 0 until count) {
                        rawMatte1024[i] = ((rawMatte1024[i] - minVal) / range).coerceIn(0f, 1f)
                    }
                }
            }

            // Resample 1024x1024 matte to source full resolution (fullWidth x fullHeight)
            val invW = 1023f / (fullWidth - 1).coerceAtLeast(1)
            val invH = 1023f / (fullHeight - 1).coerceAtLeast(1)
            for (y in 0 until fullHeight) {
                val my = y * invH
                val rowOff = y * fullWidth
                for (x in 0 until fullWidth) {
                    val mx = x * invW
                    outMatte[rowOff + x] = sampleBilinearFloat(rawMatte1024, 1024, 1024, mx, my)
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "RMBG-1.4 1024x1024 inference failed", e)
            false
        }
    }

    // =========================================================================
    // MODEL 3: BiSeNet 19-Class CelebAMask-HQ Face & Neck Parser (512x512)
    // =========================================================================
    private fun runBiSeNetPassportParsing(
        sourceBitmap: Bitmap,
        cropRect: Rect,
        fullWidth: Int,
        fullHeight: Int,
        mpPerson256: FloatArray,
        mpHeadHair256: FloatArray,
        mpFaceSkin256: FloatArray,
        outPerson512: FloatArray,
        outHeadNeck512: FloatArray,
        outCloth512: FloatArray,
        outSkin512: FloatArray
    ): PassportBiometricGeometry {
        val cropL = cropRect.left.toFloat()
        val cropT = cropRect.top.toFloat()
        val scaleX = cropRect.width().toFloat() / 512f
        val scaleY = cropRect.height().toFloat() / 512f

        val interpreter = bisenetInterpreter
        if (interpreter != null) {
            try {
                val cropBmp = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
                Canvas(cropBmp).drawBitmap(
                    sourceBitmap,
                    cropRect,
                    Rect(0, 0, 512, 512),
                    Paint(Paint.FILTER_BITMAP_FLAG)
                )
                val pixels = IntArray(512 * 512)
                cropBmp.getPixels(pixels, 0, 512, 0, 0, 512, 512)

                val hw = 512 * 512
                val inBuf = ByteBuffer.allocateDirect(3 * hw * 4).order(ByteOrder.nativeOrder())
                for (i in 0 until hw) {
                    val r = ((pixels[i] shr 16) and 0xFF) / 255f
                    inBuf.putFloat((r - 0.485f) / 0.229f)
                }
                for (i in 0 until hw) {
                    val g = ((pixels[i] shr 8) and 0xFF) / 255f
                    inBuf.putFloat((g - 0.456f) / 0.224f)
                }
                for (i in 0 until hw) {
                    val b = (pixels[i] and 0xFF) / 255f
                    inBuf.putFloat((b - 0.406f) / 0.225f)
                }
                inBuf.rewind()

                val outBuf = ByteBuffer.allocateDirect(19 * hw * 4).order(ByteOrder.nativeOrder())
                interpreter.run(inBuf, outBuf)
                outBuf.rewind()

                val floatBuf = outBuf.asFloatBuffer()
                val cLogits = FloatArray(19)
                val eyeProb512 = FloatArray(hw)

                var sumFaceX = 0f
                var sumFaceY = 0f
                var sumFaceW = 0f

                var sumNoseX = 0f
                var sumNoseY = 0f
                var sumNoseW = 0f

                var sumMouthX = 0f
                var sumMouthY = 0f
                var sumMouthW = 0f

                var sumNeckX = 0f
                var sumNeckY = 0f
                var sumNeckW = 0f

                var minHeadY = 512f
                var maxFaceSkinY = 0f
                var minFaceX = 512f
                var maxFaceX = 0f
                var minNeckX = 512f
                var maxNeckX = 0f

                val invFullW256 = 255f / (fullWidth - 1).coerceAtLeast(1)
                val invFullH256 = 255f / (fullHeight - 1).coerceAtLeast(1)

                for (i in 0 until hw) {
                    var maxL = -1e9f
                    for (c in 0 until 19) {
                        val v = floatBuf.get(c * hw + i)
                        cLogits[c] = v
                        if (v > maxL) maxL = v
                    }
                    var sumExp = 0f
                    for (c in 0 until 19) {
                        val e = exp((cLogits[c] - maxL).coerceIn(-16f, 16f))
                        cLogits[c] = e
                        sumExp += e
                    }
                    val invSum = 1f / sumExp.coerceAtLeast(1e-6f)

                    val px = (i % 512).toFloat()
                    val py = (i / 512).toFloat()
                    val fullX = cropL + px * scaleX
                    val fullY = cropT + py * scaleY

                    // Gate BiSeNet with MediaPipe 6-Class so background objects (like bathroom arches/frames)
                    // are NEVER misclassified as hair/hat by BiSeNet!
                    val mpPerson = sampleBilinearFloat(mpPerson256, 256, 256, fullX * invFullW256, fullY * invFullH256)
                    val mpHeadHair = sampleBilinearFloat(mpHeadHair256, 256, 256, fullX * invFullW256, fullY * invFullH256)
                    val mpFace = sampleBilinearFloat(mpFaceSkin256, 256, 256, fullX * invFullW256, fullY * invFullH256)

                    val mpPersonGate = ((mpPerson - 0.20f) / 0.35f).coerceIn(0f, 1f)
                    val mpHairGate = ((mpHeadHair - 0.22f) / 0.35f).coerceIn(0f, 1f)

                    val pFaceSkin = (cLogits[1] + cLogits[10]) * invSum
                    val pEars = (cLogits[7] + cLogits[8]) * invSum
                    val pNeck = (cLogits[14] + cLogits[15]) * invSum
                    val pCloth = cLogits[16] * invSum
                    val pHairHat = ((cLogits[17] + cLogits[18]) * invSum) * mpHairGate
                    val pEyes = (cLogits[4] + cLogits[5]) * invSum
                    val pBrows = (cLogits[2] + cLogits[3]) * invSum
                    val pNose = cLogits[10] * invSum
                    val pMouth = (cLogits[11] + cLogits[12] + cLogits[13]) * invSum

                    val pAllSkin = ((pFaceSkin + pEars + pNeck) * mpPersonGate).coerceIn(0f, 1f)
                    val pHeadNeck = ((pAllSkin + pHairHat + pEyes + pBrows + pMouth) * mpPersonGate).coerceIn(0f, 1f)
                    val pPerson = ((pHeadNeck + pCloth) * mpPersonGate).coerceIn(0f, 1f)

                    outPerson512[i] = pPerson
                    outHeadNeck512[i] = pHeadNeck
                    outCloth512[i] = pCloth.coerceIn(0f, 1f)
                    outSkin512[i] = pAllSkin
                    eyeProb512[i] = pEyes

                    if (pFaceSkin > 0.35f && mpFace > 0.20f) {
                        sumFaceX += px * pFaceSkin
                        sumFaceY += py * pFaceSkin
                        sumFaceW += pFaceSkin
                        if (py > maxFaceSkinY) maxFaceSkinY = py
                        if (px < minFaceX) minFaceX = px
                        if (px > maxFaceX) maxFaceX = px
                    }
                    // True hair crown detection: requires BOTH BiSeNet hair/face AND MediaPipe hair/face > 0.45f
                    if ((pFaceSkin + pHairHat) > 0.48f && mpHeadHair > 0.48f && px in 120f..392f) {
                        if (py < minHeadY) minHeadY = py
                    }
                    if (pNose > 0.28f) {
                        sumNoseX += px * pNose
                        sumNoseY += py * pNose
                        sumNoseW += pNose
                    }
                    if (pMouth > 0.25f) {
                        sumMouthX += px * pMouth
                        sumMouthY += py * pMouth
                        sumMouthW += pMouth
                    }
                    if (pNeck > 0.30f && mpPersonGate > 0.4f) {
                        sumNeckX += px * pNeck
                        sumNeckY += py * pNeck
                        sumNeckW += pNeck
                        if (px < minNeckX) minNeckX = px
                        if (px > maxNeckX) maxNeckX = px
                    }
                }

                val splitX = when {
                    sumNoseW > 15f -> sumNoseX / sumNoseW
                    sumFaceW > 40f -> sumFaceX / sumFaceW
                    else -> 256f
                }

                var sumLeftEyeX = 0f
                var sumLeftEyeY = 0f
                var sumLeftEyeW = 0f
                var sumRightEyeX = 0f
                var sumRightEyeY = 0f
                var sumRightEyeW = 0f

                for (i in 0 until hw) {
                    val w = eyeProb512[i]
                    if (w > 0.18f) {
                        val px = (i % 512).toFloat()
                        val py = (i / 512).toFloat()
                        if (px < splitX) {
                            sumLeftEyeX += px * w
                            sumLeftEyeY += py * w
                            sumLeftEyeW += w
                        } else {
                            sumRightEyeX += px * w
                            sumRightEyeY += py * w
                            sumRightEyeW += w
                        }
                    }
                }

                val lx = if (sumLeftEyeW > 6f) cropL + (sumLeftEyeX / sumLeftEyeW) * scaleX else cropRect.exactCenterX() - cropRect.width() * 0.12f
                val ly = if (sumLeftEyeW > 6f) cropT + (sumLeftEyeY / sumLeftEyeW) * scaleY else cropRect.top + cropRect.height() * 0.38f
                val rx = if (sumRightEyeW > 6f) cropL + (sumRightEyeX / sumRightEyeW) * scaleX else cropRect.exactCenterX() + cropRect.width() * 0.12f
                val ry = if (sumRightEyeW > 6f) cropT + (sumRightEyeY / sumRightEyeW) * scaleY else cropRect.top + cropRect.height() * 0.38f

                val eyeMidX = (lx + rx) * 0.5f
                val eyeMidY = (ly + ry) * 0.5f
                val eyeDist = hypot(rx - lx, ry - ly).coerceAtLeast(cropRect.width() * 0.14f)

                val nx = if (sumNoseW > 10f) cropL + (sumNoseX / sumNoseW) * scaleX else eyeMidX
                val ny = if (sumNoseW > 10f) cropT + (sumNoseY / sumNoseW) * scaleY else eyeMidY + eyeDist * 0.52f
                val mx = if (sumMouthW > 10f) cropL + (sumMouthX / sumMouthW) * scaleX else eyeMidX
                val my = if (sumMouthW > 10f) cropT + (sumMouthY / sumMouthW) * scaleY else eyeMidY + eyeDist * 1.05f

                // Biometric Chin Bottom in full image pixels (tightly bounded below mouth)
                val rawChinY = if (maxFaceSkinY > 50f) cropT + maxFaceSkinY * scaleY else my + eyeDist * 0.62f
                val chinBottomY = rawChinY.coerceIn(my + eyeDist * 0.42f, my + eyeDist * 0.85f)

                // Biometric Crown Top in full image pixels (tightly bounded above eyes so background arches never inflate head height)
                val rawCrownY = if (minHeadY < 450f) cropT + minHeadY * scaleY else eyeMidY - eyeDist * 1.48f
                val crownTopY = rawCrownY.coerceIn(
                    (eyeMidY - eyeDist * 1.75f).coerceAtLeast(0f),
                    (eyeMidY - eyeDist * 1.18f).coerceAtLeast(0f)
                )

                val headWidthPx = if (maxFaceX > minFaceX + 30f) {
                    ((maxFaceX - minFaceX) * scaleX).coerceIn(eyeDist * 1.95f, eyeDist * 2.55f)
                } else {
                    eyeDist * 2.22f
                }

                val headCenterX = if (sumFaceW > 40f) {
                    0.55f * eyeMidX + 0.45f * (cropL + (sumFaceX / sumFaceW) * scaleX)
                } else {
                    eyeMidX
                }

                val neckCenterX = if (sumNeckW > 25f) {
                    0.45f * headCenterX + 0.55f * (cropL + (sumNeckX / sumNeckW) * scaleX)
                } else {
                    headCenterX
                }
                val headHeightPx = (chinBottomY - crownTopY).coerceAtLeast(eyeDist * 2.1f)
                val neckCollarY = chinBottomY + headHeightPx * 0.04f
                val neckWidthPx = if (maxNeckX > minNeckX + 20f) {
                    ((maxNeckX - minNeckX) * scaleX).coerceIn(headWidthPx * 0.52f, headWidthPx * 0.78f)
                } else {
                    headWidthPx * 0.62f
                }

                val rawTiltDeg = -Math.toDegrees(atan2((ry - ly).toDouble(), (rx - lx).toDouble())).toFloat()
                val autoEyeTiltDeg = if (abs(rawTiltDeg) <= 15f) rawTiltDeg else 0f

                return PassportBiometricGeometry(
                    leftEyeX = lx,
                    leftEyeY = ly,
                    rightEyeX = rx,
                    rightEyeY = ry,
                    noseX = nx,
                    noseY = ny,
                    mouthX = mx,
                    mouthY = my,
                    crownTopY = crownTopY,
                    chinBottomY = chinBottomY,
                    headCenterX = headCenterX,
                    headWidthPx = headWidthPx,
                    neckCenterX = neckCenterX,
                    neckCollarY = neckCollarY,
                    neckWidthPx = neckWidthPx,
                    autoEyeTiltDeg = autoEyeTiltDeg,
                    activeModels = emptyList(),
                    inferenceTimeMs = 0L
                )
            } catch (e: Exception) {
                Log.e(TAG, "BiSeNet passport parsing error", e)
            }
        }

        val cx = cropRect.exactCenterX()
        val cy = cropRect.exactCenterY()
        val fw = cropRect.width() * 0.45f
        val fh = cropRect.height() * 0.55f
        return PassportBiometricGeometry(
            leftEyeX = cx - fw * 0.25f,
            leftEyeY = cy - fh * 0.12f,
            rightEyeX = cx + fw * 0.25f,
            rightEyeY = cy - fh * 0.12f,
            noseX = cx,
            noseY = cy + fh * 0.05f,
            mouthX = cx,
            mouthY = cy + fh * 0.24f,
            crownTopY = (cy - fh * 0.55f).coerceAtLeast(0f),
            chinBottomY = (cy + fh * 0.45f).coerceAtMost(fullHeight.toFloat()),
            headCenterX = cx,
            headWidthPx = fw,
            neckCenterX = cx,
            neckCollarY = (cy + fh * 0.48f).coerceAtMost(fullHeight.toFloat()),
            neckWidthPx = fw * 0.62f,
            autoEyeTiltDeg = 0f,
            activeModels = emptyList(),
            inferenceTimeMs = 0L
        )
    }

    /**
     * Fuses RMBG-1.4 1024x1024 full-frame alpha matte with MediaPipe 256x256 and BiSeNet 512x512
     * semantic masks, applying a studio alpha curve so 100% of background pixels become pure 0.0f.
     */
    private fun fusePassportMasks(
        width: Int,
        height: Int,
        origPixels: IntArray,
        faceCropRect: Rect,
        rmbgSuccess: Boolean,
        rmbgMatte: FloatArray,
        mpPerson256: FloatArray,
        mpClothes256: FloatArray,
        mpHeadNeck256: FloatArray,
        mpHeadHair256: FloatArray,
        mpFaceSkin256: FloatArray,
        mpBodySkin256: FloatArray,
        bisenetPerson512: FloatArray,
        bisenetHeadNeck512: FloatArray,
        bisenetCloth512: FloatArray,
        bisenetSkin512: FloatArray,
        chinBottomY: Float,
        neckCenterX: Float,
        neckWidthPx: Float,
        outPersonAlpha: FloatArray,
        outClothesMask: FloatArray,
        outHeadAndNeckMask: FloatArray,
        outSkinMask: FloatArray
    ) {
        val invW256 = 255f / (width - 1).coerceAtLeast(1)
        val invH256 = 255f / (height - 1).coerceAtLeast(1)

        val fCropL = faceCropRect.left
        val fCropT = faceCropRect.top
        val fCropR = faceCropRect.right
        val fCropB = faceCropRect.bottom
        val invFCropW512 = 511f / faceCropRect.width().coerceAtLeast(1)
        val invFCropH512 = 511f / faceCropRect.height().coerceAtLeast(1)

        for (y in 0 until height) {
            val u256Y = y * invH256
            val inFCropY = y in fCropT until fCropB
            val bY = if (inFCropY) (y - fCropT) * invFCropH512 else 0f
            val rowOff = y * width

            for (x in 0 until width) {
                val idx = rowOff + x
                val u256X = x * invW256

                val mpPerson = sampleBilinearFloat(mpPerson256, 256, 256, u256X, u256Y)
                val mpCloth = sampleBilinearFloat(mpClothes256, 256, 256, u256X, u256Y)
                val mpHeadNeck = sampleBilinearFloat(mpHeadNeck256, 256, 256, u256X, u256Y)
                val mpSkin = (
                    sampleBilinearFloat(mpFaceSkin256, 256, 256, u256X, u256Y) +
                        sampleBilinearFloat(mpBodySkin256, 256, 256, u256X, u256Y)
                    ).coerceIn(0f, 1f)

                val c = origPixels[idx]
                val r = ((c shr 16) and 0xFF).toFloat()
                val g = ((c shr 8) and 0xFF).toFloat()
                val b = (c and 0xFF).toFloat()
                val skinChromGate = computeSmoothSkinGate(r, g, b)

                val rmbgA = if (rmbgSuccess) rmbgMatte[idx] else mpPerson

                // Razor-sharp studio curve:
                // When RMBG-1.4 succeeds, use its direct high-resolution 1024x1024 matte with a sigmoid contrast curve
                // centered at 0.38 to completely eliminate background haze while preserving fine subpixel hair strands.
                val razorAlpha = if (rmbgSuccess) {
                    val t = ((rmbgA - 0.38f) / 0.18f).coerceIn(0f, 1f)
                    t * t * (3f - 2f * t)
                } else {
                    val mpGate = ((mpPerson - 0.12f) / 0.30f).coerceIn(0f, 1f)
                    val t = ((mpPerson * mpGate - 0.15f) / 0.55f).coerceIn(0f, 1f)
                    t * t * (3f - 2f * t)
                }

                var finalAlpha = razorAlpha
                val inFCrop = inFCropY && (x in fCropL until fCropR)
                if (inFCrop) {
                    val bX = (x - fCropL) * invFCropW512
                    val borderDist = min(min(bX, 511f - bX), min(bY, 511f - bY))
                    val wCrop = (borderDist / 24f).coerceIn(0f, 1f)

                    val biHeadNeck = sampleBilinearFloat(bisenetHeadNeck512, 512, 512, bX, bY)
                    val biCloth = sampleBilinearFloat(bisenetCloth512, 512, 512, bX, bY)
                    val biSkin = sampleBilinearFloat(bisenetSkin512, 512, 512, bX, bY)

                    // Reinforce solid interior facial skin & features
                    finalAlpha = max(finalAlpha, biSkin * wCrop)

                    outClothesMask[idx] = max(mpCloth, biCloth * wCrop).coerceIn(0f, 1f)
                    outHeadAndNeckMask[idx] = (biHeadNeck * wCrop + mpHeadNeck * (1f - wCrop)).coerceIn(0f, 1f)
                    outSkinMask[idx] = ((biSkin * wCrop + mpSkin * (1f - wCrop)) * skinChromGate).coerceIn(0f, 1f)
                } else {
                    outClothesMask[idx] = mpCloth.coerceIn(0f, 1f)
                    outHeadAndNeckMask[idx] = mpHeadNeck.coerceIn(0f, 1f)
                    outSkinMask[idx] = (mpSkin * skinChromGate).coerceIn(0f, 1f)
                }

                outPersonAlpha[idx] = finalAlpha

                // Below chin level, any non-skin pixel or pixel outside the natural neck column is removable clothing/arm
                if (y > chinBottomY - neckWidthPx * 0.20f) {
                    val distFromNeckX = abs(x - neckCenterX)
                    if (distFromNeckX > neckWidthPx * 0.55f) {
                        val lateralWeight = ((distFromNeckX - neckWidthPx * 0.55f) / (neckWidthPx * 0.20f))
                            .coerceIn(0f, 1f)
                        outClothesMask[idx] = max(outClothesMask[idx], lateralWeight)
                        outHeadAndNeckMask[idx] *= (1f - lateralWeight)
                    }
                    if (skinChromGate < 0.25f && y > chinBottomY) {
                        outClothesMask[idx] = max(outClothesMask[idx], 1f - skinChromGate * 4f)
                    }
                }
            }
        }
    }

    /**
     * Samples the user's true lower-face / upper-neck skin color so we can seamlessly fill any
     * occluded portion of the shirt's V-neck opening.
     */
    private fun sampleUserNeckSkinReferenceRgb(
        width: Int,
        height: Int,
        pixels: IntArray,
        skinMask: FloatArray,
        geom: PassportBiometricGeometry
    ): FloatArray {
        val xMin = (geom.headCenterX - geom.headWidthPx * 0.28f).roundToInt().coerceIn(0, width - 1)
        val xMax = (geom.headCenterX + geom.headWidthPx * 0.28f).roundToInt().coerceIn(0, width - 1)
        val yMin = (geom.mouthY + (geom.chinBottomY - geom.mouthY) * 0.25f).roundToInt().coerceIn(0, height - 1)
        val yMax = (geom.chinBottomY + geom.neckWidthPx * 0.30f).roundToInt().coerceIn(0, height - 1)

        var sumR = 0f
        var sumG = 0f
        var sumB = 0f
        var sumW = 0f

        for (y in yMin..yMax) {
            val rowOff = y * width
            for (x in xMin..xMax) {
                val idx = rowOff + x
                val wSkin = skinMask[idx]
                if (wSkin > 0.45f) {
                    val c = pixels[idx]
                    val r = ((c shr 16) and 0xFF).toFloat()
                    val g = ((c shr 8) and 0xFF).toFloat()
                    val b = (c and 0xFF).toFloat()
                    val gate = computeSmoothSkinGate(r, g, b)
                    val w = wSkin * gate
                    if (w > 0.3f) {
                        sumR += r * w
                        sumG += g * w
                        sumB += b * w
                        sumW += w
                    }
                }
            }
        }

        return if (sumW > 5f) {
            floatArrayOf(sumR / sumW, sumG / sumW, sumB / sumW)
        } else {
            floatArrayOf(212f, 172f, 148f)
        }
    }

    private fun computeSmoothSkinGate(r: Float, g: Float, b: Float): Float {
        val y = 0.299f * r + 0.587f * g + 0.114f * b
        if (y < 32f) return 0f

        val cr = 128f + 0.500f * r - 0.418688f * g - 0.081312f * b
        val cb = 128f - 0.168736f * r - 0.331264f * g + 0.500f * b

        val crLow = ((cr - 132f) / 5f).coerceIn(0f, 1f)
        val crHigh = ((178f - cr) / 8f).coerceIn(0f, 1f)
        val cbLow = ((cb - 88f) / 5f).coerceIn(0f, 1f)
        val cbHigh = ((126f - cb) / 5f).coerceIn(0f, 1f)
        val yGate = ((y - 35f) / 20f).coerceIn(0f, 1f)

        return (crLow * crHigh * cbLow * cbHigh * yGate).coerceIn(0f, 1f)
    }

    /**
     * Runs GPEN-BFR-256 Blind Face Restoration GAN + studio skin balancing + boundary hair decontamination.
     */
    private fun computeStudioPolishedForeground(
        width: Int,
        height: Int,
        origPixels: IntArray,
        faceCropRect: Rect,
        geom: PassportBiometricGeometry,
        personAlpha: FloatArray,
        skinMask: FloatArray,
        outGpenUsed: BooleanArray
    ): IntArray {
        val numPixels = width * height
        val outPixels = IntArray(numPixels)

        val gpenR = FloatArray(numPixels)
        val gpenG = FloatArray(numPixels)
        val gpenB = FloatArray(numPixels)
        val gpenMask = FloatArray(numPixels)

        val gpenOk = runGpenFaceRestorationForPassport(
            width = width,
            height = height,
            origPixels = origPixels,
            faceCropRect = faceCropRect,
            geom = geom,
            outGpenR = gpenR,
            outGpenG = gpenG,
            outGpenB = gpenB,
            outGpenMask = gpenMask
        )
        outGpenUsed[0] = gpenOk

        for (i in 0 until numPixels) {
            val cOrig = origPixels[i]
            var r = ((cOrig shr 16) and 0xFF).toFloat()
            var g = ((cOrig shr 8) and 0xFF).toFloat()
            var b = (cOrig and 0xFF).toFloat()

            if (gpenOk) {
                val mGpen = gpenMask[i].coerceIn(0f, 1f)
                if (mGpen > 0.005f) {
                    r = r * (1f - mGpen) + gpenR[i] * mGpen
                    g = g * (1f - mGpen) + gpenG[i] * mGpen
                    b = b * (1f - mGpen) + gpenB[i] * mGpen
                }
            }

            val wSkin = skinMask[i].coerceIn(0f, 1f)
            if (wSkin > 0.05f) {
                val rn = r / 255f
                val gn = g / 255f
                val bn = b / 255f
                r = ((rn + 0.14f * rn * (1f - rn) * wSkin) * 255f).coerceIn(0f, 255f)
                g = ((gn + 0.11f * gn * (1f - gn) * wSkin) * 255f).coerceIn(0f, 255f)
                b = ((bn + 0.09f * bn * (1f - bn) * wSkin) * 255f).coerceIn(0f, 255f)
            }

            // Variant C: Morphological Edge decontamination on semi-transparent boundary pixels
            // Completely replaces original background color bleed (from walls, shadows) with pure internal clothing/skin color
            val alpha = personAlpha[i]
            if (alpha in 0.03f..0.92f) {
                val y = i / width
                val x = i % width
                var bestAlpha = alpha
                var bestColor = cOrig

                // Search across multi-pixel radius (steps 2, 4, 6) in 8 directions to locate confident solid foreground
                for (step in 2..6 step 2) {
                    if (x >= step && personAlpha[i - step] > bestAlpha) {
                        bestAlpha = personAlpha[i - step]; bestColor = origPixels[i - step]
                    }
                    if (x + step < width && personAlpha[i + step] > bestAlpha) {
                        bestAlpha = personAlpha[i + step]; bestColor = origPixels[i + step]
                    }
                    if (y >= step && personAlpha[i - step * width] > bestAlpha) {
                        bestAlpha = personAlpha[i - step * width]; bestColor = origPixels[i - step * width]
                    }
                    if (y + step < height && personAlpha[i + step * width] > bestAlpha) {
                        bestAlpha = personAlpha[i + step * width]; bestColor = origPixels[i + step * width]
                    }
                }

                val ir = ((bestColor shr 16) and 0xFF).toFloat()
                val ig = ((bestColor shr 8) and 0xFF).toFloat()
                val ib = (bestColor and 0xFF).toFloat()

                // Blend factor: fully decontaminate outer fringes (alpha < 0.5) while softly tapering near solid interior
                val decontamW = ((0.92f - alpha) / 0.89f).coerceIn(0f, 1f) * 0.85f
                r = r * (1f - decontamW) + ir * decontamW
                g = g * (1f - decontamW) + ig * decontamW
                b = b * (1f - decontamW) + ib * decontamW
            }

            outPixels[i] = (0xFF shl 24) or
                (r.roundToInt().coerceIn(0, 255) shl 16) or
                (g.roundToInt().coerceIn(0, 255) shl 8) or
                b.roundToInt().coerceIn(0, 255)
        }

        return outPixels
    }

    // =========================================================================
    // MODEL 4: GPEN-BFR-256 Blind Face Restoration GAN (256x256 FFHQ Space)
    // =========================================================================
    private fun runGpenFaceRestorationForPassport(
        width: Int,
        height: Int,
        origPixels: IntArray,
        faceCropRect: Rect,
        geom: PassportBiometricGeometry,
        outGpenR: FloatArray,
        outGpenG: FloatArray,
        outGpenB: FloatArray,
        outGpenMask: FloatArray
    ): Boolean {
        val session = gpenSession ?: return false
        val env = ortEnv ?: return false

        return try {
            val uBar = 128.0f
            val vBar = 142.5f
            val uPts = floatArrayOf(96.0f, 160.0f, 128.0f, 128.0f)
            val vPts = floatArrayOf(118.0f, 118.0f, 150.0f, 184.0f)
            val xPts = floatArrayOf(geom.leftEyeX, geom.rightEyeX, geom.noseX, geom.mouthX)
            val yPts = floatArrayOf(geom.leftEyeY, geom.rightEyeY, geom.noseY, geom.mouthY)

            val xBar = (xPts[0] + xPts[1] + xPts[2] + xPts[3]) * 0.25f
            val yBar = (yPts[0] + yPts[1] + yPts[2] + yPts[3]) * 0.25f

            var numA = 0f
            var numB = 0f
            var den = 0f
            for (k in 0 until 4) {
                val du = uPts[k] - uBar
                val dv = vPts[k] - vBar
                val dx = xPts[k] - xBar
                val dy = yPts[k] - yBar
                numA += du * dx + dv * dy
                numB += du * dy - dv * dx
                den += du * du + dv * dv
            }
            val aSim = if (den > 1e-4f) numA / den else faceCropRect.width().toFloat() / 256.0f
            val bSim = if (den > 1e-4f) numB / den else 0.0f

            val hw = 256 * 256
            val alignedR = FloatArray(hw)
            val alignedG = FloatArray(hw)
            val alignedB = FloatArray(hw)

            for (v in 0 until 256) {
                val dv = v - vBar
                val rowOff = v * 256
                for (u in 0 until 256) {
                    val du = u - uBar
                    val srcX = xBar + aSim * du - bSim * dv
                    val srcY = yBar + bSim * du + aSim * dv
                    val rgb = sampleBilinearRgb(origPixels, width, height, srcX, srcY)
                    val idx = rowOff + u
                    alignedR[idx] = rgb[0]
                    alignedG[idx] = rgb[1]
                    alignedB[idx] = rgb[2]
                }
            }

            val inFloats = FloatArray(3 * hw)
            for (i in 0 until hw) {
                inFloats[i] = (alignedB[i] / 255.0f - 0.5f) / 0.5f
                inFloats[hw + i] = (alignedG[i] / 255.0f - 0.5f) / 0.5f
                inFloats[2 * hw + i] = (alignedR[i] / 255.0f - 0.5f) / 0.5f
            }

            val inputName = session.inputNames.first()
            val outFloats = FloatArray(3 * hw)
            OnnxTensor.createTensor(env, FloatBuffer.wrap(inFloats), longArrayOf(1, 3, 256, 256)).use { inputTensor ->
                session.run(mapOf(inputName to inputTensor)).use { results ->
                    val outTensor = results[0] as OnnxTensor
                    val fb = outTensor.floatBuffer
                    fb.rewind()
                    fb.get(outFloats)
                }
            }

            val finalR256 = FloatArray(hw)
            val finalG256 = FloatArray(hw)
            val finalB256 = FloatArray(hw)
            val mask256 = FloatArray(hw)

            for (v in 0 until 256) {
                val rowOff = v * 256
                for (u in 0 until 256) {
                    val idx = rowOff + u
                    val bVal = ((outFloats[idx] * 0.5f + 0.5f) * 255.0f).coerceIn(0f, 255f)
                    val gVal = ((outFloats[hw + idx] * 0.5f + 0.5f) * 255.0f).coerceIn(0f, 255f)
                    val rVal = ((outFloats[2 * hw + idx] * 0.5f + 0.5f) * 255.0f).coerceIn(0f, 255f)

                    finalR256[idx] = rVal * 0.72f + alignedR[idx] * 0.28f
                    finalG256[idx] = gVal * 0.72f + alignedG[idx] * 0.28f
                    finalB256[idx] = bVal * 0.72f + alignedB[idx] * 0.28f

                    val distEll = sqrt(((u - 128f) / 78f).let { it * it } + ((v - 142f) / 96f).let { it * it })
                    mask256[idx] = ((1.06f - distEll) / 0.36f).coerceIn(0f, 1f)
                }
            }

            val detSim = (aSim * aSim + bSim * bSim).coerceAtLeast(1e-5f)
            val invDet = 1.0f / detSim
            val scaleApprox = sqrt(detSim)
            val maxRadiusPx = (140f * scaleApprox).roundToInt()
            val yMin = (yBar.roundToInt() - maxRadiusPx).coerceAtLeast(0)
            val yMax = (yBar.roundToInt() + maxRadiusPx).coerceAtMost(height - 1)
            val xMin = (xBar.roundToInt() - maxRadiusPx).coerceAtLeast(0)
            val xMax = (xBar.roundToInt() + maxRadiusPx).coerceAtMost(width - 1)

            for (y in yMin..yMax) {
                val dy = y - yBar
                val rowOff = y * width
                for (x in xMin..xMax) {
                    val dx = x - xBar
                    val u = uBar + (aSim * dx + bSim * dy) * invDet
                    val v = vBar + (aSim * dy - bSim * dx) * invDet
                    if (u >= 1f && u < 254f && v >= 1f && v < 254f) {
                        val m = sampleBilinearFloat(mask256, 256, 256, u, v)
                        if (m > 0.002f) {
                            val idx = rowOff + x
                            outGpenR[idx] = sampleBilinearFloat(finalR256, 256, 256, u, v)
                            outGpenG[idx] = sampleBilinearFloat(finalG256, 256, 256, u, v)
                            outGpenB[idx] = sampleBilinearFloat(finalB256, 256, 256, u, v)
                            outGpenMask[idx] = m
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "GPEN passport restoration error", e)
            false
        }
    }

    private fun sampleBilinearFloat(
        grid: FloatArray,
        w: Int,
        h: Int,
        x: Float,
        y: Float
    ): Float {
        val cx = x.coerceIn(0f, (w - 1).toFloat())
        val cy = y.coerceIn(0f, (h - 1).toFloat())
        val x0 = cx.toInt().coerceIn(0, w - 1)
        val y0 = cy.toInt().coerceIn(0, h - 1)
        val x1 = (x0 + 1).coerceAtMost(w - 1)
        val y1 = (y0 + 1).coerceAtMost(h - 1)
        val fx = cx - x0
        val fy = cy - y0

        val row0 = y0 * w
        val row1 = y1 * w
        val v00 = grid[row0 + x0]
        val v10 = grid[row0 + x1]
        val v01 = grid[row1 + x0]
        val v11 = grid[row1 + x1]

        val top = v00 + (v10 - v00) * fx
        val bot = v01 + (v11 - v01) * fx
        return top + (bot - top) * fy
    }

    private fun sampleBilinearRgb(
        pixels: IntArray,
        w: Int,
        h: Int,
        x: Float,
        y: Float
    ): FloatArray {
        val cx = x.coerceIn(0f, (w - 1).toFloat())
        val cy = y.coerceIn(0f, (h - 1).toFloat())
        val x0 = cx.toInt().coerceIn(0, w - 1)
        val y0 = cy.toInt().coerceIn(0, h - 1)
        val x1 = (x0 + 1).coerceAtMost(w - 1)
        val y1 = (y0 + 1).coerceAtMost(h - 1)
        val fx = cx - x0
        val fy = cy - y0

        val c00 = pixels[y0 * w + x0]
        val c10 = pixels[y0 * w + x1]
        val c01 = pixels[y1 * w + x0]
        val c11 = pixels[y1 * w + x1]

        val w00 = (1f - fx) * (1f - fy)
        val w10 = fx * (1f - fy)
        val w01 = (1f - fx) * fy
        val w11 = fx * fy

        val r = ((c00 shr 16) and 0xFF) * w00 +
            ((c10 shr 16) and 0xFF) * w10 +
            ((c01 shr 16) and 0xFF) * w01 +
            ((c11 shr 16) and 0xFF) * w11
        val g = ((c00 shr 8) and 0xFF) * w00 +
            ((c10 shr 8) and 0xFF) * w10 +
            ((c01 shr 8) and 0xFF) * w01 +
            ((c11 shr 8) and 0xFF) * w11
        val b = (c00 and 0xFF) * w00 +
            (c10 and 0xFF) * w10 +
            (c01 and 0xFF) * w01 +
            (c11 and 0xFF) * w11

        return floatArrayOf(r, g, b)
    }

    fun close() {
        try {
            selfieInterpreter?.close()
            bisenetInterpreter?.close()
            rmbgSession?.close()
            gpenSession?.close()
        } catch (_: Exception) {
        }
        selfieInterpreter = null
        bisenetInterpreter = null
        rmbgSession = null
        gpenSession = null
    }
}
