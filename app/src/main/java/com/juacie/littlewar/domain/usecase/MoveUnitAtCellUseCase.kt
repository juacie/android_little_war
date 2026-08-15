package com.juacie.littlewar.domain.usecase

import com.juacie.littlewar.domain.repository.FormationRepository
import javax.inject.Inject

class MoveUnitAtCellUseCase @Inject constructor(
    private val formationRepository: FormationRepository
) {
    operator fun invoke(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int) =
        formationRepository.moveUnitAt(fromRow, fromCol, toRow, toCol)
}
