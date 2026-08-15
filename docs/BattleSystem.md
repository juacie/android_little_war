# Battle System — Formulas (Milestone 001)

This documents exactly what `:battle-engine` implements, so balance changes can be made by editing
`battle-engine/src/main/resources/game-data/milestone-001.json` without needing to read the Kotlin
source. If this doc and the code ever disagree, the code (specifically `CombatMath.kt`,
`Targeting.kt`, `BattleEngine.kt`) is the source of truth — please update this file to match.

## Grid & distance

The battlefield is two 3×5 grids facing each other across a fixed 1-cell gap. `row` is depth
(0 = frontline, 2 = backline); `col` is lane (0..4).

```
depthDistance = attackerRow + defenderRow + 1
laneDistance  = |attackerCol - defenderCol|
distance      = max(depthDistance, laneDistance)
```

A unit can only target enemies where `distance <= attackRange`. There is no movement in Milestone
001 — a unit that can't reach anyone in range simply idles that turn. This is intentional: range
and formation composition are meant to be the puzzle, not something you can walk around.

## Turn order (ATB / energy gauge)

Every tick, every alive unit gains `attackSpeed * 20` energy. Any unit at ≥100 energy acts (highest
energy first), spends 100 energy (not reset to 0 — the remainder carries over), and picks a target.
Ties on energy (very common — mirrored units share `attackSpeed`, so both sides often cross the
threshold on the same tick) are broken by shuffling the tied group with the battle's own seeded
`Random`, **not** by unit id: an id-based tie-break sorts `"ENEMY-..."` before `"PLAYER-..."`
alphabetically, which silently gave the enemy side first strike on every tied tick and, when that
first strike killed the tied player unit, skipped that unit's action entirely for the tick. That bug
showed up as an enemy win rate of ~60% in an otherwise perfectly mirrored formation. Simulation runs
up to 300 ticks; if neither side is wiped by then, the battle is a **draw**.

## Targeting

- Primary target: nearest alive enemy in range; ties broken by lowest current HP (focus fire), then
  by unit id.
- AoE (`aoeShape = PLUS`): primary target plus the up/down/left/right neighbours on the *defender's*
  grid that are alive. `SINGLE` only ever hits the primary target.

## Hit and crit rolls

```
hitChance  = clamp(accuracy - evasion, 5, 95)
critChance = clamp(physicalCritChance or magicCritChance, 0, 75)
```

Both are independent rolls against `Random(seed)`, consumed in a fixed order so the same seed always
produces the same sequence of hits/misses/crits.

## Damage

```
mitigation = 100 / (100 + defense)      # armor for physical, magicDefense for magical
critFactor = critMultiplier if crit else 1.0
damage     = floor(attack * mitigation * elementMultiplier * critFactor), minimum 1
```

`attack`/`defense` are chosen by `damageType` (physical → physicalAttack/armor, magical →
magicAttack/magicDefense). `attack` already includes the leader's aura multiplier if one is present
on that side (see below).

## Elemental triangle

```
FIRE  beats WOOD
WOOD  beats WATER
WATER beats FIRE
LIGHT beats DARK
DARK  beats LIGHT
```

Attacker's element beats defender's element → ×1.25. Defender's element beats attacker's → ×0.8.
Anything else (including `NONE` on either side) → ×1.0.

## Leader aura

If a formation includes a unit with `isLeader = true`, every unit's `physicalAttack` and
`magicAttack` is multiplied by `1 + leaderAttackBuffPercent / 100` once, at formation-build time
(not per-tick, not range-limited — that's a possible v2 refinement, not Milestone 001 scope).

## Known simplifications (intentional, documented so they aren't mistaken for bugs)

- No movement — see "Grid & distance" above.
- 1 formation slot = 1 combatant (the GDD's "N people per tile" squad-count idea is deferred).
- Leader death does not end the battle early; the leader is just another combatant.
- Only `SINGLE` and `PLUS` AoE shapes exist so far (line/full-board AoE are future work).
- Backline-vs-backline (`row=2` on both sides) is always distance 5 — the maximum possible on a
  3-row board. If a unit meant to still be dangerous from the backline (e.g. archer/mage) has
  `attackRange < 5`, two such units surviving on opposite backlines can never reach each other and
  the battle stalls out to the 300-tick draw cap. `archer`/`mage` are set to `attackRange = 5` for
  exactly this reason — don't lower it below 5 without re-running a mirror-match simulation to
  check the draw rate.
