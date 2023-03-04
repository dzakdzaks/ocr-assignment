package com.dzakdzaks.ocr.data.maps.impl.remote.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RouteResponse(
    @field:Json(name = "distance") val distance: Int? = null,
    @field:Json(name = "duration") val duration: Int? = null,
)
