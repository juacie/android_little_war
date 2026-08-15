package com.juacie.littlewar.battleengine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BattleEngineTest {

    private val gameData = GameDataLoader.loadMilestone001()
    private val player = Formation.fromFormationData(BattleSide.PLAYER, gameData.playerFormation)
    private val enemy = Formation.fromFormationData(BattleSide.ENEMY, gameData.enemyFormation)

    @Test
    fun simulate_isDeterministic_forTheSameSeed() {
        val resultA = BattleEngine.simulate(player, enemy, gameData, seed = 12345L)
        val resultB = BattleEngine.simulate(player, enemy, gameData, seed = 12345L)

        assertEquals(resultA.outcome, resultB.outcome)
        assertEquals(resultA.totalTicks, resultB.totalTicks)
        assertEquals(resultA.events.map { it::class }, resultB.events.map { it::class })
    }

    @Test
    fun simulate_differentSeeds_produceDifferentEventSequences() {
        val results = (1..20L).map { seed -> BattleEngine.simulate(player, enemy, gameData, seed) }
        val distinctEventSequences = results.map { it.events.toString() }.distinct()
        assertTrue(distinctEventSequences.size > 1, "expected RNG-driven event sequences to vary across seeds")
    }

    @Test
    fun simulate_alwaysEndsWithABattleEndEvent_withinTickCap() {
        val result = BattleEngine.simulate(player, enemy, gameData, seed = 7L)
        assertTrue(result.events.last() is BattleEndEvent)
        assertTrue(result.totalTicks <= 300)
    }

    @Test
    fun simulate_massiveNumericalAdvantage_winsDecisively() {
        val overwhelmingSlots = listOf(
            FormationSlotData("shield", 0, 0), FormationSlotData("shield", 0, 1), FormationSlotData("shield", 0, 2),
            FormationSlotData("shield", 1, 0), FormationSlotData("shield", 1, 1), FormationSlotData("shield", 1, 2),
            FormationSlotData("shield", 2, 0), FormationSlotData("shield", 2, 1), FormationSlotData("shield", 2, 2)
        )
        val bigPlayer = Formation.fromFormationData(BattleSide.PLAYER, FormationData(overwhelmingSlots))
        val loneEnemy = Formation.fromFormationData(
            BattleSide.ENEMY,
            FormationData(listOf(FormationSlotData("leader", 0, 0)))
        )

        val result = BattleEngine.simulate(bigPlayer, loneEnemy, gameData, seed = 99L)
        assertEquals(BattleOutcome.PLAYER_VICTORY, result.outcome)
    }
}
