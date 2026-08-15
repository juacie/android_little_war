package com.juacie.littlewar.battleengine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FormationTest {

    private val gameData = GameDataLoader.loadMilestone001()

    @Test
    fun leaderBuff_increasesOnlyTheLeadersOwnSquad() {
        val withLeader = Formation.fromFormationData(
            BattleSide.PLAYER,
            FormationData(listOf(FormationSlotData("archer", 0, 0), FormationSlotData("leader", 1, 0)))
        ).toSquads(gameData)
        val withoutLeader = Formation.fromFormationData(
            BattleSide.PLAYER,
            FormationData(listOf(FormationSlotData("archer", 0, 0)))
        ).toSquads(gameData)

        // Leader buff 範圍收窄成「限定同方陣」：領袖方陣旁邊的弓兵方陣不應該再吃到 buff。
        val archerNextToLeader = withLeader.first { it.definition.id == "archer" }
        val plainArcher = withoutLeader.first { it.definition.id == "archer" }
        assertEquals(plainArcher.effectivePhysicalAttack, archerNextToLeader.effectivePhysicalAttack)

        // 但領袖方陣自己吃得到自己的 leaderAttackBuffPercent。
        val leaderSquad = withLeader.first { it.definition.id == "leader" }
        val baseLeaderAttack = gameData.unitById("leader").physicalAttack
        assertTrue(leaderSquad.effectivePhysicalAttack > baseLeaderAttack)
    }

    @Test
    fun milestone001Preset_bothFormationsLoadIntoValidSquads() {
        val player = Formation.fromFormationData(BattleSide.PLAYER, gameData.playerFormation).toSquads(gameData)
        val enemy = Formation.fromFormationData(BattleSide.ENEMY, gameData.stageById("stage-02-legion").enemyFormation).toSquads(gameData)

        assertTrue(player.size == 8)
        assertTrue(enemy.size == 8)
        assertTrue(player.any { it.definition.isLeader })
        assertTrue(enemy.any { it.definition.isLeader })
    }

    @Test
    fun squadMaxHp_isCapacityTimesUnitHp() {
        val squads = Formation.fromFormationData(
            BattleSide.PLAYER,
            FormationData(listOf(FormationSlotData("shield", 0, 0)))
        ).toSquads(gameData)

        val shieldDef = gameData.unitById("shield")
        val squad = squads.first()
        assertEquals(shieldDef.squadCapacity * shieldDef.hp, squad.maxHp)
        assertEquals(squad.maxHp, squad.currentHp)
    }
}
