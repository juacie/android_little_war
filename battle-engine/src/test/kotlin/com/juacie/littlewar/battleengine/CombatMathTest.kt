package com.juacie.littlewar.battleengine

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CombatMathTest {

    @Test
    fun elementalTriangle_fireBeatsWood_woodBeatsWater_waterBeatsFire() {
        assertEquals(1.25, Element.FIRE.multiplierAgainst(Element.WOOD))
        assertEquals(1.25, Element.WOOD.multiplierAgainst(Element.WATER))
        assertEquals(1.25, Element.WATER.multiplierAgainst(Element.FIRE))
        assertEquals(0.8, Element.WOOD.multiplierAgainst(Element.FIRE))
    }

    @Test
    fun lightAndDark_counterEachOther() {
        assertEquals(1.25, Element.LIGHT.multiplierAgainst(Element.DARK))
        assertEquals(1.25, Element.DARK.multiplierAgainst(Element.LIGHT))
    }

    @Test
    fun noneElement_isAlwaysNeutral() {
        assertEquals(1.0, Element.NONE.multiplierAgainst(Element.FIRE))
        assertEquals(1.0, Element.FIRE.multiplierAgainst(Element.NONE))
    }

    @Test
    fun computeDamage_neverGoesBelowOne_evenAgainstHighDefense() {
        val damage = CombatMath.computeDamage(
            attack = 1,
            defense = 10_000,
            elementMultiplier = 1.0,
            isCritical = false,
            critMultiplier = 1.5
        )
        assertEquals(1, damage)
    }

    @Test
    fun computeDamage_criticalHitsMultiplyDamage() {
        val normal = CombatMath.computeDamage(100, 0, 1.0, isCritical = false, critMultiplier = 1.5)
        val critical = CombatMath.computeDamage(100, 0, 1.0, isCritical = true, critMultiplier = 1.5)
        assertTrue(critical > normal)
    }

    @Test
    fun rollHit_isDeterministic_forAGivenSeed() {
        val a = CombatMath.rollHit(Random(42), accuracy = 90.0, evasion = 10.0)
        val b = CombatMath.rollHit(Random(42), accuracy = 90.0, evasion = 10.0)
        assertEquals(a, b)
    }

    @Test
    fun rollHit_chanceIsClampedToHitBand() {
        val random = Random(1)
        var hits = 0
        val trials = 2000
        repeat(trials) {
            if (CombatMath.rollHit(random, accuracy = 999.0, evasion = 0.0)) hits++
        }
        val hitRate = hits.toDouble() / trials
        assertTrue(hitRate < 1.0, "expected some misses even at extreme accuracy, got rate=$hitRate")
    }
}
