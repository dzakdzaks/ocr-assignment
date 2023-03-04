package com.dzakdzaks.ocr.data.maps.impl.repository

import com.dzakdzaks.ocr.core.util.OCRResult
import com.dzakdzaks.ocr.core.util.isEmptyOrBlank
import com.dzakdzaks.ocr.core.util.toError
import com.dzakdzaks.ocr.core.util.toKiloMeter
import com.dzakdzaks.ocr.core.util.toReadableHour
import com.dzakdzaks.ocr.data.maps.api.model.Path
import com.dzakdzaks.ocr.data.maps.api.model.RequestFirebaseResult
import com.dzakdzaks.ocr.data.maps.api.repository.MapsRepository
import com.dzakdzaks.ocr.data.maps.impl.mapper.toPath
import com.dzakdzaks.ocr.data.maps.impl.remote.api.MapsApi
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.tasks.await

class MapsRepositoryImpl(
    private val mapsApi: MapsApi,
    private val fireStore: FirebaseFirestore,
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
    }.onStart { emit(OCRResult.Loading) }.flowOn(ioDispatcher)

    override fun uploadDrivingPath(
        request: RequestFirebaseResult,
    ): Flow<OCRResult<String>> = flow {
        if (request.origin.isEmptyOrBlank()) emit(OCRResult.Error("Origin can't be empty"))
        if (request.destination.isEmptyOrBlank()) emit(OCRResult.Error("Origin can't be empty"))
        if (request.resultText.isEmptyOrBlank()) emit(OCRResult.Error("Result text can't be empty"))
        if (request.duration == 0L) emit(OCRResult.Error("Duration can't be 0"))
        if (request.distance == 0L) emit(OCRResult.Error("Distance can't be 0"))
        try {
            val data = hashMapOf(
                "origin" to request.origin,
                "destination" to request.destination,
                "resultText" to request.resultText,
                "duration" to request.duration.toReadableHour(),
                "durationRaw" to request.duration,
                "distance" to request.distance.toKiloMeter(),
                "distanceRaw" to request.distance,
                "date" to SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
                ).format(System.currentTimeMillis()),
            )
            fireStore.collection("results").add(data).await()
            emit(OCRResult.Success("Success"))
        } catch (e: Exception) {
            emit(OCRResult.Error(e.localizedMessage ?: "Upload Data Error"))
        }
    }.onStart { emit(OCRResult.Loading) }.flowOn(ioDispatcher)
}
