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

    @Test
    fun simulate_tiedEnergyActors_areNotAlwaysResolvedInFavorOfOneSide() {
        // Two identical lone units always reach the energy threshold on the exact same tick, so
        // every seed produces a same-tick tie. Unit id ("ENEMY-..." < "PLAYER-..." alphabetically)
        // must not decide who acts first in that tie, or the enemy side gets a systematic
        // first-strike advantage in every mirror matchup.
        val onePlayer = Formation.fromFormationData(
            BattleSide.PLAYER,
            FormationData(listOf(FormationSlotData("shield", 0, 0)))
        )
        val oneEnemy = Formation.fromFormationData(
            BattleSide.ENEMY,
            FormationData(listOf(FormationSlotData("shield", 0, 0)))
        )

        val firstActorSides = (1..40L).map { seed ->
            val result = BattleEngine.simulate(onePlayer, oneEnemy, gameData, seed)
            val firstAttack = result.events.filterIsInstance<AttackEvent>().first()
            if (firstAttack.attackerId.startsWith("PLAYER")) BattleSide.PLAYER else BattleSide.ENEMY
        }

        assertTrue(firstActorSides.contains(BattleSide.PLAYER), "enemy should not always win the tied first strike")
        assertTrue(firstActorSides.contains(BattleSide.ENEMY), "player should not always win the tied first strike")
    }
}
