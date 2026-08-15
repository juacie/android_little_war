package com.juacie.littlewar.domain.usecase

import com.juacie.littlewar.domain.repository.GameDataRepository
import javax.inject.Inject

/** A formation is battle-ready with exactly one leader and at least one other unit. */
class ValidateFormationUseCase @Inject constructor(
    private val gameDataRepository: GameDataRepository
) {
    operator fun invoke(slots: List<String?>): Boolean {
        val gameData = gameDataRepository.getGameData()
        val filled = slots.filterNotNull()
        val leaderCount = filled.count { gameData.unitById(it).isLeader }
        return leaderCount == 1 && filled.size > leaderCount
    }
}
