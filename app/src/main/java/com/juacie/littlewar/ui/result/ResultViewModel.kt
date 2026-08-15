package com.juacie.littlewar.ui.result

import com.juacie.littlewar.domain.usecase.ObserveBattleResultUseCase
import com.juacie.littlewar.domain.usecase.ResetBattleUseCase
import com.juacie.littlewar.ui.mvi.MviViewModel
import com.juacie.littlewar.ui.result.ResultContract.Effect
import com.juacie.littlewar.ui.result.ResultContract.Event
import com.juacie.littlewar.ui.result.ResultContract.State
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    observeBattleResult: ObserveBattleResultUseCase,
    private val resetBattle: ResetBattleUseCase
) : MviViewModel<State, Event, Effect>(State(result = observeBattleResult().value)) {

    override fun setEvent(event: Event) {
        when (event) {
            Event.PlayAgain -> finish(Effect.NavigateToFormation)
            Event.GoHome -> finish(Effect.NavigateToHome)
        }
    }

    private fun finish(effect: Effect) {
        resetBattle()
        sendEffect(effect)
    }
}
