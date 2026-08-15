package com.juacie.littlewar.ui.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juacie.littlewar.domain.usecase.ObserveBattleResultUseCase
import com.juacie.littlewar.domain.usecase.ResetBattleUseCase
import com.juacie.littlewar.ui.result.ResultContract.Effect
import com.juacie.littlewar.ui.result.ResultContract.Event
import com.juacie.littlewar.ui.result.ResultContract.State
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    observeBattleResult: ObserveBattleResultUseCase,
    private val resetBattle: ResetBattleUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(State(result = observeBattleResult().value))
    val state: StateFlow<State> = _state.asStateFlow()

    private val effectChannel = Channel<Effect>()
    val effect = effectChannel.receiveAsFlow()

    fun setEvent(event: Event) {
        when (event) {
            Event.PlayAgain -> finish(Effect.NavigateToFormation)
            Event.GoHome -> finish(Effect.NavigateToHome)
        }
    }

    private fun finish(effect: Effect) {
        resetBattle()
        viewModelScope.launch { effectChannel.send(effect) }
    }
}
