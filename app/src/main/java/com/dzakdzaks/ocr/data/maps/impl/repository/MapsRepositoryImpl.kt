package com.dzakdzaks.ocr.data.maps.impl.repository

import com.dzakdzaks.ocr.core.util.OCRResult
import com.dzakdzaks.ocr.core.util.toError
import com.dzakdzaks.ocr.data.maps.api.model.Path
import com.dzakdzaks.ocr.data.maps.api.repository.MapsRepository
import com.dzakdzaks.ocr.data.maps.impl.mapper.toPath
import com.dzakdzaks.ocr.data.maps.impl.remote.api.MapsApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart

class MapsRepositoryImpl(
    private val mapsApi: MapsApi,
    private val ioDispatcher: CoroutineDispatcher,
) : MapsRepository {

    override fun fetchDrivingPath(
        origin: String,
        destination: String,
    ): Flow<OCRResult<Path>> = flow {
        try {
            val data = mapsApi.findDrivingPath(origin, destination)
            emit(OCRResult.Success(data.toPath()))
        } catch (e: Exception) {
            emit(e.toError())
        }
    }
        .onStart {
            emit(OCRResult.Loading)
        }
        .flowOn(ioDispatcher)
}
