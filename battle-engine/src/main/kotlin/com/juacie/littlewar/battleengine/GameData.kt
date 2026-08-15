package com.juacie.littlewar.battleengine

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class FormationSlotData(val unitId: String, val row: Int, val col: Int)

@Serializable
data class FormationData(val slots: List<FormationSlotData>)

/** One selectable PvE stage: a name for the stage-select UI plus the enemy formation it fights. */
@Serializable
data class StageData(val id: String, val name: String, val enemyFormation: FormationData)

@Serializable
data class GameData(
    val units: List<UnitDefinition>,
    val playerFormation: FormationData,
    val stages: List<StageData>
) {
    fun unitById(id: String): UnitDefinition =
        units.firstOrNull { it.id == id } ?: error("Unknown unit definition id: $id")

    fun stageById(id: String): StageData =
        stages.firstOrNull { it.id == id } ?: error("Unknown stage id: $id")
}

/** Loads data-driven game content from classpath JSON. Server and client read the same files. */
object GameDataLoader {
    private val json = Json { ignoreUnknownKeys = true }

    fun loadMilestone001(): GameData {
        val stream = requireNotNull(
            GameDataLoader::class.java.getResourceAsStream("/game-data/milestone-001.json")
        ) { "game-data/milestone-001.json not found on classpath" }
        return stream.use { json.decodeFromString(GameData.serializer(), it.readBytes().decodeToString()) }
    }
}
