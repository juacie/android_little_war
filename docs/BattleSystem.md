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

## Squad model (Phase 1 of the 方陣制 redesign)

As of the 方陣制 Phase 1 redesign (see `docs/SquadBattleConcept.md`), **1 formation slot = 1 squad**,
not 1 combatant. A squad's HP is pooled: `maxHp = squadCapacity * unitHp`, and the squad is only
removed from the battle once its pooled HP reaches 0. `squadCapacity` is a fixed, data-driven number
per unit type (`milestone-001.json`) — there's no leveling/growth system yet, so it doesn't scale
with anything at runtime this phase. The exact capacity numbers are initial guesses, not
playtest-tuned values.

A squad's combat *output* also scales with `squadCapacity` (representing the whole squad attacking
together, not just one member) — see "Squad attack output" below. Movement, hero units, and the
off-board 全軍領袖 (army-wide leader with no HP, not on the board) redesign are **not** part of this
phase; they're separate future milestones tracked in `docs/ROADMAP.md`.

### Squad attack output

```
squadAttackMultiplier  = squadCapacity * leaderMultiplier   # leaderMultiplier: see "Leader aura" below
effectivePhysicalAttack = physicalAttack * squadAttackMultiplier   # baked in once, at formation-build time
effectiveMagicAttack    = magicAttack * squadAttackMultiplier
```

### Proportional power decay

At the moment of resolving a hit, the *attacker's own* `effectivePhysicalAttack`/`effectiveMagicAttack`
and `accuracy` are scaled down by the attacker's live HP fraction — this can't be baked in at
formation-build time like the multiplier above, because it changes every time the squad takes damage:

```
hpFraction = currentHp / maxHp
attack     = effectiveAttack * hpFraction
accuracy   = accuracy * hpFraction
```

The defender's `armor`/`magicDefense`/`evasion` are **not** affected by the defender's own
`hpFraction` — decay only ever weakens the squad taking the losses, on offense.

### Retreat evasion bonus (細格參與戰鬥計算)

A squad at ≤50% HP gets a flat `+10` evasion bonus (`Squad.retreatEvasionBonus`) — the idea being
that a battered squad's surviving members have huddled toward the back of their own 5×5 sub-grid and
are harder to pin down. This is deliberately implemented as an **evasion bonus, not a distance/range
penalty**: there's no movement yet, so if a weakened squad's retreat pushed it out of an attacker's
fixed `attackRange`, a squad that just crossed the 50% threshold could permanently strand itself just
out of reach of the only attacker in range, stalling the fight to a 300-tick draw that should have
been a decisive win. (This is exactly how an earlier distance-penalty version of this mechanic broke
`BattleEngineTest.simulate_massiveNumericalAdvantage_winsDecisively`.) An evasion bonus can never
remove a target from range, only make it harder to hit, so this failure mode isn't possible.

## Leader aura

If a squad's own unit type has `isLeader = true`, **only that squad's own**
`physicalAttack`/`magicAttack` is multiplied by `1 + leaderAttackBuffPercent / 100`, once, at
formation-build time (not per-tick). Before the 方陣制 Phase 1 redesign this buff applied to every
unit on the whole side; it's now scoped down to the leader's own squad only. Range-limited auras
that buff *other* squads are a possible future refinement, not this phase's scope.

## Known simplifications (intentional, documented so they aren't mistaken for bugs)

- No movement — see "Grid & distance" above. Squads don't reposition mid-battle; the retreat evasion
  bonus above is the only way a squad's internal state affects combat without movement.
- Leader death does not end the battle early; the leader's squad is just another squad.
- Only `SINGLE` and `PLUS` AoE shapes exist so far (line/full-board AoE are future work). AoE is
  still resolved squad-vs-squad on the 3×5 macro grid — it does not reach into a squad's internal
  5×5 sub-grid.
- Backline-vs-backline (`row=2` on both sides) is always distance 5 — the maximum possible on a
  3-row board. If a unit meant to still be dangerous from the backline (e.g. archer/mage) has
  `attackRange < 5`, two such units surviving on opposite backlines can never reach each other and
  the battle stalls out to the 300-tick draw cap. `archer`/`mage` are set to `attackRange = 5` for
  exactly this reason — don't lower it below 5 without re-running a mirror-match simulation to
  check the draw rate.
- Hero units and the off-board 全軍領袖 (army-wide leader, no HP, not on the board) are not
  implemented — see `docs/SquadBattleConcept.md` and `docs/ROADMAP.md`.
