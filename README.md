# 小小戰爭 Little War

Android RPG 策略遊戲。核心玩法：兵種收集 × 領袖抽卡 × 陣型策略 × 屬性克制 × 自動戰鬥 × RPG 成長。

目前進度：**Milestone 001 — Playable Battle Prototype**（純本機、無後端）。完整企劃見 [Game_Project_GDD.md](Game_Project_GDD.md)；開發規範與環境注意事項見 [CLAUDE.md](CLAUDE.md)；戰鬥公式見 [docs/BattleSystem.md](docs/BattleSystem.md)；完整 Milestone 路線圖見 [docs/ROADMAP.md](docs/ROADMAP.md)。

## 結構

```text
battle-engine/   — 純 Kotlin 戰鬥模擬核心，無 Android 依賴，決定性模擬（同 seed = 同結果）
app/             — Android 端（Compose + Clean Architecture + MVI + Hilt）
backend/         — 規劃中，尚未動工
docs/            — 技術文件
```

## 建置與執行

```bash
./gradlew :battle-engine:test     # 戰鬥引擎單元測試
./gradlew :app:assembleDebug      # 編譯 debug APK
./gradlew :app:installDebug       # 安裝到已連接的裝置／模擬器
```

建置需要 JDK 21（建議直接用 Android Studio 內建的 JBR）。若在新機器上遇到 `jlink executable ... does not exist` 之類的錯誤，看 [CLAUDE.md](CLAUDE.md) 的「建置環境」一節。

## 開發

用 Android Studio 開啟根目錄即可。`:battle-engine` 是純 Kotlin/JVM 模組，改戰鬥數值只需要編輯 `battle-engine/src/main/resources/game-data/milestone-001.json`，不用改程式碼。
