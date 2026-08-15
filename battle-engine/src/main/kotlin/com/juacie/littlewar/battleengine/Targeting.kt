package com.juacie.littlewar.battleengine

object Targeting {

    /**
     * Distance between a squad and an opposing squad. The two 3x5 grids face each other across a
     * 1-cell gap, so depth distance is (attackerRow + defenderRow + 1); lane distance is the
     * column difference. The larger of the two gates whether an attack is in range.
     *
     * Deliberately NOT affected by a squad's internal retreat state (see Squad.retreatEvasionBonus):
     * without movement, pushing a weakened squad out of range here could permanently strand it
     * just out of reach of the only attacker in range, stalling the fight into a draw forever.
     */
    fun distance(attacker: Squad, defender: Squad): Int {
        val depthDistance = attacker.position.row + defender.position.row + 1
        val laneDistance = kotlin.math.abs(attacker.position.col - defender.position.col)
        return maxOf(depthDistance, laneDistance)
    }

    /** Nearest alive enemy squad in range; ties broken by lowest HP (focus fire), then squad id. */
    fun findPrimaryTarget(actor: Squad, enemies: List<Squad>): Squad? =
        enemies
            .asSequence()
            .filter { it.isAlive }
            .filter { distance(actor, it) <= actor.definition.attackRange }
            .sortedWith(
                compareBy(
                    { distance(actor, it) },
                    { it.currentHp },
                    { it.id }
                )
            )
            .firstOrNull()

    fun collectAoeTargets(primary: Squad, shape: AoeShape, allEnemies: List<Squad>): List<Squad> {
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
