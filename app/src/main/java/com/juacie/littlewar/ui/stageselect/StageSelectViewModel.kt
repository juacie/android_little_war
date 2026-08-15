package com.juacie.littlewar.ui.stageselect

import androidx.lifecycle.viewModelScope
import com.juacie.littlewar.domain.usecase.GetStagesUseCase
import com.juacie.littlewar.domain.usecase.ObserveSelectedStageUseCase
import com.juacie.littlewar.domain.usecase.SelectStageUseCase
import com.juacie.littlewar.ui.mvi.MviViewModel
import com.juacie.littlewar.ui.stageselect.StageSelectContract.Effect
import com.juacie.littlewar.ui.stageselect.StageSelectContract.Event
import com.juacie.littlewar.ui.stageselect.StageSelectContract.State
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StageSelectViewModel @Inject constructor(
    getStages: GetStagesUseCase,
    private val observeSelectedStage: ObserveSelectedStageUseCase,
    private val selectStage: SelectStageUseCase
) : MviViewModel<State, Event, Effect>(State(stages = getStages())) {

    init {
        viewModelScope.launch {
            observeSelectedStage().collect { selectedStageId ->
                setState { copy(selectedStageId = selectedStageId) }
            }
        }
    }

    override fun setEvent(event: Event) {
        when (event) {
            is Event.SelectStage -> selectStage(event.stageId)
            Event.ConfirmStage -> confirmStage()
        }
    }

    private fun confirmStage() {
        if (currentState.selectedStageId == null) return
        sendEffect(Effect.NavigateToFormation)
    }
}
