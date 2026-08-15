package com.juacie.littlewar.battleengine

/**
 * 方陣制 Phase 2（移動機制，見 docs/SquadBattleConcept.md）。構想文件原本設計的是 5x5 細格移動，
 * 但那需要方陣人數成長曲線這種還沒拍板的系統；這階段先在既有的 3x5 macro grid 上做「一次一格」的
 * 移動，之後真的要做細格移動時再擴充，不寫死死板 API。
 */
object Movement {

    /**
     * 往 [target] 靠近一格的確定性移動規則，找不到能縮短距離的方向就回傳 null（不消耗這次移動）。
     * distance = max(depthDistance, laneDistance)（見 Targeting.distance），所以永遠優先縮短
     * 目前「卡住」的那個軸；兩軸打平時優先縮短縱深（往自己那側的第一排靠）。
     *
     * [isBlocked] 讓呼叫端回報某個候選格是否被自己人佔住（預設都不擋，維持原本單純的幾何計算，
     * 也是既有測試呼叫兩參數版本時的行為）。優先方向被擋住時，會改試另一軸的候選格（側移），
     * 兩個方向都擋住（或另一軸根本沒得走）才真的回傳 null——避免同一路的後排方陣卡死在原地，
     * 直到前排死亡騰出格子才會動。
     */
    fun planStep(mover: Position, target: Position, isBlocked: (Position) -> Boolean = { false }): Position? {
        val depthDistance = mover.row + target.row + 1
        val laneDistance = kotlin.math.abs(mover.col - target.col)
        val rowStep = if (mover.row > 0) Position(mover.row - 1, mover.col) else null
        val colStep = if (mover.col != target.col) {
            Position(mover.row, mover.col + if (target.col > mover.col) 1 else -1)
        } else {
            null
        }

        val primary = when {
            depthDistance >= laneDistance && rowStep != null -> rowStep
            colStep != null -> colStep
            else -> rowStep
        }
        val secondary = if (primary === rowStep) colStep else rowStep

        return listOfNotNull(primary, secondary).firstOrNull { !isBlocked(it) }
    }
}
