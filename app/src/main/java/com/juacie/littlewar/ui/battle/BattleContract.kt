package com.juacie.littlewar.ui.battle

import com.juacie.littlewar.battleengine.Position
import com.juacie.littlewar.battleengine.SquadSnapshot

object BattleContract {
    /** 目前正在出手的攻擊者／目標，讓畫面上能對攻擊者播放撲擊動畫。seq 保證每次攻擊都是新的觸發鍵。 */
    data class ActiveAttack(val attackerId: String, val targetId: String, val seq: Int)

    data class State(
        val roster: List<SquadSnapshot> = emptyList(),
        val hp: Map<String, Int> = emptyMap(),
        val positions: Map<String, Position> = emptyMap(),
        val logText: String = "",
        val flashTargetId: String? = null,
        val activeAttack: ActiveAttack? = null,
        val isFinished: Boolean = false
    )

    sealed interface Event {
        data object SkipAnimation : Event
    }

    sealed interface Effect {
        data object NavigateToResult : Effect
    }
}
