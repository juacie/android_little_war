package com.juacie.littlewar.battleengine

import kotlinx.serialization.Serializable

@Serializable
enum class Element {
    NONE,
    FIRE,
    WATER,
    WOOD,
    LIGHT,
    DARK;

    /** Elemental damage multiplier when [this] attacks [defender]. */
    fun multiplierAgainst(defender: Element): Double = when {
        this == NONE || defender == NONE -> 1.0
        beats(defender) -> 1.25
        defender.beats(this) -> 0.8
        else -> 1.0
    }

    private fun beats(other: Element): Boolean = when (this) {
        FIRE -> other == WOOD
        WOOD -> other == WATER
        WATER -> other == FIRE
        LIGHT -> other == DARK
        DARK -> other == LIGHT
        NONE -> false
    }
}
