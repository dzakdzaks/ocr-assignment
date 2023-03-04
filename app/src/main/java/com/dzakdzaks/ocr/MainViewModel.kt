package com.dzakdzaks.ocr

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dzakdzaks.ocr.core.util.OCRResult
import com.dzakdzaks.ocr.data.maps.api.model.Path
import com.dzakdzaks.ocr.domain.maps.usecase.FindDrivingPathUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val findDrivingPathUseCase: FindDrivingPathUseCase,
) : ViewModel() {

    var photoFile: File? = null

    var resultText: String = ""

    var currentLocation: Location? = null

    var distance: Int = 0

    var duration: Int = 0

    private val _path =
        MutableStateFlow<OCRResult<Path>>(OCRResult.Empty)
    val path: StateFlow<OCRResult<Path>> = _path

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var pathJob: Job? = null

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
        private const val PLAZA_INDONESIA_LAT_LNG = "-6.193859700000001,106.8219662"
    }

}
