# Roadmap — Milestone 總覽

這份清單原始出處是專案最初的規劃母稿（使用者提供的 Master Instruction），repo 內先前沒有留存，只有 [Game_Project_GDD.md](../Game_Project_GDD.md) 裡籠統的 7 步 roadmap。這裡補上完整編號，方便之後每次開新 Milestone 時查對應範圍與順序。

**這是方向性清單，不是承諾的排程。** 目前唯一在執行的是 M001（見下方「目前狀態」），之後每個 Milestone 開工前都要先確認範圍、不要一次跳做多個未被要求的項目（見 [CLAUDE.md](../CLAUDE.md)「現況」一節）。

## 目前狀態

**M001 — Playable Battle Prototype：核心玩法已完成並可玩**（3×5 戰場、4→8 種基礎兵種＋1 領袖、自動戰鬥、命中／爆擊／屬性克制、勝負判定、Android 排兵佈陣＋戰鬥演出 UI）。詳細完成條件見 [Game_Project_GDD.md](../Game_Project_GDD.md) 第 4 節；戰鬥公式見 [BattleSystem.md](BattleSystem.md)。

GDD 第 16 節 Step 3（兵種池擴充＋敵人關卡雛形）已完成雛形：8 種基礎兵＋1 領袖、3 個可選關卡（Home → 選關 → 排陣 → 戰鬥 → 結果）。這是提前做的一小塊 M010／M014 內容，仍是純本機資料，不涉及星等／獎勵／伺服器。

Step 2（實機／模擬器實際遊玩，收集「好不好玩」的主觀回饋）還沒做——下一步不是自動往 M002 推進，而是先完成這個playtest，確認核心戰鬥手感站得住腳之後，再決定要往下列哪個方向擴充。

## 完整 Milestone 清單

```text
Phase 0 — Foundation
  M000 Project Initialization

Phase 1 — Core Battle
  M001 Playable Battle Prototype
  M002 Battle Engine
  M003 Battle Determinism
  M004 Formation System

Phase 2 — Combat Depth
  M005 Damage Formula
  M006 Element System
  M007 Attack Range & Targeting
  M008 Skill System
  M009 Battle AI

Phase 3 — RPG
  M010 Unit System
  M011 Leader System
  M012 Upgrade System
  M013 Inventory

Phase 4 — PvE
  M014 Stage System
  M015 First Chapter
  M016 Star System
  M017 Stamina

Phase 5 — Economy
  M018 Currency
  M019 Chest
  M020 Keys
  M021 Gacha

Phase 6 — Android Product
  M022 Home
  M023 Formation UI
  M024 Battle UI
  M025 Unit UI
  M026 Leader UI
  M027 Gacha UI
  M028 Chest UI
  M029 Shop UI
  M030 Profile UI

Phase 7 — Backend
  M031 Authentication
  M032 Player Profile
  M033 Inventory
  M034 Stage API
  M035 Battle API
  M036 Reward API
  M037 Gacha API
  M038 Purchase Verification
  M039 Anti-cheat

Phase 8 — Monetization
  M040 Google Play Billing
  M041 Diamond Package
  M042 Subscription
  M043 Battle Pass
  M044 First Purchase
  M045 Remote Config

Phase 9 — PvP
  M046 PvP Battle Engine
  M047 Defense Formation
  M048 Matchmaking
  M049 PvP Result
  M050 Ranking
  M051 Season

Phase 10 — Live Service
  M052 Guild
  M053 Guild War
  M054 Daily Quest
  M055 Weekly Quest
  M056 Achievement
  M057 Event
  M058 Limited Leader
  M059 Limited Unit
  M060 New Chapter
  M061 PvP Season
  M062 Ranking Reward
```

M062 之後可以持續新增，不預設終點。實際開發不必嚴格照號碼順序——例如目前在 M001 範圍內先做「擴充兵種池」，本質上是提前做一小塊 M010（Unit System）的內容，這是合理的漸進式擴充，只要不整段跳去做 Gacha／PvP／Backend 這種明顯超出目前範圍的 Phase 即可。

## Milestone 文件模板

之後開新 Milestone 文件時用這個結構：

```markdown
# Mxxx — Name
## Goal
## Why
## Scope
### Must Have
### Should Have
### Won't Have
## Technical Requirements
## Dependencies
## Acceptance Criteria
## Tests
## Documentation
## Status
```
