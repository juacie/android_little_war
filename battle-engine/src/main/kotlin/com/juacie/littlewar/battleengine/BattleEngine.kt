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
        val playerUnits = playerFormation.toSquads(gameData)
        val enemyUnits = enemyFormation.toSquads(gameData)
        val allUnits = playerUnits + enemyUnits

        val roster = allUnits.map {
            SquadSnapshot(it.id, it.definition.name, it.side, it.position, it.maxHp, it.definition.isLeader)
        }

        val events = mutableListOf<BattleEvent>()
        events += BattleStartEvent(0, playerUnits.map { it.id }, enemyUnits.map { it.id })

        var tick = 0
        while (tick < MAX_TICKS && playerUnits.any { it.isAlive } && enemyUnits.any { it.isAlive }) {
            tick++
            allUnits.filter { it.isAlive }.forEach { it.energy += it.definition.attackSpeed * TICK_ENERGY_SCALE }

            // Ties (both sides reaching threshold the same tick, common when attackSpeed matches
            // across the mirror) must NOT be broken by unit id: "ENEMY-..." sorts before
            // "PLAYER-..." alphabetically, which would silently give the enemy side first strike
            // on every tied tick. Shuffle ties with the battle's own seeded Random instead, so
            // the result stays deterministic per-seed without being side-biased.
            val actors = allUnits
                .filter { it.isAlive && it.energy >= ENERGY_THRESHOLD }
                .groupBy { it.energy }
                .entries
                .sortedByDescending { it.key }
                .flatMap { (_, tiedActors) -> if (tiedActors.size > 1) tiedActors.shuffled(random) else tiedActors }

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
        actor: Squad,
        target: Squad,
        random: Random,
        tick: Int,
        events: MutableList<BattleEvent>
    ) {
        val def = actor.definition
        // 比例戰力衰減：只打折攻擊方自己的輸出（攻擊力／命中率），防禦方的 armor/magicDefense/
        // evasion 不受影響。方陣還活著時 hpFraction > 0，這裡不會是 0（死亡方陣不會被選為 actor）。
        val decay = actor.hpFraction
        val (baseAttack, defense, critChance) = when (def.damageType) {
            DamageType.PHYSICAL -> Triple(actor.effectivePhysicalAttack, target.definition.armor, def.physicalCritChance)
            DamageType.MAGICAL -> Triple(actor.effectiveMagicAttack, target.definition.magicDefense, def.magicCritChance)
        }
        val attack = (baseAttack * decay).toInt()
        val accuracy = def.accuracy * decay
        val evasion = target.definition.evasion + target.retreatEvasionBonus

        if (!CombatMath.rollHit(random, accuracy, evasion)) {
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
