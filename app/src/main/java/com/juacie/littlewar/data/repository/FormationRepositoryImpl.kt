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
        val current = _playerSlots.value
        val next = if (current[index] == selected) null else selected
        // 放置新兵種前先檢查兵種數量上限（data-driven，見 UnitDefinition.formationCap）；
        // 上限到了就整個 tap 無效，維持原陣容不變，不用彈提示（沿用既有的「不合法就不變動」模式）。
        if (next != null) {
            val cap = gameData.unitById(next).formationCap
            if (cap != null && current.count { it == next } >= cap) return
        }
        _playerSlots.value = current.toMutableList().apply { this[index] = next }
    }

    override fun moveUnitAt(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int) {
        val fromIndex = fromRow * GRID_COLS + fromCol
        val toIndex = toRow * GRID_COLS + toCol
        if (fromIndex == toIndex) return
        _playerSlots.value = _playerSlots.value.toMutableList().apply {
            val temp = this[toIndex]
            this[toIndex] = this[fromIndex]
            this[fromIndex] = temp
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
