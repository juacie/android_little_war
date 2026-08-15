package com.juacie.littlewar.ui.formation

import com.juacie.littlewar.battleengine.UnitDefinition

object FormationContract {
    data class State(
        val units: List<UnitDefinition> = emptyList(),
        val slots: List<String?> = List(15) { null },
        val selectedUnitId: String? = null,
        val isValid: Boolean = false,
        val enemyStageName: String = "",
        val enemySlots: List<String?> = List(15) { null }
    )

    sealed interface Event {
        data class SelectUnit(val unitId: String) : Event
        data class TapCell(val row: Int, val col: Int) : Event
        data class MoveUnit(val fromRow: Int, val fromCol: Int, val toRow: Int, val toCol: Int) : Event
        data object ConfirmFormation : Event
    }

    sealed interface Effect {
        data object NavigateToBattle : Effect
    }
}
