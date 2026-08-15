package com.juacie.littlewar.battleengine

import kotlinx.serialization.Serializable

/** SINGLE hits only the primary target. PLUS also hits enemies orthogonally adjacent to it. */
@Serializable
enum class AoeShape { SINGLE, PLUS }
