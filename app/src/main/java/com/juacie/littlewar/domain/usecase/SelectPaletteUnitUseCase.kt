package com.juacie.littlewar.domain.usecase

import com.juacie.littlewar.domain.repository.FormationRepository
import javax.inject.Inject

class SelectPaletteUnitUseCase @Inject constructor(
    private val formationRepository: FormationRepository
) {
    operator fun invoke(unitId: String) = formationRepository.selectUnit(unitId)
}
