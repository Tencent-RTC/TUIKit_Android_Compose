package io.trtc.tuikit.atomicx.messagelist.viewmodels

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


data class HighlightConfig(
    val flashCount: Int = 3,
    val flashDuration: Long = 250L,
)

data class HighlightState(
    val highLightKey: String,
    val config: HighlightConfig,
    val startTime: Long = System.currentTimeMillis(),
)

object HighlightManager {
    private val _highlights = MutableStateFlow<Map<String, HighlightState>>(emptyMap())
    val highlights: StateFlow<Map<String, HighlightState>> = _highlights

    fun addHighlight(key: String, config: HighlightConfig = HighlightConfig()) {
        val currentHighlights = _highlights.value.toMutableMap()
        currentHighlights[key] = HighlightState(
            highLightKey = key,
            config = config,
            startTime = System.currentTimeMillis()
        )
        _highlights.value = currentHighlights
        val coroutine = CoroutineScope(Dispatchers.Main)
        coroutine.launch {
            delay(config.flashDuration * config.flashCount * 2 + 100)
            removeHighlight(key)
        }
    }

    fun removeHighlight(key: String) {
        val currentHighlights = _highlights.value.toMutableMap()
        currentHighlights.remove(key)
        _highlights.value = currentHighlights
    }

    fun clearAllHighlights() {
        _highlights.value = emptyMap()
    }

    fun getHighlight(key: String): HighlightState? {
        return _highlights.value[key]
    }

    fun isHighlighted(key: String): Boolean {
        return _highlights.value.containsKey(key)
    }
}