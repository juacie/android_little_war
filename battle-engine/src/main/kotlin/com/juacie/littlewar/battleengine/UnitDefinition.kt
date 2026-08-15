package com.juacie.littlewar.battleengine

import kotlinx.serialization.Serializable

/**
 * Data-driven unit stats loaded from game-data JSON. Never hardcode combat numbers
 * in Kotlin code — balance changes should only ever require editing this data.
 */
@Serializable
data class UnitDefinition(
    val id: String,
    val name: String,
    val hp: Int,
    val physicalAttack: Int,
    val magicAttack: Int,
    val armor: Int,
    val magicDefense: Int,
    val accuracy: Double,
    val evasion: Double,
    val physicalCritChance: Double,
    val magicCritChance: Double,
    val critMultiplier: Double = 1.5,
    val attackRange: Int,
    val attackSpeed: Double,
    val damageType: DamageType,
    val element: Element,
    val aoeShape: AoeShape = AoeShape.SINGLE,
    val isLeader: Boolean = false,
    val leaderAttackBuffPercent: Double = 0.0,
    // 方陣人數上限，Phase 1 先用固定值（不做等級成長曲線）。初始猜測值，
    // 待逐兵種 playtest 後再調整，詳見 docs/SquadBattleConcept.md。
    val squadCapacity: Int = 1,
    // 排兵佈陣階段「同一兵種最多可放幾格」的上限，null = 不限制。跟 squadCapacity（單一方陣內部人數）
    // 是不同層級的數字，這個管的是 3x5 戰場上同兵種佔用的格數，避免單一兵種灌爆整個陣容。
    val formationCap: Int? = null
)
