package com.dzakdzaks.ocr.data.maps.api.model

data class RequestFirebaseResult(
    val origin: String,
    val destination: String,
    val resultText: String,
    val duration: Long,
    val distance: Long,
)
