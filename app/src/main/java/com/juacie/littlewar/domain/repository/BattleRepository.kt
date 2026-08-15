package com.juacie.littlewar.domain.repository

import com.juacie.littlewar.battleengine.BattleResult
import kotlinx.coroutines.flow.StateFlow

/**
 * Runs and holds the most recent battle result. Today this simulates locally via :battle-engine;
 * once a backend exists, only this implementation needs to change to call the server instead —
 * nothing above the domain layer should need to know the difference.
 */
interface BattleRepository {
    val battleResult: StateFlow<BattleResult?>

    fun runBattle(playerSlots: List<String?>): BattleResult
    fun clear()
}
