package com.juacie.littlewar.battleengine

import kotlin.random.Random

/** Pure, seed-driven combat formulas. See docs/BattleSystem.md for the rationale behind each constant. */
object CombatMath {
    private const val MIN_HIT_CHANCE = 5.0
    private const val MAX_HIT_CHANCE = 95.0
    private const val MIN_CRIT_CHANCE = 0.0
    private const val MAX_CRIT_CHANCE = 75.0

    fun rollHit(random: Random, accuracy: Double, evasion: Double): Boolean {
        val hitChance = (accuracy - evasion).coerceIn(MIN_HIT_CHANCE, MAX_HIT_CHANCE)
        return random.nextDouble(0.0, 100.0) < hitChance
    }

    fun rollCritical(random: Random, critChance: Double): Boolean {
        val chance = critChance.coerceIn(MIN_CRIT_CHANCE, MAX_CRIT_CHANCE)
        return random.nextDouble(0.0, 100.0) < chance
    }

    fun computeDamage(
        attack: Int,
        defense: Int,
        elementMultiplier: Double,
        isCritical: Boolean,
        critMultiplier: Double
    ): Int {
        val mitigation = 100.0 / (100.0 + defense)
        val critFactor = if (isCritical) critMultiplier else 1.0
        val raw = attack * mitigation * elementMultiplier * critFactor
        return raw.toInt().coerceAtLeast(1)
    }
}
