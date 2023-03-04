package com.dzakdzaks.ocr.core.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import androidx.activity.ComponentActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dzakdzaks.ocr.R
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun <T> ComponentActivity.collectLatestLifecycleFlow(flow: Flow<T>, collect: suspend (T) -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.CREATED) {
            flow.collectLatest(collect)
        }
    }
}

fun View.showSnackBarIndefinite(
    msg: String,
    positiveAction: () -> Unit = {},
) {
    Snackbar.make(this, msg, Snackbar.LENGTH_INDEFINITE).setAction("Yes") {
        positiveAction()
    }.show()
}

fun View.showSnackBar(
    msg: String,
) {
    Snackbar.make(this, msg, Snackbar.LENGTH_LONG).show()
}

fun View.visibleGone() {
    visibility = if (!isVisible) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

fun View.visibleGone(visible: Boolean) {
    visibility = if (visible) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

fun FragmentActivity.goToApplicationSettings() {
    startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
    )
}

fun Context.outputImageDirectory(): File {
    val mediaDir = this.externalMediaDirs.firstOrNull()?.let {
        File(it, resources.getString(R.string.app_name)).apply {
            mkdirs()
        }
    }
    val resultCacheDir = if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir

    return File(
        resultCacheDir, SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault()
        ).format(System.currentTimeMillis()) + ".jpg"
    )
}

suspend fun Context.rotateImageCorrectly(photoFile: File) = withContext(Dispatchers.IO) {
    val sourceBitmap = MediaStore.Images.Media.getBitmap(contentResolver, photoFile.toUri())

    val exif = ExifInterface(photoFile.inputStream())
    val rotation =
        exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    val rotationInDegrees = when (rotation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        ExifInterface.ORIENTATION_TRANSVERSE -> -90
        ExifInterface.ORIENTATION_TRANSPOSE -> -270
        else -> 0
    }
    val matrix = Matrix().apply {
        if (rotation != 0) preRotate(rotationInDegrees.toFloat())
    }

    val rotatedBitmap = Bitmap.createBitmap(
        sourceBitmap, 0, 0, sourceBitmap.width, sourceBitmap.height, matrix, true
    )

    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(photoFile))

    sourceBitmap.recycle()
    rotatedBitmap.recycle()
}

fun String.isEmptyOrBlank(): Boolean = isEmpty() && isBlank()

/** Int is second*/
fun Long.toReadableHour(): String {
    val hours = this / 3_600L
    val minutes = this % 3_600L / 60L
    val seconds = this % 60L
    return String.format("%02d Hour %02d Minutes %02d Seconds ", hours, minutes, seconds)
}

/** Int is meter*/
fun Long.toKiloMeter(): String {
    if (this < 1_000L) return "$this Meter"
    val km = this.toDouble() / 1_000L
    val number3digits: Double = String.format("%.3f", km).toDouble()
    val number2digits: Double = String.format("%.2f", number3digits).toDouble()
    val solution: Double = String.format("%.1f", number2digits).toDouble()
    return "$solution Kilometer"
}
