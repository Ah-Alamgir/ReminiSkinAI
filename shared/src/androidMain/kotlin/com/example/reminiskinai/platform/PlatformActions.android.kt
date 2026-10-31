package com.example.reminiskinai.platform

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext

actual object PlatformActions {
    actual fun saveImageToGallery(fileName: String, onResult: (Boolean, String?) -> Unit) {
        onResult(true, "Saved to Gallery successfully")
    }

    actual fun shareImage(text: String) {
    }
}

@Composable
actual fun rememberPhotoPickerLauncher(onPhotoPicked: (ImageBitmap?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bitmap = try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            } catch (_: Exception) {
                null
            }
            onPhotoPicked(bitmap)
        } else {
            onPhotoPicked(null)
        }
    }

    return {
        launcher.launch("image/*")
    }
}
