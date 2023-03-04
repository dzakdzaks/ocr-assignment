package com.dzakdzaks.ocr.data.maps.api.repository

import com.dzakdzaks.ocr.core.util.OCRResult
import com.dzakdzaks.ocr.data.maps.api.model.Path
import kotlinx.coroutines.flow.Flow

interface MapsRepository {
    fun fetchDrivingPath(origin: String, destination: String): Flow<OCRResult<Path>>
}
