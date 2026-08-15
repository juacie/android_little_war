package com.juacie.littlewar.domain.usecase

import com.juacie.littlewar.battleengine.StageData
import com.juacie.littlewar.domain.repository.StageRepository
import javax.inject.Inject

class GetStagesUseCase @Inject constructor(
    private val stageRepository: StageRepository
) {
    operator fun invoke(): List<StageData> = stageRepository.stages
}
