package com.juacie.littlewar.data.repository

import com.juacie.littlewar.battleengine.BattleEngine
import com.juacie.littlewar.battleengine.BattleResult
import com.juacie.littlewar.battleengine.BattleSide
import com.juacie.littlewar.battleengine.Formation
import com.juacie.littlewar.battleengine.FormationSlot
import com.juacie.littlewar.battleengine.Position
import com.juacie.littlewar.domain.repository.BattleRepository
import com.juacie.littlewar.domain.repository.GameDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val GRID_COLS = 5

@Singleton
class BattleRepositoryImpl @Inject constructor(
    private val gameDataRepository: GameDataRepository
) : BattleRepository {

    private val _battleResult = MutableStateFlow<BattleResult?>(null)
    override val battleResult: StateFlow<BattleResult?> = _battleResult.asStateFlow()

    override fun runBattle(playerSlots: List<String?>): BattleResult {
        val gameData = gameDataRepository.getGameData()
        val playerFormation = Formation(
            BattleSide.PLAYER,
            playerSlots.mapIndexedNotNull { index, unitId ->
                unitId?.let { FormationSlot(Position(index / GRID_COLS, index % GRID_COLS), it) }
            }
        )
        val enemyFormation = Formation.fromFormationData(BattleSide.ENEMY, gameData.enemyFormation)
        // TODO(Step 4 backend): swap this local simulate() for a server call once :battle-engine
        // runs authoritatively on a Kotlin/Ktor backend — this is the only place that needs to change.
        val result = BattleEngine.simulate(
            playerFormation = playerFormation,
            enemyFormation = enemyFormation,
            gameData = gameData,
            seed = System.currentTimeMillis()
        )
        _battleResult.value = result
        return result
    }

    override fun clear() {
        _battleResult.value = null
    }
}
