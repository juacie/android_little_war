package com.juacie.littlewar.ui.battle

import com.juacie.littlewar.battleengine.UnitSnapshot

object BattleContract {
    data class State(
        val roster: List<UnitSnapshot> = emptyList(),
        val hp: Map<String, Int> = emptyMap(),
        val logText: String = "",
        val flashTargetId: String? = null,
        val isFinished: Boolean = false
    )

    sealed interface Event {
        data object SkipAnimation : Event
    }

    sealed interface Effect {
        data object NavigateToResult : Effect
    }
}
