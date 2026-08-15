package com.juacie.littlewar.domain.repository

import com.juacie.littlewar.battleengine.StageData
import kotlinx.coroutines.flow.StateFlow

/** Holds the list of PvE stages and which one the player picked, shared across StageSelect/Battle screens. */
interface StageRepository {
    val stages: List<StageData>
    val selectedStageId: StateFlow<String?>

    fun selectStage(stageId: String)
}
