package com.juacie.littlewar.domain.repository

import kotlinx.coroutines.flow.StateFlow

/** Holds the player's in-progress formation for the session, shared across Formation/Battle screens. */
interface FormationRepository {
    val playerSlots: StateFlow<List<String?>>
    val selectedUnitId: StateFlow<String?>

    fun selectUnit(unitId: String)
    fun toggleUnitAt(row: Int, col: Int)
    fun moveUnitAt(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int)
}
