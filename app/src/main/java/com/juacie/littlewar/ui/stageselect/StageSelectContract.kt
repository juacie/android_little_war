package com.juacie.littlewar.ui.stageselect

import com.juacie.littlewar.battleengine.StageData

object StageSelectContract {
    data class State(
        val stages: List<StageData> = emptyList(),
        val selectedStageId: String? = null
    )

    sealed interface Event {
        data class SelectStage(val stageId: String) : Event
        data object ConfirmStage : Event
    }

    sealed interface Effect {
        data object NavigateToFormation : Effect
    }
}
