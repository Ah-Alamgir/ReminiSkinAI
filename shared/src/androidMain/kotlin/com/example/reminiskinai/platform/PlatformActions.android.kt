package com.example.reminiskinai.platform

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import kotlin.math.max

actual object PlatformActions {
    var appContext: Context? = null

    actual fun saveImageToGallery(fileName: String, onResult: (Boolean, String?) -> Unit) {
        saveImageToGallery(null, fileName, onResult)
    }

    actual fun saveImageToGallery(
        image: ImageBitmap?,
        fileName: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val ctx = appContext
        if (ctx == null) {
            onResult(false, "Context not available")
            return
        }

        try {
            val bitmap = image?.asAndroidBitmap() ?: run {
                try {
                    ctx.assets.open("composeResources/reminiskinai.shared.generated.resources/drawable/sample_1_remini_ref.webp").use {
                        BitmapFactory.decodeStream(it)
                    }
                } catch (_: Throwable) {
                    Bitmap.createBitmap(1080, 1440, Bitmap.Config.ARGB_8888)
                }
            }

            val actualFileName = if (fileName.contains(".")) fileName else "${fileName}_${System.currentTimeMillis()}.jpg"
            val resolver = ctx.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, actualFileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AuraAI")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri == null) {
                onResult(false, "Could not open MediaStore")
                return
            }

            resolver.openOutputStream(uri)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 98, stream)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }

            // Ensure media scanner indexes the new photo so it shows immediately in Google Photos / Gallery
            MediaScannerConnection.scanFile(ctx, arrayOf(uri.toString()), arrayOf("image/jpeg"), null)

            onResult(true, actualFileName)
        } catch (e: Exception) {
            onResult(false, e.message ?: "Failed to save image")
        }
    }

    actual fun shareImage(text: String) {
        val ctx = appContext ?: return
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Share via Aura AI").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(chooser)
        } catch (_: Exception) {}
    }
}

@Composable
actual fun rememberPhotoPickerLauncher(onPhotoPicked: (ImageBitmap?) -> Unit): () -> Unit {
    val context = LocalContext.current

    DisposableEffect(context) {
        PlatformActions.appContext = context.applicationContext
        onDispose {}
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bitmap = try {
                var sampleSize = 1
                context.contentResolver.openInputStream(uri)?.use { boundsStream ->
                    val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeStream(boundsStream, null, boundsOpts)
                    val maxDim = max(boundsOpts.outWidth, boundsOpts.outHeight)
                    while (maxDim / (sampleSize * 2) >= 1200) {
                        sampleSize *= 2
                    }
                }

                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val opts = BitmapFactory.Options().apply {
                        inSampleSize = sampleSize
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }
                    BitmapFactory.decodeStream(stream, null, opts)?.asImageBitmap()
                }
            } catch (_: Throwable) {
                null
            }
            onPhotoPicked(bitmap)
        } else {
            onPhotoPicked(null)
        }
    }

    return {
        launcher.launch(
            androidx.activity.result.PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
    }
}
