package com.juacie.littlewar.battleengine

/**
 * The battle engine never mutates UI state directly — it emits an ordered event log.
 * Android (and later, the server's own clients) replay this log to animate the fight.
 */
sealed interface BattleEvent {
    val tick: Int
}

data class BattleStartEvent(
    override val tick: Int,
    val playerUnitIds: List<String>,
    val enemyUnitIds: List<String>
) : BattleEvent

data class AttackEvent(
    override val tick: Int,
    val attackerId: String,
    val primaryTargetId: String
) : BattleEvent

data class DamageEvent(
    override val tick: Int,
    val attackerId: String,
    val targetId: String,
    val amount: Int,
    val remainingHp: Int
) : BattleEvent

data class CriticalEvent(
    override val tick: Int,
    val attackerId: String,
    val targetId: String
) : BattleEvent

data class MissEvent(
    override val tick: Int,
    val attackerId: String,
    val targetId: String
) : BattleEvent

data class DeathEvent(
    override val tick: Int,
    val unitId: String
) : BattleEvent

/** 方陣制 Phase 2：沒有敵人在射程內時，方陣改成往最近的敵方方陣移動一格（見 Movement.planStep）。 */
data class MoveEvent(
    override val tick: Int,
    val squadId: String,
    val from: Position,
    val to: Position
) : BattleEvent

enum class BattleOutcome { PLAYER_VICTORY, ENEMY_VICTORY, DRAW }

data class BattleEndEvent(
    override val tick: Int,
    val outcome: BattleOutcome
) : BattleEvent
