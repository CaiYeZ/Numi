# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Project Overview

数笔 (Numi) is a personal finance tracking Android app built with Kotlin and Jetpack Compose. It supports income/expense recording, bill statistics with charts, batch operations, dark mode, and JSON data export. All UI text and comments are in Chinese.

## Build & Test Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Run instrumented tests (requires device/emulator)
./gradlew connectedDebugAndroidTest

# Clean and rebuild
./gradlew clean assembleDebug

# Stop daemon (if AAPT2 issues occur)
./gradlew --stop
```

## Architecture

**Single-module app** (`com.herb.numi`) with package separation:

| Package | Role |
|---------|------|
| `data/` | Room entities, DAO, database, repository |
| `ui/` | Compose screens, ViewModel, reusable components |
| `ui/theme/` | Material 3 theme (Color, Type, Theme) |
| `ui/navigation/` | Bottom nav bar (floating pill design) |

**Pattern**: MVVM with `RecordViewModel` extending `AndroidViewModel`. The ViewModel obtains `RecordRepository` by casting `Application` to `NumiApplication`. No DI framework — `NumiApplication` holds lazy singletons for `AppDatabase` and `RecordRepository`.

**Navigation**: `MainActivity` uses `AnimatedContent` with slide transitions to switch between 4 screens (Home, Bills, Record, Settings). The Record screen overlays full-screen (hides the bottom nav bar). `BackHandler` returns from Record to the previous screen.

**Database**: Room with a single `records` table (`Record` entity). `RecordDao` exposes `Flow<List<Record>>` for reactive queries. `@PrimaryKey(autoGenerate = true)` on `id: Long`.

**State**: `RecordViewModel` uses private `MutableStateFlow` backed by public `StateFlow` for all input state (amount, recordType, selectedCategory, note, selectedTime, editingRecordId). `StateFlow` exposes `Flow` from Room DAOs via `stateIn()` with `WhileSubscribed(5000)`.

## Key Dependencies

- **Compose BOM** 2024.09.00 (managed via `gradle/libs.versions.toml`)
- **Kotlin** 2.0.21, **AGP** 9.1.0, **KSP** 2.0.21-1.0.27
- **Room** 2.7.0 with KSP compiler
- **Vico** 1.13.1 for charts (BillsScreen)
- **compileSdk** 36, **minSdk** 24, **targetSdk** 36
- **JDK** 11 source/target compatibility

## Code Conventions

- Records have `type` as `"income"` or `"expense"` (String, not enum)
- Category names are Chinese strings (e.g., "餐饮", "工资")
- `Calendar` is used for time handling throughout the UI layer
- Amount input: max 2 decimal places, max 10 integer digits, leading "0" replaced by typed digit
- `data class` `copy()` is used for immutable updates when editing records
- No proguard/R8 minification in release builds
