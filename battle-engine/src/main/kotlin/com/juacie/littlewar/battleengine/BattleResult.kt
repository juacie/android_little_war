package com.juacie.littlewar.battleengine

/** A squad's fixed starting info, so a client can render the board without re-deriving it from Formation. */
data class SquadSnapshot(
    val id: String,
    val name: String,
    val side: BattleSide,
    val position: Position,
    val maxHp: Int,
    val isLeader: Boolean
)

data class BattleResult(
    val outcome: BattleOutcome,
    val totalTicks: Int,
    val roster: List<SquadSnapshot>,
    val events: List<BattleEvent>,
    val survivingPlayerUnitIds: List<String>,
    val survivingEnemyUnitIds: List<String>
)
