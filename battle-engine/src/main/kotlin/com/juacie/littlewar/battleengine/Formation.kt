package com.juacie.littlewar.battleengine

data class FormationSlot(val position: Position, val unitDefinitionId: String?)

/** A side's placement of units on the 3x5 grid, before the battle starts. */
data class Formation(val side: BattleSide, val slots: List<FormationSlot>) {

    fun toUnitInstances(gameData: GameData): List<UnitInstance> {
        val occupied = slots.filter { it.unitDefinitionId != null }
        require(occupied.isNotEmpty()) { "Formation for $side has no units" }

        val leaderBuffPercent = occupied
            .mapNotNull { slot -> gameData.unitById(slot.unitDefinitionId!!).takeIf { it.isLeader }?.leaderAttackBuffPercent }
            .maxOrNull() ?: 0.0
        val multiplier = 1.0 + leaderBuffPercent / 100.0

        return occupied.mapIndexed { index, slot ->
            val def = gameData.unitById(slot.unitDefinitionId!!)
            UnitInstance(
                id = "$side-${def.id}-$index-${slot.position.row}${slot.position.col}",
                definition = def,
                side = side,
                position = slot.position,
                effectivePhysicalAttack = (def.physicalAttack * multiplier).toInt(),
                effectiveMagicAttack = (def.magicAttack * multiplier).toInt(),
                currentHp = def.hp
            )
        }
    }

    companion object {
        fun fromFormationData(side: BattleSide, data: FormationData): Formation =
            Formation(side, data.slots.map { FormationSlot(Position(it.row, it.col), it.unitId) })
    }
}
