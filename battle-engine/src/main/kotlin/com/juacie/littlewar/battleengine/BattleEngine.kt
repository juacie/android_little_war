package com.juacie.littlewar.battleengine

import kotlin.random.Random

/**
 * The authoritative battle simulator. Same seed + same formations = same event log, always.
 * This determinism is what will let a future server compute results the Android client cannot
 * be trusted to compute itself, and what makes Replay possible later.
 */
object BattleEngine {
    private const val ENERGY_THRESHOLD = 100.0
    private const val TICK_ENERGY_SCALE = 20.0
    private const val MAX_TICKS = 300

    fun simulate(playerFormation: Formation, enemyFormation: Formation, gameData: GameData, seed: Long): BattleResult {
        val random = Random(seed)
        val playerUnits = playerFormation.toUnitInstances(gameData)
        val enemyUnits = enemyFormation.toUnitInstances(gameData)
        val allUnits = playerUnits + enemyUnits

        val roster = allUnits.map {
            UnitSnapshot(it.id, it.definition.name, it.side, it.position, it.definition.hp, it.definition.isLeader)
        }

        val events = mutableListOf<BattleEvent>()
        events += BattleStartEvent(0, playerUnits.map { it.id }, enemyUnits.map { it.id })

        var tick = 0
        while (tick < MAX_TICKS && playerUnits.any { it.isAlive } && enemyUnits.any { it.isAlive }) {
            tick++
            allUnits.filter { it.isAlive }.forEach { it.energy += it.definition.attackSpeed * TICK_ENERGY_SCALE }

            val actors = allUnits
                .filter { it.isAlive && it.energy >= ENERGY_THRESHOLD }
                .sortedWith(compareByDescending<UnitInstance> { it.energy }.thenBy { it.id })

            for (actor in actors) {
                if (!actor.isAlive) continue
                actor.energy -= ENERGY_THRESHOLD

                val enemyPool = if (actor.side == BattleSide.PLAYER) enemyUnits else playerUnits
                val primaryTarget = Targeting.findPrimaryTarget(actor, enemyPool) ?: continue

                events += AttackEvent(tick, actor.id, primaryTarget.id)
                val targets = Targeting.collectAoeTargets(primaryTarget, actor.definition.aoeShape, enemyPool)
                for (target in targets) {
                    if (target.isAlive) resolveHit(actor, target, random, tick, events)
                }
            }
        }

        val playerAlive = playerUnits.any { it.isAlive }
        val enemyAlive = enemyUnits.any { it.isAlive }
        val outcome = when {
            playerAlive && !enemyAlive -> BattleOutcome.PLAYER_VICTORY
            enemyAlive && !playerAlive -> BattleOutcome.ENEMY_VICTORY
            else -> BattleOutcome.DRAW
        }
        events += BattleEndEvent(tick, outcome)

        return BattleResult(
            outcome = outcome,
            totalTicks = tick,
            roster = roster,
            events = events,
            survivingPlayerUnitIds = playerUnits.filter { it.isAlive }.map { it.id },
            survivingEnemyUnitIds = enemyUnits.filter { it.isAlive }.map { it.id }
        )
    }

    private fun resolveHit(
        actor: UnitInstance,
        target: UnitInstance,
        random: Random,
        tick: Int,
        events: MutableList<BattleEvent>
    ) {
        val def = actor.definition
        val (attack, defense, critChance) = when (def.damageType) {
            DamageType.PHYSICAL -> Triple(actor.effectivePhysicalAttack, target.definition.armor, def.physicalCritChance)
            DamageType.MAGICAL -> Triple(actor.effectiveMagicAttack, target.definition.magicDefense, def.magicCritChance)
        }

        if (!CombatMath.rollHit(random, def.accuracy, target.definition.evasion)) {
            events += MissEvent(tick, actor.id, target.id)
            return
        }

        val isCritical = CombatMath.rollCritical(random, critChance)
        val elementMultiplier = def.element.multiplierAgainst(target.definition.element)
        val damage = CombatMath.computeDamage(attack, defense, elementMultiplier, isCritical, def.critMultiplier)

        target.currentHp = (target.currentHp - damage).coerceAtLeast(0)

        if (isCritical) events += CriticalEvent(tick, actor.id, target.id)
        events += DamageEvent(tick, actor.id, target.id, damage, target.currentHp)
        if (!target.isAlive) events += DeathEvent(tick, target.id)
    }
}
