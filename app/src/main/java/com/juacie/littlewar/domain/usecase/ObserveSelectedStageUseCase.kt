package com.juacie.littlewar.domain.usecase

import com.juacie.littlewar.domain.repository.StageRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveSelectedStageUseCase @Inject constructor(
    private val stageRepository: StageRepository
) {
    operator fun invoke(): StateFlow<String?> = stageRepository.selectedStageId
}
