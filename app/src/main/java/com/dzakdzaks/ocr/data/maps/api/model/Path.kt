package com.dzakdzaks.ocr.data.maps.api.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Path(
    val route: Route,
)
