package com.juacie.littlewar.battleengine

/** A live combatant on the battlefield during simulation. */
data class UnitInstance(
    val id: String,
    val definition: UnitDefinition,
    val side: BattleSide,
    val position: Position,
    val effectivePhysicalAttack: Int,
    val effectiveMagicAttack: Int,
    var currentHp: Int,
    var energy: Double = 0.0
) {
    val isAlive: Boolean get() = currentHp > 0
}
