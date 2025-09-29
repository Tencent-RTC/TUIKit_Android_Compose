package io.trtc.tuikit.chat

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

sealed class Event {
    data class ContactDeleted(val contactID: String) : Event()
    data class GroupDeleted(val groupID: String) : Event()
}

object EventBus {
    private var _events = MutableSharedFlow<Event>(extraBufferCapacity = 64)
    val events = _events.asSharedFlow()

    fun post(event: Event) {
        _events.tryEmit(event)
    }

}

inline fun <reified T> EventBus.collectOn(
    lifecycleOwner: LifecycleOwner,
    crossinline action: (T) -> Unit
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
            events.filterIsInstance<T>().collect { action(it) }
        }
    }
}
