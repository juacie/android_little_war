package com.juacie.littlewar.data.repository

import com.juacie.littlewar.battleengine.StageData
import com.juacie.littlewar.domain.repository.GameDataRepository
import com.juacie.littlewar.domain.repository.StageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StageRepositoryImpl @Inject constructor(
    gameDataRepository: GameDataRepository
) : StageRepository {

    override val stages: List<StageData> = gameDataRepository.getGameData().stages

    private val _selectedStageId = MutableStateFlow(stages.firstOrNull()?.id)
    override val selectedStageId: StateFlow<String?> = _selectedStageId.asStateFlow()

    override fun selectStage(stageId: String) {
        _selectedStageId.value = stageId
    }
}
