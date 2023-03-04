package com.dzakdzaks.ocr.data.maps.impl.mapper

import com.dzakdzaks.ocr.data.maps.api.model.Path
import com.dzakdzaks.ocr.data.maps.api.model.Route
import com.dzakdzaks.ocr.data.maps.impl.remote.response.PathResponse
import com.dzakdzaks.ocr.data.maps.impl.remote.response.RouteResponse

fun PathResponse.toPath(): Path = Path(
    route = route?.toRoute() ?: Route(distance = 0, duration = 0)
)

fun RouteResponse.toRoute(): Route = Route(
    distance = distance ?: 0,
    duration = duration ?: 0,
)
