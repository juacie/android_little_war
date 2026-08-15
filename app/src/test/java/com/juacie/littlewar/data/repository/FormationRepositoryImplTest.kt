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

    @Test
    fun toggleUnitAt_rejectsPlacement_onceFormationCapReached() {
        // milestone-001.json 裡 leader.formationCap = 1，且初始陣容已經放了 1 位領袖 (row 1, col 2)。
        repository.selectUnit("leader")

        repository.toggleUnitAt(row = 1, col = 0) // 空格，改放第 2 位領袖應該被擋下

        assertNull(repository.playerSlots.value[5])
        assertEquals(1, repository.playerSlots.value.count { it == "leader" })
    }

    @Test
    fun toggleUnitAt_allowsPlacement_upToFormationCap() {
        // cavalry.formationCap = 3，初始陣容裡沒有騎兵，從 0 開始放到剛好第 3 隻應該成功。
        repository.selectUnit("cavalry")

        repository.toggleUnitAt(row = 1, col = 0)
        repository.toggleUnitAt(row = 1, col = 1)
        repository.toggleUnitAt(row = 1, col = 3)

        assertEquals(3, repository.playerSlots.value.count { it == "cavalry" })
    }

    @Test
    fun toggleUnitAt_rejectsFourthPlacement_pastFormationCap() {
        // 承上，第 4 隻騎兵應該被擋下，格子維持空著。
        repository.selectUnit("cavalry")
        repository.toggleUnitAt(row = 1, col = 0)
        repository.toggleUnitAt(row = 1, col = 1)
        repository.toggleUnitAt(row = 1, col = 3)

        repository.toggleUnitAt(row = 1, col = 4)

        assertEquals(3, repository.playerSlots.value.count { it == "cavalry" })
        assertNull(repository.playerSlots.value[9])
    }

    @Test
    fun toggleUnitAt_removingAndReplacing_stillRespectsCap() {
        // 移除已放置的領袖之後，重新放置一次領袖應該仍然成功（count 先扣回 0，不是永久卡死）。
        repository.selectUnit("leader")
        repository.toggleUnitAt(row = 1, col = 2) // 移除初始陣容裡的領袖

        assertEquals(0, repository.playerSlots.value.count { it == "leader" })

        repository.toggleUnitAt(row = 1, col = 2) // 重新放回去

        assertEquals(1, repository.playerSlots.value.count { it == "leader" })
    }
}
