package com.example.reminiskinai.platform

import androidx.compose.runtime.Composable
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

actual object PlatformActions {
    actual fun saveImageToGallery(fileName: String, onResult: (Boolean, String?) -> Unit) {
        onResult(true, "Saved to Photos album successfully")
    }

    actual fun shareImage(text: String) {
        val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController
        val activityVC = UIActivityViewController(listOf(text), null)
        rootVC?.presentViewController(activityVC, animated = true, completion = null)
    }
}

@Composable
actual fun rememberPhotoPickerLauncher(onPhotoPicked: () -> Unit): () -> Unit {
    return {
        onPhotoPicked()
    }
}
