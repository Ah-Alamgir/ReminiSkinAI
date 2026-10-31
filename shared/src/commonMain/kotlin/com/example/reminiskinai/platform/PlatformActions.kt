package com.example.reminiskinai.platform

import androidx.compose.runtime.Composable

expect object PlatformActions {
    fun saveImageToGallery(fileName: String, onResult: (Boolean, String?) -> Unit)
    fun shareImage(text: String)
}

@Composable
expect fun rememberPhotoPickerLauncher(onPhotoPicked: () -> Unit): () -> Unit
