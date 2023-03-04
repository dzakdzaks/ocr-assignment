package com.dzakdzaks.ocr.domain.maps.usecase

import com.dzakdzaks.ocr.data.maps.api.model.RequestFirebaseResult
import com.dzakdzaks.ocr.data.maps.api.repository.MapsRepository
import javax.inject.Inject

class UploadDrivingPathUseCase @Inject constructor(private val mapsRepository: MapsRepository) {
    operator fun invoke(
        request: RequestFirebaseResult,
    ) =
        mapsRepository.uploadDrivingPath(
            request = request,
        )
}
