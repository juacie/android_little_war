package com.juacie.littlewar.ui.formation

import androidx.lifecycle.viewModelScope
import com.juacie.littlewar.battleengine.StageData
import com.juacie.littlewar.domain.usecase.GetGameDataUseCase
import com.juacie.littlewar.domain.usecase.GetStagesUseCase
import com.juacie.littlewar.domain.usecase.MoveUnitAtCellUseCase
import com.juacie.littlewar.domain.usecase.ObserveFormationUseCase
import com.juacie.littlewar.domain.usecase.ObserveSelectedStageUseCase
import com.juacie.littlewar.domain.usecase.SelectPaletteUnitUseCase
import com.juacie.littlewar.domain.usecase.StartBattleUseCase
import com.juacie.littlewar.domain.usecase.ToggleUnitAtCellUseCase
import com.juacie.littlewar.domain.usecase.ValidateFormationUseCase
import com.juacie.littlewar.ui.formation.FormationContract.Effect
import com.juacie.littlewar.ui.formation.FormationContract.Event
import com.juacie.littlewar.ui.formation.FormationContract.State
import com.juacie.littlewar.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val GRID_ROWS = 3
private const val GRID_COLS = 5

@HiltViewModel
class FormationViewModel @Inject constructor(
    getGameData: GetGameDataUseCase,
    getStages: GetStagesUseCase,
    private val observeSelectedStage: ObserveSelectedStageUseCase,
    private val observeFormation: ObserveFormationUseCase,
    private val selectPaletteUnit: SelectPaletteUnitUseCase,
    private val toggleUnitAtCell: ToggleUnitAtCellUseCase,
    private val moveUnitAtCell: MoveUnitAtCellUseCase,
    private val validateFormation: ValidateFormationUseCase,
    private val startBattle: StartBattleUseCase
) : MviViewModel<State, Event, Effect>(State(units = getGameData().units)) {

    private val stages = getStages()

    init {
        viewModelScope.launch {
            observeFormation().collect { snapshot ->
                setState {
                    copy(
                        slots = snapshot.slots,
                        selectedUnitId = snapshot.selectedUnitId,
                        isValid = validateFormation(snapshot.slots)
                    )
                }
            }
        }
        viewModelScope.launch {
            observeSelectedStage().collect { stageId ->
                val stage = stages.firstOrNull { it.id == stageId }
                setState {
                    copy(
                        enemyStageName = stage?.name.orEmpty(),
                        enemySlots = buildEnemySlots(stage)
                    )
                }
            }
        }
    }

    override fun setEvent(event: Event) {
        when (event) {
            is Event.SelectUnit -> selectPaletteUnit(event.unitId)
            is Event.TapCell -> toggleUnitAtCell(event.row, event.col)
            is Event.MoveUnit -> moveUnitAtCell(event.fromRow, event.fromCol, event.toRow, event.toCol)
            Event.ConfirmFormation -> confirmFormation()
        }
    }

    private fun confirmFormation() {
        if (!currentState.isValid) return
        startBattle()
        sendEffect(Effect.NavigateToBattle)
    }

    private fun buildEnemySlots(stage: StageData?): List<String?> {
        val slots = MutableList<String?>(GRID_ROWS * GRID_COLS) { null }
        stage?.enemyFormation?.slots?.forEach { slot ->
            slots[slot.row * GRID_COLS + slot.col] = slot.unitId
        }
        return slots
    }
}
