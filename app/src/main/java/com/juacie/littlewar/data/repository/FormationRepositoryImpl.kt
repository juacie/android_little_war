package com.juacie.littlewar.data.repository

import com.juacie.littlewar.domain.repository.FormationRepository
import com.juacie.littlewar.domain.repository.GameDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val GRID_ROWS = 3
private const val GRID_COLS = 5

@Singleton
class FormationRepositoryImpl @Inject constructor(
    gameDataRepository: GameDataRepository
) : FormationRepository {

    private val gameData = gameDataRepository.getGameData()

    private val _playerSlots = MutableStateFlow(buildInitialSlots())
    override val playerSlots: StateFlow<List<String?>> = _playerSlots.asStateFlow()

    private val _selectedUnitId = MutableStateFlow(gameData.units.firstOrNull { !it.isLeader }?.id)
    override val selectedUnitId: StateFlow<String?> = _selectedUnitId.asStateFlow()

    override fun selectUnit(unitId: String) {
        _selectedUnitId.value = unitId
    }

    override fun toggleUnitAt(row: Int, col: Int) {
        val index = row * GRID_COLS + col
        val selected = _selectedUnitId.value
        _playerSlots.value = _playerSlots.value.toMutableList().apply {
            this[index] = if (this[index] == selected) null else selected
        }
    }

    private fun buildInitialSlots(): List<String?> {
        val slots = MutableList<String?>(GRID_ROWS * GRID_COLS) { null }
        gameData.playerFormation.slots.forEach { slot ->
            slots[slot.row * GRID_COLS + slot.col] = slot.unitId
        }
        return slots
    }
}
