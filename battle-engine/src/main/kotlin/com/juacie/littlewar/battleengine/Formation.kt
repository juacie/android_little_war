package com.juacie.littlewar.battleengine

data class FormationSlot(val position: Position, val unitDefinitionId: String?)

/** A side's placement of units on the 3x5 grid, before the battle starts. */
data class Formation(val side: BattleSide, val slots: List<FormationSlot>) {

    fun toSquads(gameData: GameData): List<Squad> {
        val occupied = slots.filter { it.unitDefinitionId != null }
        require(occupied.isNotEmpty()) { "Formation for $side has no units" }

        return occupied.mapIndexed { index, slot ->
            val def = gameData.unitById(slot.unitDefinitionId!!)
            // Leader buff 範圍收窄成「限定同方陣」：只有 leader 型方陣自己吃得到自己的
            // leaderAttackBuffPercent，不再像 Milestone 001 那樣套用到整側所有方陣。
            val leaderMultiplier = if (def.isLeader) 1.0 + def.leaderAttackBuffPercent / 100.0 else 1.0
            val maxHp = def.squadCapacity * def.hp
            // 攻擊力要跟著人數上限一起放大——方陣血量池化之後，人越多不只是「更耐打」，
            // 攻擊輸出也代表整個方陣同時出手，否則大方陣會變成純防禦、殺不動小方陣，
            // BattleEngineTest 的 9v1 決勝測試就是在抓這個。BattleEngine.resolveHit 那邊
            // 還會再乘上即時的 hpFraction 做比例戰力衰減，兩者疊加：滿編輸出最高，殘存越少
            // 輸出跟著等比例往下掉。
            val squadAttackMultiplier = def.squadCapacity * leaderMultiplier
            Squad(
                id = "$side-${def.id}-$index-${slot.position.row}${slot.position.col}",
                definition = def,
                side = side,
                position = slot.position,
                capacity = def.squadCapacity,
                maxHp = maxHp,
                effectivePhysicalAttack = (def.physicalAttack * squadAttackMultiplier).toInt(),
                effectiveMagicAttack = (def.magicAttack * squadAttackMultiplier).toInt(),
                currentHp = maxHp
            )
        }
    }

    companion object {
        fun fromFormationData(side: BattleSide, data: FormationData): Formation =
            Formation(side, data.slots.map { FormationSlot(Position(it.row, it.col), it.unitId) })
    }
}
