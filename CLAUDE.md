# CLAUDE.md — 專案注意事項

給未來的自己（或未來的 Claude）快速抓回專案脈絡用。詳細設計理念看 [Game_Project_GDD.md](Game_Project_GDD.md)，戰鬥公式看 [docs/BattleSystem.md](docs/BattleSystem.md)。

## 現況

單人開發中的 Android RPG 策略遊戲，目前唯一在做的範圍是 **Milestone 001 — Playable Battle Prototype**（純本機、無後端、無帳號系統）。不要在這之前就動手做抽卡／商城／PvP／伺服器串接，除非使用者明確要求擴大範圍。

## 專案結構

```text
android_little_war/
├── battle-engine/     — 純 Kotlin 模組，無 Android 依賴。所有戰鬥數值計算都在這裡，
│                         未來要重用在 Server 端也是靠這個模組完全獨立這件事。
│                         改戰鬥公式只改這裡，不要把邏輯外洩到 :app。
├── app/                — Android 端，Clean Architecture 三層 + MVI + Hilt DI（見下）
├── backend/            — 目前只有規劃文件，還沒動工（見 backend/README.md）
├── docs/BattleSystem.md — 戰鬥公式的權威文件，程式碼跟這份文件如果對不上，以程式碼為準，
│                          但要記得回來更新文件
└── Game_Project_GDD.md — 完整企劃書
```

## Android 架構（app/）

**Clean Architecture（Presentation / Domain / Data）+ MVI + Hilt DI**，這是使用者明確要求的架構，之後新增功能要延續同一套模式，不要退回原本簡單的單一 ViewModel 寫法。

```text
ui/<feature>/
  ├── <Feature>Contract.kt   — State / Event / Effect 三個型別，UI 只能透過這個介面跟 ViewModel 溝通
  ├── <Feature>ViewModel.kt  — @HiltViewModel，收 Event、發 State（StateFlow）與一次性 Effect（Channel）
  └── <Feature>Screen.kt     — 純顯示，讀 state.collectAsStateWithLifecycle()，把互動包成 Event 送出

domain/
  ├── repository/   — interface，不依賴任何 Android/Hilt 之外的東西
  └── usecase/      — 一個 usecase 對應一個明確的操作，thin wrapper 也沒關係，
                       重點是 ViewModel 永遠不直接碰 data 層的實作

data/repository/    — repository 介面的實作，@Singleton，透過 di/AppModule.kt 用 @Binds 綁定
```

新增一個畫面／功能時的標準流程：先在 `domain/repository` 定義需要的介面方法 → `data/repository` 實作 → 需要的話包一個 `domain/usecase` → 在 `ui/<feature>/` 建 Contract + ViewModel + Screen → 到 `ui/navigation/LittleWarNavHost.kt` 掛路由。

`FormationRepository` 和 `BattleRepository` 是 `@Singleton`，作為 Formation → Battle → Result 三個畫面之間共享狀態的唯一來源，不要在畫面之間用 nav argument 傳遞整個陣型或戰鬥結果。

## 建置環境（重要，容易忘記）

- Kotlin **2.3.21**、AGP **9.2.1**、compileSdk **37**（Compose 1.12 起強制要求）、JVM target **21**。
- **AGP 9 起不用再套用 `org.jetbrains.kotlin.android` plugin**，Kotlin 編譯內建在 AGP 裡。`:battle-engine` 這種純 Kotlin/JVM 模組才需要套 `org.jetbrains.kotlin.jvm`。
- `gradle.properties` 裡手動釘死用 **Android Studio 內建的 JBR (JDK 21)** 當 Gradle 的 Java toolchain（`org.gradle.java.home` + `org.gradle.java.installations.paths`），並關掉 toolchain 自動偵測。原因：這台機器上 Gradle 的 toolchain 自動偵測會掃到 VS Code Java 擴充套件內建、缺少 `jlink` 的殘缺 JRE，導致 compileSdk 37 的 jlink transform 失敗。**如果之後在別台機器上建置失敗、錯誤訊息提到 `jlink executable ... does not exist`，去改這兩行路徑，不要浪費時間懷疑是程式碼問題。**
- Hilt 用 2.60.1、KSP 用 2.3.11 — 這兩個在 build 當下有驗證過可以正常 resolve，如果之後 bump Kotlin 版本要記得重新確認相容性（Hilt 的 Gradle plugin 跟 AGP 9 的相容性歷史上出過好幾次問題）。

## 開發慣例

- **戰鬥數值一律 data-driven**：改兵種數值只改 `battle-engine/src/main/resources/game-data/milestone-001.json`，絕對不要把數字寫死在 Kotlin 程式碼裡。
- **戰鬥模擬必須是 deterministic**：同一個 seed + 同一份陣型 = 完全相同的結果與事件序列。這是為了以後 Replay / PvP / Anti-cheat。改 `BattleEngine.kt` 時要注意別不小心引入非決定性的順序（例如用 HashMap 疊代順序取代明確排序）。
- **非顯而易懂的邏輯要加中文註解**（使用者明確要求，方便未來回來看時不用重新推導一次）。不用每行都寫，但公式、非直覺的設計取捨（例如「為什麼命中率要 clamp 在 5%~95%」「為什麼不做移動機制」）要留一句話說明「為什麼」，不是說明「做了什麼」。
- 已知的刻意簡化（不是 bug，是 Milestone 001 範圍內的取捨）列在 [docs/BattleSystem.md](docs/BattleSystem.md) 最後一節，改動前先看那裡有沒有已經記錄過的理由。

## 測試

- `./gradlew :battle-engine:test` — 戰鬥引擎的單元測試，改了 `:battle-engine` 裡任何東西都要先跑這個。
- `./gradlew :app:assembleDebug` — 確認整個 app 能編譯。
- 手動驗證：這台機器上有 emulator AVD `Pixel_10_Pro`，以及一支實機 Pixel 6a（`adb devices` 看到的 `24071JEGR03365`）。裝置上有多台時，adb 指令一定要用 `-s <device-id>` 指定，避免不小心把測試版 APK 裝到不該裝的裝置上。

## Git 工作流程

使用者是這個專案唯一的開發者，已明確要求：**完成一個階段性的小功能、且確認可以建置／測試通過之後，直接 commit（並 push 到 `origin/main`）,不用每次都先問。** 如果建置失敗或測試沒過，不要 commit，先修好。**commit message 一律用中文撰寫**，重點是講清楚「為什麼」這次改動存在，不是條列做了什麼。

### push 權限注意事項

`origin` 指向 `git@github.com:juacie/android_little_war.git`，但這台機器上設定的 SSH 帳號目前是 `mygo-jack`，沒有這個 repo 的寫入權限，push 會被拒絕。commit 本身不受影響（照樣 commit），但 push 會失敗直到帳號權限問題解決（加 collaborator／換 remote／換 SSH key 三選一，要問使用者要選哪個，不要自己猜）。
