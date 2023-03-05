package com.dzakdzaks.ocr.ui.second

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class SecondViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val _resultText =
        MutableStateFlow(savedStateHandle.get<String>(SecondActivity.EXTRA_RESULT_TEXT) ?: "")
    val resultText = _resultText.asStateFlow()

    private val _duration =
        MutableStateFlow(savedStateHandle.get<Long>(SecondActivity.EXTRA_DURATION) ?: 0L)
    val duration = _duration.asStateFlow()

    private val _distance =
        MutableStateFlow(savedStateHandle.get<Long>(SecondActivity.EXTRA_DISTANCE) ?: 0L)
    val distance = _distance.asStateFlow()

}
