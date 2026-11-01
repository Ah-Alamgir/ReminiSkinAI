package com.example.reminiskinai.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject
import platform.posix.memcpy

import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.EncodedImageFormat
import platform.Foundation.dataWithBytes
import platform.UIKit.UIImageWriteToSavedPhotosAlbum

actual object PlatformActions {
    actual fun saveImageToGallery(fileName: String, onResult: (Boolean, String?) -> Unit) {
        saveImageToGallery(null, fileName, onResult)
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun saveImageToGallery(
        image: ImageBitmap?,
        fileName: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        try {
            val uiImage: UIImage? = if (image != null) {
                val skiaBitmap = image.asSkiaBitmap()
                val skiaImage = Image.makeFromBitmap(skiaBitmap)
                val data = skiaImage.encodeToData(EncodedImageFormat.JPEG, 98)
                val bytes = data?.bytes
                if (bytes != null && bytes.isNotEmpty()) {
                    val nsData: NSData = bytes.usePinned { pinned ->
                        NSData.dataWithBytes(pinned.addressOf(0), bytes.size.toULong())
                    }
                    UIImage.imageWithData(nsData)
                } else null
            } else null

            if (uiImage != null) {
                UIImageWriteToSavedPhotosAlbum(uiImage, null, null, null)
                val actualName = if (fileName.contains(".")) fileName else "$fileName.jpg"
                onResult(true, actualName)
            } else {
                onResult(false, "Could not convert image to save")
            }
        } catch (e: Exception) {
            onResult(false, e.message ?: "Failed to save photo to iOS Photos")
        }
    }

    actual fun shareImage(text: String) {
        val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController
        val activityVC = UIActivityViewController(listOf(text), null)
        rootVC?.presentViewController(activityVC, animated = true, completion = null)
    }
}

private class PhotoPickerDelegate(
    private val onPicked: (ImageBitmap?) -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    @OptIn(ExperimentalForeignApi::class)
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        val uiImage = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        val nsData = uiImage?.let { UIImageJPEGRepresentation(it, 0.9) }

        var resultBitmap: ImageBitmap? = null
        if (nsData != null && nsData.length.toInt() > 0) {
            val length = nsData.length.toInt()
            val bytes = ByteArray(length)
            memcpy(bytes.refTo(0), nsData.bytes, nsData.length)
            try {
                resultBitmap = Image.makeFromEncoded(bytes).toComposeImageBitmap()
            } catch (_: Exception) {}
        }

        picker.dismissViewControllerAnimated(true) {
            onPicked(resultBitmap)
        }
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true) {
            onPicked(null)
        }
    }
}

private var activePickerDelegate: PhotoPickerDelegate? = null

@Composable
actual fun rememberPhotoPickerLauncher(onPhotoPicked: (ImageBitmap?) -> Unit): () -> Unit {
    return remember {
        {
            val delegate = PhotoPickerDelegate(onPhotoPicked)
            activePickerDelegate = delegate
            val picker = UIImagePickerController().apply {
                sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
                this.delegate = delegate
            }
            val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController
            rootVC?.presentViewController(picker, animated = true, completion = null)
        }
    }
}
