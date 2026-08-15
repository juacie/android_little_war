package com.juacie.littlewar.domain.usecase

import com.juacie.littlewar.battleengine.BattleResult
import com.juacie.littlewar.domain.repository.BattleRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveBattleResultUseCase @Inject constructor(
    private val battleRepository: BattleRepository
) {
    operator fun invoke(): StateFlow<BattleResult?> = battleRepository.battleResult
}
