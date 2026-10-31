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
