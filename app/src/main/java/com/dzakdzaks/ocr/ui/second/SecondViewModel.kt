package com.dzakdzaks.ocr.ui.second

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SecondViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
): ViewModel() {

    val resultText = MutableLiveData(savedStateHandle.get<String>(SecondActivity.EXTRA_RESULT_TEXT) ?: "")
    val duration = MutableLiveData(savedStateHandle.get<Long>(SecondActivity.EXTRA_DURATION) ?: 0L)
    val distance = MutableLiveData(savedStateHandle.get<Long>(SecondActivity.EXTRA_DISTANCE)?: 0L)

}
