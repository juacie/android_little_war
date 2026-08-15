package com.juacie.littlewar.domain.usecase

import com.juacie.littlewar.battleengine.GameData
import com.juacie.littlewar.domain.repository.GameDataRepository
import javax.inject.Inject

class GetGameDataUseCase @Inject constructor(
    private val gameDataRepository: GameDataRepository
) {
    operator fun invoke(): GameData = gameDataRepository.getGameData()
}
