package com.juacie.littlewar.ui.stageselect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juacie.littlewar.domain.usecase.GetStagesUseCase
import com.juacie.littlewar.domain.usecase.ObserveSelectedStageUseCase
import com.juacie.littlewar.domain.usecase.SelectStageUseCase
import com.juacie.littlewar.ui.stageselect.StageSelectContract.Effect
import com.juacie.littlewar.ui.stageselect.StageSelectContract.Event
import com.juacie.littlewar.ui.stageselect.StageSelectContract.State
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StageSelectViewModel @Inject constructor(
    getStages: GetStagesUseCase,
    private val observeSelectedStage: ObserveSelectedStageUseCase,
    private val selectStage: SelectStageUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(State(stages = getStages()))
    val state: StateFlow<State> = _state.asStateFlow()

    private val effectChannel = Channel<Effect>()
    val effect = effectChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            observeSelectedStage().collect { selectedStageId ->
                _state.value = _state.value.copy(selectedStageId = selectedStageId)
            }
        }
    }

    fun setEvent(event: Event) {
        when (event) {
            is Event.SelectStage -> selectStage(event.stageId)
            Event.ConfirmStage -> confirmStage()
        }
    }

    private fun confirmStage() {
        if (_state.value.selectedStageId == null) return
        viewModelScope.launch { effectChannel.send(Effect.NavigateToFormation) }
    }
}
