package com.juacie.littlewar.ui.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * MVI 畫面共用的 ViewModel 骨架：State/Effect 的宣告與 setEvent 進入點是每個畫面都一樣的樣板，
 * 統一放這裡讓子類別只需要處理自己的 Event 分派邏輯與狀態轉換。
 */
abstract class MviViewModel<State, Event, Effect>(initialState: State) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    protected val currentState: State
        get() = _state.value

    protected fun setState(reducer: State.() -> State) {
        _state.value = _state.value.reducer()
    }

    private val effectChannel = Channel<Effect>()
    val effect = effectChannel.receiveAsFlow()

    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch { effectChannel.send(effect) }
    }

    abstract fun setEvent(event: Event)
}
