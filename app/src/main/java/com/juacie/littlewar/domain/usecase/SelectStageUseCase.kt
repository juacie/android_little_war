package com.juacie.littlewar.domain.usecase

import com.juacie.littlewar.domain.repository.StageRepository
import javax.inject.Inject

class SelectStageUseCase @Inject constructor(
    private val stageRepository: StageRepository
) {
    operator fun invoke(stageId: String) = stageRepository.selectStage(stageId)
}
