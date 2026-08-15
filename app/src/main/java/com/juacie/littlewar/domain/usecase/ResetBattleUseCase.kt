package com.juacie.littlewar.domain.usecase

import com.juacie.littlewar.domain.repository.BattleRepository
import javax.inject.Inject

class ResetBattleUseCase @Inject constructor(
    private val battleRepository: BattleRepository
) {
    operator fun invoke() = battleRepository.clear()
}
