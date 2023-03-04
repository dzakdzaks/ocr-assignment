package com.dzakdzaks.ocr.data.maps.impl.remote.api

import com.dzakdzaks.ocr.data.maps.impl.remote.response.PathResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MapsApi {
    @GET("FindDrivingPath")
    suspend fun findDrivingPath(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
    ): PathResponse
}
