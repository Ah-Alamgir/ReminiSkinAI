package com.example.reminiskinai.platform

import androidx.compose.runtime.Composable

actual object PlatformActions {
    actual fun saveImageToGallery(fileName: String, onResult: (Boolean, String?) -> Unit) {
        onResult(true, "Saved to Gallery successfully")
    }

    actual fun shareImage(text: String) {
    }
}

@Composable
actual fun rememberPhotoPickerLauncher(onPhotoPicked: () -> Unit): () -> Unit {
    return {
        onPhotoPicked()
    }
}
