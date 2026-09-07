# 📱 Sample Applications & KMP Adoption Guides

This directory contains consumer-facing integration guides demonstrating how diverse client applications adopt and consume the **`github-core-kmp`** headless SDK engine according to the official JetBrains Kotlin Multiplatform paradigms.

---

## 🧭 Official JetBrains KMP Adoption Index

| Official JetBrains Paradigm | What is Shared? | Integration Guide | Executable Sample |
| :--- | :--- | :--- | :--- |
| 🧩 [**1. Share a piece of logic**](https://kotlinlang.org/multiplatform/#choose-share-what-piece-of-logic) | Validation rules & pure domain entities (Zero 3rd-party dependencies) | [**1. Piece of Logic Guide**](./1-guide-share-piece-of-logic.md) | [`sample/sample-share-piece-of-logic`](../../sample/sample-share-piece-of-logic) |
| 🚀 [**2. Share logic but keep UI native**](https://kotlinlang.org/multiplatform/#choose-share-what-logic-native-ui) | Full Headless SDK engine (Network, Cache, Use Cases) + 100% Native UI | • [**2. Android Guide**](./2-guide-share-logic-native-ui-android.md)<br>• [**2. iOS Guide**](./2-guide-share-logic-native-ui-ios.md) | • [`sample/sample-share-logic-native-ui-android`](../../sample/sample-share-logic-native-ui-android)<br>• [`sample/sample-share-logic-native-ui-ios`](../../sample/sample-share-logic-native-ui-ios) |
| 🎨 [**3. Share both logic and UI**](https://kotlinlang.org/multiplatform/#choose-share-what-both-logic-ui) | Shared SDK logic + Shared Compose Multiplatform UI across platforms | [**3. Both Logic & UI Guide**](./3-guide-share-both-logic-and-ui.md) | [`sample/sample-share-both-logic-and-ui`](../../sample/sample-share-both-logic-and-ui) |

---

## 🏛️ Adoption Patterns Overview

### 1. 🧩 [1. Share a piece of logic](./1-guide-share-piece-of-logic.md)
* **Goal:** Zero-risk, incremental adoption.
* **Scope:** Shares isolated pure business logic like `QueryValidator` and pure Kotlin entities.
* **Dependencies:** Zero external runtime dependencies.
* **Target:** Consumed by lightweight apps or existing projects wanting shared validation without network/storage refactoring.

### 2. 🚀 [2. Share logic but keep UI native](./2-guide-share-logic-native-ui-android.md)
* **Goal:** Maximum platform fidelity with 100% native UI performance.
* **Scope:** Full headless SDK engine (Ktor networking, SQLite caching, APM telemetry, Use Cases).
* **Frontends:**
  * **Android:** Native Jetpack Compose UI, Material 3, and Kotlin Coroutines.
  * **iOS:** Native SwiftUI, `@StateObject` / `@Published` ViewModels, and Swift `async/await`.

### 3. 🎨 [3. Share both logic and UI](./3-guide-share-both-logic-and-ui.md)
* **Goal:** Maximum code reuse and fast time-to-market.
* **Scope:** Shares both the headless SDK engine and the `@Composable` UI layer using Compose Multiplatform (CMP) across Android, iOS, and Desktop.
