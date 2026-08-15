# 小小戰爭 Little War — Game Design Document (GDD)

> **文件狀態：v0.2 — Milestone 001 進行中**
>
> 目標：做出一款可以真正上架、可長期營運的 Android RPG 策略遊戲。
> 核心玩法：**兵種收集 × 領袖抽卡 × 陣型策略 × 屬性克制 × 自動戰鬥 × RPG 成長**

> **關於本文件**：原始構想稿在傳輸過程中文字編碼損毀（非 UTF-8 純文字），已依原始章節架構與你提供的核心設計重新整理撰寫，內容保留原意並補上 Milestone 001 執行細節與已做的技術決策。

---

## 1. 專案願景

這是一款以「陣型戰術」為核心的 RPG 策略遊戲。

玩家從基礎的小兵開始，逐步收集各種兵種與強化領袖，組建自己的軍隊配方。

玩家真正要思考的不是「單純堆戰力、誰的數字比較高」，而是：

- 我該放什麼兵？
- 前排後排要怎麼配置？
- 要迎擊敵人的什麼屬性？
- 要選擇物理還是魔法？
- 如何利用攻擊距離與攻擊範圍？
- 哪一種陣型最適合這關的敵人？
- 在有限的放置格數中，如何配置最高效率？

長期規劃包含：PvE 關卡、兵種收集、領袖收集、RPG 成長、裝備／技能、抽卡、寶箱、PvP、競技場、活動、賽季 — 但這些全部**不在第一階段範圍內**。

---

## 2. 核心遊戲循環

長期的核心 Loop：

```text
登入 → 領取資源 → 強化兵種／領袖 → 配置陣容
  → 挑戰 PvE → 取得資源（經驗、鑽石、貨幣）
  → 開啟寶箱／抽取兵種 → 獲得更強兵種或領袖
  → 重新配置陣容 → 提升整體強度 → 未來進入 PvP
```

最重要的核心 Loop（第一版只做這一段）：

```text
收集 → 培育 → 配置 → 戰鬥 → 檢視結果 → 再挑戰
```

---

## 3. 目前的開發策略

> **「先做一場真的好玩的戰鬥，再談其他一切。」**

不先做抽卡、商城、40 關卡。第一個版本只驗證一件事：**這場戰鬥好不好玩**。

```text
Prototype（本文件範圍）
  → 驗證戰鬥好玩
  → 小規模 MVP
  → 驗證留存
  → 驗證付費
  → 擴充更多內容
```

若「戰鬥好不好玩」這個核心假設不成立，不會繼續堆內容，而是回頭調整戰鬥系統。

---

## 4. Milestone 001 — Playable Battle Prototype（目前進度）

**這是目前唯一在建置的範圍。**

### 完成條件

- [x] 3 × 5 戰場（3 列深度 × 5 行車道）
- [x] 4 種兵（弓兵／法師／盾兵／槍兵）
- [x] 1 個領袖（提供全軍攻擊力 Buff）
- [x] 敵我雙方陣型（資料驅動，同一份 JSON 定義雙方單位）
- [x] 自動戰鬥（無需玩家操作，能量條驅動的行動順序）
- [x] 物理／魔法傷害
- [x] 攻擊距離、攻擊範圍（含法師的十字範圍 AoE）
- [x] 命中／閃避、暴擊
- [x] 五行屬性克制
- [x] Battle Event（Attack／Damage／Critical／Miss／Death／BattleEnd）
- [x] 勝利／敗北／平手判定
- [x] Android 端排兵佈陣 UI ＋ 簡易戰鬥動畫演出

**明確排除**：抽卡、商城、付費、40 關、PvP、伺服器連線。

### 技術決策（相對於初版構想稿的調整）

1. **Battle Engine 獨立成 `:battle-engine` 模組**（純 Kotlin、無 Android 依賴）。
   `:app` 只依賴它，不重新實作任何戰鬥邏輯 — 對應你的要求「Android 只是把戰鬥結果演出來」。
2. **資料驅動**：所有兵種數值放在 `battle-engine/src/main/resources/game-data/milestone-001.json`，不寫死在 Kotlin 裡（見 [docs/BattleSystem.md](docs/BattleSystem.md)）。
3. **決定性模擬（Deterministic Simulation）**：同一個 seed ＋ 同一份陣型 = 完全相同的戰鬥過程與結果。這是未來 PvP、Replay、Anti-cheat、戰鬥測試的基礎（見第 31 節）。
4. **未來後端語言選擇由 Node.js＋TypeScript 改為 Kotlin＋Ktor**（見第 33 節）— 理由是可以讓伺服器直接重用同一份 `:battle-engine` 模組，徹底避免「Client 一套邏輯、Server 又要重寫一套」導致兩邊算出不同結果的風險。這是本文件與最初構想稿的主要差異，其餘設計維持原構想。

---

## 5. 基礎兵種（Milestone 001 版本）

| 兵種 | 定位 | 傷害型態 | 屬性 | 範圍 |
|---|---|---|---|---|
| 弓兵 | 物理遠程單體 | 物理 | 無 | 5 |
| 法師 | 魔法遠程 AoE（十字） | 魔法 | 火 | 5 |
| 盾兵 | 近戰坦克 | 物理 | 無 | 1 |
| 槍兵 | 近戰輸出 | 物理 | 水 | 1 |
| 小兵 | 近戰基礎填充兵 | 物理 | 無 | 1 |
| 騎兵 | 近戰爆發（高閃避／高暴擊） | 物理 | 木 | 1 |
| 弩兵 | 物理遠程單體（高精準／高爆擊） | 物理 | 光 | 5 |
| 刺客 | 近戰極端爆發（低血量高閃避高暴擊） | 物理 | 暗 | 1 |
| 領袖 | 全軍 Buff（+15% 攻擊） | 物理 | 無 | 2 |

完整數值見 `battle-engine/src/main/resources/game-data/milestone-001.json`。這只是第一版平衡數字，之後會依實際對戰數據調整 — 調整只需要改 JSON，不需要改程式碼。

小兵／騎兵／弩兵／刺客是第一批兵種池擴充，補上五行中原本缺席的木／光／暗三種屬性代表。取名與定位依據原始構想稿（Melee／Ranged／Special／High Tier 分類）挑選，尚未實作的部分（矛兵、重裝兵、劍士、狂戰士、火砲手、投石兵、魔法師〔第二位〕、治療師、將軍、聖騎士、大魔法師、黑暗騎士、元素使）留待後續依實際對戰回饋決定優先順序。

> 已知待確認事項：目前預設陣容（第 8 節）把槍兵放在 row1（中排），但距離公式下 row1 攻擊者的最小可能距離是 2（`depthDistance = 1 + 0 + 1`），而槍兵射程只有 1 —— 這代表中排槍兵在目前預設陣容下永遠打不到任何目標，純粹當血量海綿。這是排陣數值問題，不是本次兵種池擴充的範圍，先記錄在這裡，留給之後的「數值平衡模擬」再處理。

---

## 6. 領袖系統

領袖不只是「比較強的兵」，而是會改變陣容的打法。

Milestone 001 只做一位領袖、一種效果（全軍攻擊力 +15%），驗證「領袖 Buff 影響戰局」這個機制本身是否成立。

未來每位領袖會有獨特 Buff 方向，例如：

- 兵種護甲／生命向：肉盾流
- 屬性傷害向：屬性爆發流
- 治療／減傷向：續航流

**領袖 = 陣容 Build 的核心。**

---

## 7. 屬性克制

五行循環（不做完全對稱剪刀石頭布，避免數值僵化）：

```text
火 → 剋 → 木
木 → 剋 → 水
水 → 剋 → 火

光 ↔ 暗（互剋）
```

- 克制方攻擊：+25% 傷害
- 被克方攻擊：-20% 傷害（0.8 倍，原構想稿未明訂，此為延伸設計以增加屬性博弈深度）
- 無屬性（NONE）：無加成無懲罰

---

## 8. 戰場與陣型

```text
3（深度：前排／中排／後排） × 5（車道）＝ 15 格
```

兩軍隔著固定距離對峙。距離公式：

```text
depthDistance = 我方 row + 敵方 row + 1
laneDistance  = |我方 col − 敵方 col|
distance      = max(depthDistance, laneDistance)
```

也就是說：前排對前排、同車道 → 距離 1（近戰可互打）；躲在後排、同車道的法師打對方前排 → 距離 3。**這正是你在需求中提出的「怎麼組這 3×5 戰場」的具體數學實作。**

Milestone 001 預設陣容原本重現了你原本畫的排陣示意（弓法弓在前排、盾領盾在中排、槍在後排），但實測後發現這個排法會讓脆皮的弓兵／法師直接暴露在前排、被敵方近戰反打，而後排的槍兵（射程 1）在距離公式下永遠打不到任何人（後排對後排距離必定 ≥3）。500 場鏡像對戰模擬下 100% 平手，等於整場戰鬥打不完。

修正後改為前排坦、後排輸出的標準配置：

```text
      盾    槍    盾
      槍    領    槍
      弓          法
```

（中間欄位保留未來「兵」基礎小兵單位的位置）

---

## 9. 戰鬥機制

- **行動順序**：能量條制（ATB）。每 tick 依 `attackSpeed` 累積能量，達到門檻即可行動，門檻不會歸零而是扣除，讓速度快的單位能連續行動。
- **目標選擇**：射程內距離最近的存活敵人；同距離時優先攻擊血量較低者（集火）。
- **命中判定**：`(命中 − 閃避)`，夾限在 5%～95% 之間，永遠有機率落空或必中。
- **暴擊判定**：獨立機率骰，夾限在 0%～75%。
- **傷害公式**：見 [docs/BattleSystem.md](docs/BattleSystem.md)。
- **無法觸及目標時**：單位待機（不做移動）— 這是刻意設計，逼玩家用陣型解決射程問題，而不是靠位移繞過陣型限制。
- **戰鬥上限**：300 tick 內未分勝負則判定平手（安全閥，避免無限迴圈）。

---

## 10. Battle Event（Server 為最終可信來源的基礎）

Battle Engine 不直接操作畫面，只吐出事件序列：

```text
BattleStartEvent
AttackEvent
DamageEvent
CriticalEvent
MissEvent
DeathEvent
BattleEndEvent
```

Android（未來也包含 Server 端的其他消費者）根據事件序列播放演出。這對應你提出的「Android 只是把戰鬥結果演出來」。

---

## 11. Replay（架構已預留，尚未實作儲存）

只要滿足：

```text
同一個 Random Seed ＋ 同一組初始 Formation ＋ 同一份兵種資料
  = 完全相同的一場戰鬥
```

未來只需要儲存 `seed ＋ 雙方 FormationData ＋ 使用的 game-data 版本`，即可在任何時候重播出一模一樣的戰鬥。`BattleResult.roster`（起始陣容快照）已經是為了支援這件事而設計的欄位。

---

## 12. Android 架構

```text
Kotlin + Jetpack Compose + Clean Architecture + MVI + Hilt（依你的要求採用，非最初構想稿版本）
```

```text
:app                      — Presentation／Domain／Data 三層，只消費 :battle-engine 的輸出
:battle-engine             — 純 Kotlin，無 Android 依賴，可被 Server 直接重用
```

`:app` 內部結構：

```text
ui/<feature>/
 ├── <Feature>Contract.kt   — State / Event / Effect（MVI 三件套）
 ├── <Feature>ViewModel.kt  — @HiltViewModel，State 用 StateFlow，一次性導航用 Channel<Effect>
 └── <Feature>Screen.kt     — 純顯示，把使用者互動包成 Event 送給 ViewModel

domain/
 ├── repository/     — interface，不依賴 Android 框架
 └── usecase/        — 每個操作一個 use case，ViewModel 永遠不直接碰 data 層實作

data/repository/     — repository 實作，@Singleton，透過 di/AppModule.kt 用 Hilt @Binds 綁定
```

技術選型：Kotlin、Jetpack Compose、Navigation Compose、Coroutines、kotlinx.serialization（讀取 game-data JSON）、Hilt（DI）。詳細規範與建置環境注意事項見 [CLAUDE.md](CLAUDE.md)。

---

## 13. 後端規劃（Milestone 001 之後）

原構想稿建議 Node.js + TypeScript + PostgreSQL。考量到「Server 才是最終可信來源」這個目標，**改為 Kotlin + Ktor**，理由：

- 可以直接依賴同一份 `:battle-engine` 模組，戰鬥邏輯只寫一次
- 避免 Client（Kotlin）與 Server（TypeScript）各自實作一套戰鬥公式，長期產生數值漂移或行為不一致的風險
- 資料庫仍使用 PostgreSQL（Ktor + Exposed 或純 JDBC）

```text
Android
  ↓ HTTPS REST API
Backend (Kotlin + Ktor)
  ↓
PostgreSQL
```

**此階段尚未實作**，`backend/` 目前只有規劃文件，待 Milestone 001 驗證「戰鬥好玩」後才會動工，對齊原構想稿 Step 4 的順序。

---

## 14. 商業模式方向（已與你確認：訂閱制優先）

長期營運不會走「不買就打不贏」的付費模式。優先順序：

1. **訂閱制為主**：提供便利性與加速（例如：加速恢復、每日額外寶箱、便利功能），不直接販售戰力。
2. 次要：鑽石與道具的常規販售。
3. 抽卡機制保留作為長期內容擴充手段，但不做成早期主要營收來源，且抽卡機率必須由 Server 端執行、可稽核。

這個決策目前只影響文件與後端 Schema 的預留欄位，尚未進入實作階段。

---

## 15. 雲端與部署規劃（已與你確認：先選最省錢方案）

Milestone 001 完全不需要雲端。之後端規劃啟動時的建議路線：

- **運算**：Cloud Run（或 Fly.io）— 用量計費，個人小專案初期成本趨近於零
- **資料庫**：Supabase 或 Neon 的 PostgreSQL 免費層
- **本機開發**：Docker Compose 起本地 PostgreSQL，銜接後端開發
- **App 端監測**：Firebase Crashlytics + Analytics（免費層足夠支撐到有明確付費資料前）

實際串接雲端帳號、執行部署指令需要你提供／登入對應帳號權限，目前只先準備好 Docker 化與可部署的專案結構。

---

## 16. 下一步 Roadmap

```text
Step 1（已完成）— Core Battle Engine + 基礎 UI 演出（Milestone 001）
Step 2 — 用實機／模擬器實際遊玩，收集「好不好玩」的主觀回饋，調整戰鬥數值與節奏
Step 3 — 擴充兵種池與敵人關卡（PvE 雛形，仍是本機資料，不上雲）
Step 4 — Backend（Kotlin + Ktor）＋ 帳號系統 ＋ Server 端戰鬥驗證
Step 5 — 經濟系統／抽卡／寶箱（Server 端執行，Client 不可信原則）
Step 6 — Soft Launch（Analytics、Crashlytics、Google Play Billing）
Step 7 — 正式上架
```

---

## 17. 成功／失敗判斷標準

第一階段（Milestone 001）成功的判斷不是「功能有沒有做完」，而是：

> **玩過一場後，會不會想再玩一次、想試試看換一種排陣？**

如果答案是否定的，下一步不是繼續加內容，而是回頭調整戰鬥手感與數值。
