package com.viz.prodzen.ui.screens.intervention

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class InterventionViewModel @Inject constructor() : ViewModel() {

    private val _intentionText = MutableStateFlow("")
    val intentionText = _intentionText.asStateFlow()

    fun onIntentionTextChanged(text: String) {
        _intentionText.value = text
    }
}