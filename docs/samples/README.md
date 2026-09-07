# Sample Applications & KMP Adoption Guides

This directory contains consumer-facing integration guides demonstrating how client applications adopt and consume the **`github-core-kmp`** headless SDK engine according to the official JetBrains Kotlin Multiplatform paradigms.

---

## Official JetBrains KMP Adoption Index

| Official JetBrains Paradigm | What is Shared? | Integration Guide | Executable Sample |
| :--- | :--- | :--- | :--- |
| [**1. Share a piece of logic**](https://kotlinlang.org/multiplatform/#choose-share-what-piece-of-logic) | Validation rules & pure domain entities (Zero 3rd-party dependencies) | [**1. Piece of Logic Guide**](./1-guide-share-piece-of-logic.md) | [`sample/sample-share-piece-of-logic`](../../sample/sample-share-piece-of-logic) |
| [**2. Share logic but keep UI native**](https://kotlinlang.org/multiplatform/#choose-share-what-logic-native-ui) | Full Headless SDK engine (Network, Cache, Use Cases) + 100% Native UI | • [**2. Android Guide**](./2-guide-share-logic-native-ui-android.md)<br>• [**2. iOS Guide**](./2-guide-share-logic-native-ui-ios.md) | • [`sample/sample-share-logic-native-ui-android`](../../sample/sample-share-logic-native-ui-android)<br>• [`sample/sample-share-logic-native-ui-ios`](../../sample/sample-share-logic-native-ui-ios) |
| [**3. Share both logic and UI**](https://kotlinlang.org/multiplatform/#choose-share-what-both-logic-ui) | Shared SDK logic + Shared Compose Multiplatform UI across platforms | [**3. Both Logic & UI Guide**](./3-guide-share-both-logic-and-ui.md) | [`sample/sample-share-both-logic-and-ui`](../../sample/sample-share-both-logic-and-ui) |

---

## The 3 Layers: How the Paradigms Differ

To understand which paradigm fits your needs, visualize an application in 3 architectural layers:

```text
┌──────────────────────────────────────────────────────────┐
│ Layer 1: UI / Presentation (Compose, SwiftUI, Views)     │
├──────────────────────────────────────────────────────────┤
│ Layer 2: Network & Cache (Ktor, SQLite, Offline Engine)  │
├──────────────────────────────────────────────────────────┤
│ Layer 3: Pure Business Rules (Validators, Models, Math)  │
└──────────────────────────────────────────────────────────┘
```

| Paradigm | Layer 1: UI | Layer 2: Network & Cache | Layer 3: Pure Logic | Shared % |
| :--- | :---: | :---: | :---: | :---: |
| **1. Share a piece of logic** | Native (Separate) | Native (Separate) | **Shared in KMP** | ~5-10% |
| **2. Share logic, native UI** | Native (Separate) | **Shared in KMP** | **Shared in KMP** | ~70% |
| **3. Share both logic and UI** | **Shared in KMP (CMP)** | **Shared in KMP** | **Shared in KMP** | ~95%+ |

---

## Purpose & When to Choose Each Type

### 1. [1. Share a piece of logic](./1-guide-share-piece-of-logic.md)
* **Purpose:** Share **only isolated algorithms or validation rules** across platforms without touching existing infrastructure.
* **Who does Networking & Storage?** 
  * Android continues using its existing native stack (Retrofit, Room, OkHttp).
  * iOS continues using its existing native stack (URLSession, CoreData, Alamofire).
  * KMP does not touch the internet or database.
* **What is in KMP?** Only [`:core-domain`](../../core-domain) (`QueryValidator`, `PaginationValidator`, immutable models). Zero 3rd-party dependencies.
* **When to choose:**
  * You have large, mature native apps and want zero architectural risk.
  * You have a bug where Android and iOS calculate or validate something differently, and want a single source of truth.

### 2. [2. Share logic but keep UI native](./2-guide-share-logic-native-ui-android.md)
* **Purpose:** Share the **entire headless data and business engine** while preserving 100% native platform UI and animations.
* **Who does Networking & Storage?**
  * KMP handles all of it using Ktor 3.x, SQLDelight SQLite, APM metrics, Circuit Breaker, and Use Cases.
  * Android developers write zero HTTP or database code.
  * iOS developers write zero HTTP or database code.
* **What is Native?**
  * Android: Native Jetpack Compose UI + Material 3 + AndroidX `ViewModel`.
  * iOS: Native SwiftUI + `@StateObject` / `@Published` + Swift concurrency (`async`/`await`).
* **When to choose (Our Core Engine Model):**
  * You are building a **Mobile Platform SDK** (`github-core-kmp`) to empower multiple independent product teams.
  * You need high platform fidelity (native iOS navigation, accessibility, SwiftUI previews) with unified business logic.

### 3. [3. Share both logic and UI](./3-guide-share-both-logic-and-ui.md)
* **Purpose:** Share **virtually everything** (data layer, business logic, AND user interface) from a single unified codebase.
* **Who does UI?**
  * Compose Multiplatform (CMP) renders the exact same Composable UI tree on Android, iOS (via Skiko canvas), and Desktop (JVM).
* **When to choose:**
  * You are building a greenfield application from scratch with limited frontend engineering resources.
  * Rapid time-to-market across mobile and desktop is prioritized over platform-specific UI nuances.
