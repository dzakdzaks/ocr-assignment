package com.dzakdzaks.ocr.domain.maps.usecase

import com.dzakdzaks.ocr.data.maps.api.repository.MapsRepository
import javax.inject.Inject

class FindDrivingPathUseCase @Inject constructor(private val mapsRepository: MapsRepository) {
    operator fun invoke(origin: String, destination: String) =
        mapsRepository.fetchDrivingPath(origin = origin, destination = destination)
}
