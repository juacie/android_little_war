package com.juacie.littlewar.battleengine

/** A cell in the 3 (row, depth) x 5 (col, lane) battlefield grid. Row 0 is the frontline. */
data class Position(val row: Int, val col: Int) {
    init {
        require(row in 0..2) { "row must be within 0..2, was $row" }
        require(col in 0..4) { "col must be within 0..4, was $col" }
    }
}
