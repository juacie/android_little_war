package com.juacie.littlewar.data.repository

import com.juacie.littlewar.battleengine.GameDataLoader
import com.juacie.littlewar.domain.repository.GameDataRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class FormationRepositoryImplTest {

    private lateinit var repository: FormationRepositoryImpl

    @Before
    fun setUp() {
        val gameDataRepository = object : GameDataRepository {
            override fun getGameData() = GameDataLoader.loadMilestone001()
        }
        repository = FormationRepositoryImpl(gameDataRepository)
    }

    @Test
    fun moveUnitAt_movesUnitIntoEmptyCell() {
        val unitId = repository.playerSlots.value[0]
        checkNotNull(unitId)

        repository.moveUnitAt(fromRow = 0, fromCol = 0, toRow = 2, toCol = 4)

        assertEquals(unitId, repository.playerSlots.value[14])
        assertNull(repository.playerSlots.value[0])
    }

    @Test
    fun moveUnitAt_swapsTwoOccupiedCells() {
        // milestone-001.json 的初始陣型 (0,0)=shield、(0,1)=spearman，型別不同，適合驗證交換
        val fromUnit = repository.playerSlots.value[0]
        val toUnit = repository.playerSlots.value[1]
        checkNotNull(fromUnit)
        checkNotNull(toUnit)

        repository.moveUnitAt(fromRow = 0, fromCol = 0, toRow = 0, toCol = 1)

        assertEquals(toUnit, repository.playerSlots.value[0])
        assertEquals(fromUnit, repository.playerSlots.value[1])
    }

    @Test
    fun moveUnitAt_doesNothing_whenSourceAndTargetAreSameCell() {
        val before = repository.playerSlots.value
        repository.moveUnitAt(fromRow = 0, fromCol = 0, toRow = 0, toCol = 0)
        assertEquals(before, repository.playerSlots.value)
    }
}
