package com.juacie.littlewar.domain.usecase

import com.juacie.littlewar.domain.repository.FormationRepository
import javax.inject.Inject

class ToggleUnitAtCellUseCase @Inject constructor(
    private val formationRepository: FormationRepository
) {
    operator fun invoke(row: Int, col: Int) = formationRepository.toggleUnitAt(row, col)
}
