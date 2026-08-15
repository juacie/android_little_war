package com.juacie.littlewar.ui.formation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juacie.littlewar.domain.usecase.GetGameDataUseCase
import com.juacie.littlewar.domain.usecase.ObserveFormationUseCase
import com.juacie.littlewar.domain.usecase.SelectPaletteUnitUseCase
import com.juacie.littlewar.domain.usecase.StartBattleUseCase
import com.juacie.littlewar.domain.usecase.ToggleUnitAtCellUseCase
import com.juacie.littlewar.domain.usecase.ValidateFormationUseCase
import com.juacie.littlewar.ui.formation.FormationContract.Effect
import com.juacie.littlewar.ui.formation.FormationContract.Event
import com.juacie.littlewar.ui.formation.FormationContract.State
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FormationViewModel @Inject constructor(
    getGameData: GetGameDataUseCase,
    private val observeFormation: ObserveFormationUseCase,
    private val selectPaletteUnit: SelectPaletteUnitUseCase,
    private val toggleUnitAtCell: ToggleUnitAtCellUseCase,
    private val validateFormation: ValidateFormationUseCase,
    private val startBattle: StartBattleUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(State(units = getGameData().units))
    val state: StateFlow<State> = _state.asStateFlow()

    private val effectChannel = Channel<Effect>()
    val effect = effectChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            observeFormation().collect { snapshot ->
                _state.value = _state.value.copy(
                    slots = snapshot.slots,
                    selectedUnitId = snapshot.selectedUnitId,
                    isValid = validateFormation(snapshot.slots)
                )
            }
        }
    }

    fun setEvent(event: Event) {
        when (event) {
            is Event.SelectUnit -> selectPaletteUnit(event.unitId)
            is Event.TapCell -> toggleUnitAtCell(event.row, event.col)
            Event.ConfirmFormation -> confirmFormation()
        }
    }

    private fun confirmFormation() {
        if (!_state.value.isValid) return
        startBattle()
        viewModelScope.launch { effectChannel.send(Effect.NavigateToBattle) }
    }
}
