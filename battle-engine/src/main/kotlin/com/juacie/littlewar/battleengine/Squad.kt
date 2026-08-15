package com.juacie.littlewar.battleengine

/**
 * A live squad on the battlefield during simulation — Phase 1 of the 方陣制 redesign
 * (see docs/SquadBattleConcept.md). One formation slot = one squad with pooled HP,
 * not a single combatant.
 */
data class Squad(
    val id: String,
    val definition: UnitDefinition,
    val side: BattleSide,
    // 方陣制 Phase 2（移動機制）：position 從此可變——沒有目標在射程內時，方陣會往最近的
    // 敵方方陣移動一格（見 Movement.planStep），而不是原地 idle。
    var position: Position,
    val capacity: Int,
    val maxHp: Int,
    val effectivePhysicalAttack: Int,
    val effectiveMagicAttack: Int,
    var currentHp: Int,
    var energy: Double = 0.0
) {
    val isAlive: Boolean get() = currentHp > 0

    // 比例戰力衰減：方陣殘存血量比例，戰鬥時用來即時打折攻擊力/命中率（不能烤成常數，
    // 因為血量會隨戰鬥即時變化）。方陣剛死亡時 currentHp = 0，這裡會回傳 0.0，呼叫端
    // 不應該再對已死亡的方陣做傷害判定，所以不會拿 0.0 去做除法以外的危險運算。
    val hpFraction: Double get() = currentHp.toDouble() / maxHp

    // 細格參與戰鬥計算：方陣打到剩不到一半人時，殘存成員會往方陣內部（5x5 細格裡遠離敵人
    // 的那一側）收縮、變得更難瞄準——用「命中率變低」而不是「拉開距離」表現這件事，是刻意
    // 的選擇：這個階段沒有移動機制，如果把細格收縮換成距離懲罰，一個已經在攻擊範圍內的近戰
    // 方陣一旦把對方打到半血以下，對方就可能瞬間退出自己的射程，永遠打不死、卡成平手
    // （這正是加了距離懲罰版本時 BattleEngineTest 的 9v1 決勝測試實際炸掉的方式）。
    // 用命中率懲罰就不會有這個問題：永遠打得到，只是比較容易打偏。
    val retreatEvasionBonus: Double get() = if (hpFraction <= 0.5) 10.0 else 0.0
}
