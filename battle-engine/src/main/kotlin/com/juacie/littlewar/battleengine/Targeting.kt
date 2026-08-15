package com.juacie.littlewar.battleengine

object Targeting {

    /**
     * Distance between a unit and an opposing unit. The two 3x5 grids face each other across a
     * 1-cell gap, so depth distance is (attackerRow + defenderRow + 1); lane distance is the
     * column difference. The larger of the two gates whether an attack is in range.
     */
    fun distance(attacker: Position, defender: Position): Int {
        val depthDistance = attacker.row + defender.row + 1
        val laneDistance = kotlin.math.abs(attacker.col - defender.col)
        return maxOf(depthDistance, laneDistance)
    }

    /** Nearest alive enemy in range; ties broken by lowest HP (focus fire), then unit id. */
    fun findPrimaryTarget(actor: UnitInstance, enemies: List<UnitInstance>): UnitInstance? =
        enemies
            .asSequence()
            .filter { it.isAlive }
            .filter { distance(actor.position, it.position) <= actor.definition.attackRange }
            .sortedWith(
                compareBy(
                    { distance(actor.position, it.position) },
                    { it.currentHp },
                    { it.id }
                )
            )
            .firstOrNull()

    fun collectAoeTargets(primary: UnitInstance, shape: AoeShape, allEnemies: List<UnitInstance>): List<UnitInstance> {
        if (shape == AoeShape.SINGLE) return listOf(primary)

        val p = primary.position
        val plusPositions = listOfNotNull(
            p,
            safePosition(p.row - 1, p.col),
            safePosition(p.row + 1, p.col),
            safePosition(p.row, p.col - 1),
            safePosition(p.row, p.col + 1)
        ).toSet()

        return allEnemies.filter { it.isAlive && it.position in plusPositions }
    }

    private fun safePosition(row: Int, col: Int): Position? =
        if (row in 0..2 && col in 0..4) Position(row, col) else null
}
