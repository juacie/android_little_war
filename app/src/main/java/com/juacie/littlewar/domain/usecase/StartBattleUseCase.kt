package com.juacie.littlewar.domain.usecase

import com.juacie.littlewar.battleengine.BattleResult
import com.juacie.littlewar.domain.repository.BattleRepository
import com.juacie.littlewar.domain.repository.FormationRepository
import javax.inject.Inject

class StartBattleUseCase @Inject constructor(
    private val battleRepository: BattleRepository,
    private val formationRepository: FormationRepository
) {
    operator fun invoke(): BattleResult = battleRepository.runBattle(formationRepository.playerSlots.value)
}
