package com.example.reminiskinai.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

expect object PlatformActions {
    fun saveImageToGallery(fileName: String, onResult: (Boolean, String?) -> Unit)
    fun saveImageToGallery(image: ImageBitmap?, fileName: String, onResult: (Boolean, String?) -> Unit)
    fun shareImage(text: String)
}

@Composable
expect fun rememberPhotoPickerLauncher(onPhotoPicked: (ImageBitmap?) -> Unit): () -> Unit
