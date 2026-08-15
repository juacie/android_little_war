package com.juacie.littlewar.domain.usecase

import com.juacie.littlewar.domain.repository.FormationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class FormationSnapshot(val slots: List<String?>, val selectedUnitId: String?)

class ObserveFormationUseCase @Inject constructor(
    private val formationRepository: FormationRepository
) {
    operator fun invoke(): Flow<FormationSnapshot> =
        combine(formationRepository.playerSlots, formationRepository.selectedUnitId) { slots, selected ->
            FormationSnapshot(slots, selected)
        }
}
