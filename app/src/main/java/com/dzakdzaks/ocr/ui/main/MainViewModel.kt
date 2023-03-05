package com.dzakdzaks.ocr.ui.main

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dzakdzaks.ocr.core.util.OCRResult
import com.dzakdzaks.ocr.data.maps.api.model.Path
import com.dzakdzaks.ocr.data.maps.api.model.RequestFirebaseResult
import com.dzakdzaks.ocr.domain.maps.usecase.FindDrivingPathUseCase
import com.dzakdzaks.ocr.domain.maps.usecase.UploadDrivingPathUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val findDrivingPathUseCase: FindDrivingPathUseCase,
    private val uploadDrivingPathUseCase: UploadDrivingPathUseCase,
) : ViewModel() {

    var photoFile: File? = null

    var resultText: String = ""

    var currentLocation: Location? = null

    var distance: Long = 0L

    var duration: Long = 0L

    private val _path =
        MutableStateFlow<OCRResult<Path>>(OCRResult.Empty)
    val path = _path.asStateFlow()

    private val _uploadPath =
        MutableStateFlow<OCRResult<String>>(OCRResult.Empty)
    val uploadPath = _uploadPath.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var pathJob: Job? = null
    private var uploadPathJob: Job? = null

    fun loadFindDrivingPath() {
        currentLocation?.let {
            pathJob?.cancel()

            pathJob = viewModelScope.launch {
                findDrivingPathUseCase(
                    origin = "${it.latitude},${it.longitude}",
                    destination = PLAZA_INDONESIA_LAT_LNG
                ).collect {
                    _path.value = it
                }
            }
        }
    }

    fun uploadDrivingPath() {
        currentLocation?.let {
            uploadPathJob?.cancel()
            uploadPathJob = viewModelScope.launch {
                uploadDrivingPathUseCase(
                    request = RequestFirebaseResult(
                        origin = "${it.latitude},${it.longitude}",
                        destination = PLAZA_INDONESIA_LAT_LNG,
                        resultText = resultText,
                        duration = duration,
                        distance = distance,
                    )

                ).collect {
                    _uploadPath.value = it
                }
            }
        }
    }

    fun setIsLoading(newValue: Boolean) {
        _isLoading.value = newValue
    }

    fun deletePhotoFile(): Boolean {
        val result = photoFile?.delete()
        if (result == true) {
            photoFile = null
        }
        return result == true
    }

    companion object {
        private const val PLAZA_INDONESIA_LAT_LNG = "-6.1930672,106.8217313"
    }

}
