package com.juacie.littlewar.ui.result

import com.juacie.littlewar.battleengine.BattleResult

object ResultContract {
    data class State(val result: BattleResult? = null)

    sealed interface Event {
        data object PlayAgain : Event
        data object GoHome : Event
    }

    sealed interface Effect {
        data object NavigateToFormation : Effect
        data object NavigateToHome : Effect
    }
}
