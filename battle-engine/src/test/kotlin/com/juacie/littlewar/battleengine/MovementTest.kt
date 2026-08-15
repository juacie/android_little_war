package com.juacie.littlewar.battleengine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MovementTest {

    @Test
    fun planStep_prefersReducingDepth_whenDepthIsTheBottleneck() {
        // depthDistance = 2+2+1=5, laneDistance=0 -> depth is the bottleneck
        assertEquals(Position(1, 2), Movement.planStep(Position(2, 2), Position(2, 2)))
    }

    @Test
    fun planStep_reducesLane_towardHigherColumn_whenLaneIsTheBottleneck() {
        // depthDistance = 0+0+1=1, laneDistance=4 -> lane is the bottleneck
        assertEquals(Position(0, 1), Movement.planStep(Position(0, 0), Position(0, 4)))
    }

    @Test
    fun planStep_reducesLane_towardLowerColumn_whenTargetIsToTheLeft() {
        assertEquals(Position(0, 3), Movement.planStep(Position(0, 4), Position(0, 0)))
    }

    @Test
    fun planStep_tieBreaksTowardReducingDepthFirst() {
        // depthDistance = 1+0+1=2, laneDistance=1 -> depth >= lane, reduce row even though lane is closer
        assertEquals(Position(0, 2), Movement.planStep(Position(1, 2), Position(0, 3)))
    }

    @Test
    fun planStep_fallsBackToLane_whenAlreadyAtTheFrontRow() {
        // depthDistance = 0+0+1=1, laneDistance=2 -> lane is the real bottleneck, row can't shrink further anyway
        assertEquals(Position(0, 1), Movement.planStep(Position(0, 0), Position(0, 2)))
    }

    @Test
    fun planStep_returnsNull_whenAlreadyAsCloseAsMacroGridAllows() {
        // 已經在第一排且橫向對齊，沒有更近的移動方式了（macro grid 上的極限，不代表真的互相打得到）
        assertNull(Movement.planStep(Position(0, 2), Position(0, 2)))
    }

    @Test
    fun planStep_sideSteps_whenPreferredDirectionIsBlocked() {
        // depth 是瓶頸，優先方向是往前一排 (1,2)，但那格被自己人佔住 -> 改試側移 (2,3)
        val blockedByAlly = { pos: Position -> pos == Position(1, 2) }
        assertEquals(Position(2, 3), Movement.planStep(Position(2, 2), Position(2, 4), blockedByAlly))
    }

    @Test
    fun planStep_returnsNull_whenBothPreferredAndFallbackAreBlocked() {
        val blockAll = { _: Position -> true }
        assertNull(Movement.planStep(Position(2, 2), Position(2, 4), blockAll))
    }
}
