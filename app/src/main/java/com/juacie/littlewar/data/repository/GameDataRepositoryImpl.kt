package com.juacie.littlewar.data.repository

import com.juacie.littlewar.battleengine.GameData
import com.juacie.littlewar.battleengine.GameDataLoader
import com.juacie.littlewar.domain.repository.GameDataRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameDataRepositoryImpl @Inject constructor() : GameDataRepository {
    // 用 by lazy 只在第一次呼叫時讀取 JSON，之後重複使用同一份記憶體內的資料
    private val cachedGameData: GameData by lazy { GameDataLoader.loadMilestone001() }

    override fun getGameData(): GameData = cachedGameData
}
