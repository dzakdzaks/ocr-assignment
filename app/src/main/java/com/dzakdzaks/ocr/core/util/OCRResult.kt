package com.dzakdzaks.ocr.core.util

sealed class OCRResult<out T> private constructor() {
    object Loading : OCRResult<Nothing>()
    class Success<T>(val data: T) : OCRResult<T>()
    open class Error(open val message: String) : OCRResult<Nothing>()
    object Empty : OCRResult<Nothing>()
}
