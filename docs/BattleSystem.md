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

A unit can only target enemies where `distance <= attackRange`. If nothing is in range, the squad
moves instead — see "Movement" below.

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

## Movement (Phase 2 of the 方陣制 redesign)

When a squad's turn comes up (energy ≥ 100, same gate as an attack) and `Targeting.findPrimaryTarget`
finds nobody in range, the squad moves one cell instead of idling — consuming the same 100 energy an
attack would. This is the macro-grid slice of the movement idea in `docs/SquadBattleConcept.md`; the
full 5×5 sub-grid movement described there needs a squad-growth/leveling system that doesn't exist
yet (`squadCapacity` is still a fixed number, not a level curve), so this phase moves squads on the
existing 3×5 macro grid, one cell per action, same for every unit type (no per-unit move speed yet).

**Target:** `Targeting.findNearestEnemy` — nearest alive enemy squad by `distance`, ignoring range,
same tie-break as `findPrimaryTarget` (lowest HP, then id) so the choice is deterministic.

**Step:** `Movement.planStep` shrinks whichever axis is currently the bottleneck in
`distance = max(depthDistance, laneDistance)` — move row toward 0 (the frontline) by one if
`depthDistance >= laneDistance` and the squad isn't already at row 0, otherwise step one column toward
the target's column. Ties prefer reducing depth first. Returns `null` (no move) once the squad can't
get any closer this way — e.g. already at row 0 with its column aligned to the target's, but the
target's own row still keeps `depthDistance` above the squad's `attackRange` (structurally can't be
closed by this squad's movement alone).

**Collision:** a squad won't move onto a cell already occupied by another *living* squad on its own
side. If `planStep`'s preferred axis (the depth/lane bottleneck) is blocked this way, it falls back to
the other axis (a side-step) instead of giving up outright; only when *both* candidate cells are
occupied does the squad skip moving that tick and re-evaluate next time it acts. This was added
2026-08-15 after playtesting the original "just don't move" version — with squads sharing a column,
a same-lane back-row squad would sit frozen behind its own front-row squad until that front squad
died and freed the cell, which read as turn-based ("front row has to die before anyone else moves")
rather than every squad independently pathing toward a target. No displacement/pathfinding beyond
this one-step fallback — still deterministic, still no RNG.

No RNG is involved in movement — `planStep` is a pure function of both squads' positions — so this
doesn't touch determinism.

## Leader aura

If a squad's own unit type has `isLeader = true`, **only that squad's own**
`physicalAttack`/`magicAttack` is multiplied by `1 + leaderAttackBuffPercent / 100`, once, at
formation-build time (not per-tick). Before the 方陣制 Phase 1 redesign this buff applied to every
unit on the whole side; it's now scoped down to the leader's own squad only. Range-limited auras
that buff *other* squads are a possible future refinement, not this phase's scope.

## Known simplifications (intentional, documented so they aren't mistaken for bugs)

- Movement is macro-grid only (one 3×5 cell per action, same speed for every unit type) — see
  "Movement" above. The 5×5 sub-grid movement from `docs/SquadBattleConcept.md`, with per-unit-type
  move speed, needs a squad-growth/leveling system that doesn't exist yet.
- Leader death does not end the battle early; the leader's squad is just another squad.
- Only `SINGLE` and `PLUS` AoE shapes exist so far (line/full-board AoE are future work). AoE is
  still resolved squad-vs-squad on the 3×5 macro grid — it does not reach into a squad's internal
  5×5 sub-grid.
- Backline-vs-backline (`row=2` on both sides) starts at distance 5 — the maximum possible on a
  3-row board — but movement now lets either squad walk to row 0, shrinking `depthDistance` down to
  a minimum of 3 (`0+2+1`). A unit meant to still be dangerous from the backline (e.g. archer/mage)
  with `attackRange < 3` still risks a 300-tick draw if both squads happen to keep colliding with
  allies on the way forward (see "Collision" under Movement above) instead of ever actually closing
  the gap. `archer`/`mage` are kept at `attackRange = 5` — don't lower it without re-running a
  mirror-match simulation to check the draw rate.
- Hero units and the off-board 全軍領袖 (army-wide leader, no HP, not on the board) are not
  implemented — see `docs/SquadBattleConcept.md` and `docs/ROADMAP.md`.
