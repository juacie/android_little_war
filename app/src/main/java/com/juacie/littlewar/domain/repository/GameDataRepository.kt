package com.juacie.littlewar.domain.repository

import com.juacie.littlewar.battleengine.GameData

/** Source of truth for data-driven unit/formation definitions. Local JSON today, server later. */
interface GameDataRepository {
    fun getGameData(): GameData
}
