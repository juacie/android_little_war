package com.juacie.littlewar.battleengine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Covers the militia/cavalry/crossbow/assassin roster added on top of the Milestone 001 set. */
class RosterExpansionTest {

    private val gameData = GameDataLoader.loadMilestone001()

    @Test
    fun newUnits_areDefinedWithExpectedRoles() {
        val militia = gameData.unitById("militia")
        assertEquals(DamageType.PHYSICAL, militia.damageType)
        assertEquals(Element.NONE, militia.element)
        assertEquals(1, militia.attackRange)

        val cavalry = gameData.unitById("cavalry")
        assertEquals(Element.WOOD, cavalry.element)
        assertEquals(1, cavalry.attackRange)

        val crossbow = gameData.unitById("crossbow")
        assertEquals(Element.LIGHT, crossbow.element)
        assertEquals(5, crossbow.attackRange)

        val assassin = gameData.unitById("assassin")
        assertEquals(Element.DARK, assassin.element)
        assertTrue(assassin.evasion > militia.evasion, "assassin should be more evasive than the basic militia")
        assertTrue(assassin.physicalCritChance > militia.physicalCritChance, "assassin should crit more often than militia")
    }

    @Test
    fun newUnits_canBattle_andSimulationTerminates() {
        val player = Formation.fromFormationData(
            BattleSide.PLAYER,
            FormationData(
                listOf(
                    FormationSlotData("cavalry", 0, 1),
                    FormationSlotData("assassin", 0, 2),
                    FormationSlotData("crossbow", 2, 2),
                    FormationSlotData("leader", 1, 2)
                )
            )
        )
        val enemy = Formation.fromFormationData(
            BattleSide.ENEMY,
            FormationData(
                listOf(
                    FormationSlotData("militia", 0, 1),
                    FormationSlotData("militia", 0, 2),
                    FormationSlotData("militia", 0, 3)
                )
            )
        )

        val result = BattleEngine.simulate(player, enemy, gameData, seed = 2024L)

        assertTrue(result.events.last() is BattleEndEvent)
        assertTrue(result.totalTicks <= 300)
    }
}
