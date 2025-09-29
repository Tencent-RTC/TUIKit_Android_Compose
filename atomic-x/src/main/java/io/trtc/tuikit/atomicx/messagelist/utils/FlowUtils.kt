package io.trtc.tuikit.atomicx.messagelist.utils

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

fun <T> ViewModel.collectAsState(stateFlow: StateFlow<T>, initial: T? = null): State<T> =
    stateFlow.collectAsState(viewModelScope, initial)

fun <T> StateFlow<T>.collectAsState(scope: CoroutineScope, initial: T? = null): State<T> {
    return mutableStateOf(initial ?: value).apply {
        scope.launch {
            collect {
                value = it
            }
        }
    }
}
