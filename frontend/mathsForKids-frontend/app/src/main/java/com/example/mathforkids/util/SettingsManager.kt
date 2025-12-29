package com.example.mathforkids.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object SettingsManager {
    // Sound Settings
    var isSoundEnabled by mutableStateOf(true)
    var volume by mutableStateOf(1.0f) // 0.0f to 1.0f

    // Font Settings
    var fontScale by mutableStateOf(1.0f) // 1.0f = Normal, 0.8f = Small, 1.2f = Large
}
