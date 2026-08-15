package com.juacie.littlewar.battleengine

import kotlin.test.Test
import kotlin.test.assertEquals

class TargetingTest {

    @Test
    fun distance_adjacentFrontlines_sameLane_isOne() {
        assertEquals(1, Targeting.distance(Position(0, 2), Position(0, 2)))
    }

    @Test
    fun distance_growsWithDepth() {
        assertEquals(2, Targeting.distance(Position(1, 2), Position(0, 2)))
        assertEquals(5, Targeting.distance(Position(2, 2), Position(2, 2)))
    }

    @Test
    fun distance_laneGapCanExceedDepthGap() {
        assertEquals(4, Targeting.distance(Position(0, 0), Position(0, 4)))
    }

    @Test
    fun collectAoeTargets_single_onlyReturnsPrimary() {
        val primary = fakeUnit(Position(1, 2))
        val neighbour = fakeUnit(Position(1, 3))
        val result = Targeting.collectAoeTargets(primary, AoeShape.SINGLE, listOf(primary, neighbour))
        assertEquals(listOf(primary), result)
    }

    @Test
    fun collectAoeTargets_plus_includesOrthogonalNeighboursOnly() {
        val primary = fakeUnit(Position(1, 2))
        val up = fakeUnit(Position(0, 2))
        val down = fakeUnit(Position(2, 2))
        val left = fakeUnit(Position(1, 1))
        val right = fakeUnit(Position(1, 3))
        val diagonal = fakeUnit(Position(0, 1))
        val all = listOf(primary, up, down, left, right, diagonal)

        val result = Targeting.collectAoeTargets(primary, AoeShape.PLUS, all).toSet()

        assertEquals(setOf(primary, up, down, left, right), result)
    }

    private fun fakeUnit(position: Position): UnitInstance {
        val def = UnitDefinition(
            id = "test", name = "test", hp = 10, physicalAttack = 1, magicAttack = 0,
            armor = 0, magicDefense = 0, accuracy = 100.0, evasion = 0.0,
            physicalCritChance = 0.0, magicCritChance = 0.0, attackRange = 1,
            attackSpeed = 1.0, damageType = DamageType.PHYSICAL, element = Element.NONE
        )
        return UnitInstance(
            id = "unit-${position.row}${position.col}",
            definition = def,
            side = BattleSide.ENEMY,
            position = position,
            effectivePhysicalAttack = 1,
            effectiveMagicAttack = 0,
            currentHp = 10
        )
    }
}
