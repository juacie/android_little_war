package com.juacie.littlewar.battleengine

import kotlin.test.Test
import kotlin.test.assertTrue

class FormationTest {

    private val gameData = GameDataLoader.loadMilestone001()

    @Test
    fun leaderBuff_increasesEffectiveAttackOfAllies() {
        val withLeader = Formation.fromFormationData(
            BattleSide.PLAYER,
            FormationData(listOf(FormationSlotData("archer", 0, 0), FormationSlotData("leader", 1, 0)))
        ).toUnitInstances(gameData)
        val withoutLeader = Formation.fromFormationData(
            BattleSide.PLAYER,
            FormationData(listOf(FormationSlotData("archer", 0, 0)))
        ).toUnitInstances(gameData)

        val buffedArcher = withLeader.first { it.definition.id == "archer" }
        val plainArcher = withoutLeader.first { it.definition.id == "archer" }

        assertTrue(buffedArcher.effectivePhysicalAttack > plainArcher.effectivePhysicalAttack)
    }

    @Test
    fun milestone001Preset_bothFormationsLoadIntoValidUnitInstances() {
        val player = Formation.fromFormationData(BattleSide.PLAYER, gameData.playerFormation).toUnitInstances(gameData)
        val enemy = Formation.fromFormationData(BattleSide.ENEMY, gameData.enemyFormation).toUnitInstances(gameData)

        assertTrue(player.size == 8)
        assertTrue(enemy.size == 8)
        assertTrue(player.any { it.definition.isLeader })
        assertTrue(enemy.any { it.definition.isLeader })
    }
}
