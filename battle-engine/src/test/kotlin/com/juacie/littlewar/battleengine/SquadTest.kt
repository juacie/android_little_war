package com.juacie.littlewar.battleengine

import kotlin.test.Test
import kotlin.test.assertEquals

class SquadTest {

    private val def = UnitDefinition(
        id = "test", name = "test", hp = 100, physicalAttack = 1, magicAttack = 0,
        armor = 0, magicDefense = 0, accuracy = 100.0, evasion = 0.0,
        physicalCritChance = 0.0, magicCritChance = 0.0, attackRange = 1,
        attackSpeed = 1.0, damageType = DamageType.PHYSICAL, element = Element.NONE,
        squadCapacity = 1
    )

    private fun squad(currentHp: Int, maxHp: Int = 100) = Squad(
        id = "squad", definition = def, side = BattleSide.PLAYER, position = Position(0, 0),
        capacity = 1, maxHp = maxHp, effectivePhysicalAttack = 1, effectiveMagicAttack = 0,
        currentHp = currentHp
    )

    @Test
    fun hpFraction_isCurrentOverMax() {
        assertEquals(0.75, squad(currentHp = 75, maxHp = 100).hpFraction)
    }

    @Test
    fun retreatEvasionBonus_isZero_aboveHalfStrength() {
        assertEquals(0.0, squad(currentHp = 51, maxHp = 100).retreatEvasionBonus)
    }

    @Test
    fun retreatEvasionBonus_appliesAtHalfStrengthOrBelow() {
        assertEquals(10.0, squad(currentHp = 50, maxHp = 100).retreatEvasionBonus)
        assertEquals(10.0, squad(currentHp = 10, maxHp = 100).retreatEvasionBonus)
    }
}
