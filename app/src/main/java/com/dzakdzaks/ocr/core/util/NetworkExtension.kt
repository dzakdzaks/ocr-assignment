package com.dzakdzaks.ocr.core.util

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Exception.toError(): OCRResult.Error {
    return try {
        when {
            this is IOException && message == "No Internet" || this is UnknownHostException -> {
                error("No Internet.")
            }
            this is SocketTimeoutException -> {
                error("Time Out.")
            }
            else -> {
                error(message ?: toString())
            }
        }
    } catch (e: Exception) {
        error(e.message ?: e.toString())
    }
}


private fun error(errorMessage: String): OCRResult.Error =
    OCRResult.Error("API call failed => $errorMessage")
