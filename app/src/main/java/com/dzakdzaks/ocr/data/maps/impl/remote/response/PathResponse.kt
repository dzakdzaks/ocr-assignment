package com.dzakdzaks.ocr.data.maps.impl.remote.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PathResponse(
    @field:Json(name = "route") val route: RouteResponse? = null,
)
