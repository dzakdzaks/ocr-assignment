package com.dzakdzaks.ocr.data.maps.api.repository

import com.dzakdzaks.ocr.core.util.OCRResult
import com.dzakdzaks.ocr.data.maps.api.model.Path
import com.dzakdzaks.ocr.data.maps.api.model.RequestFirebaseResult
import kotlinx.coroutines.flow.Flow

interface MapsRepository {
    fun fetchDrivingPath(origin: String, destination: String): Flow<OCRResult<Path>>

    fun uploadDrivingPath(request: RequestFirebaseResult): Flow<OCRResult<String>>
}
