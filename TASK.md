# 📱 Client Applications & Integration Guide (`TASK.md`)

This document outlines how the **three heterogeneous client applications** consume and integrate with **`github-core-kmp`** as their single source of truth for business logic, networking, caching, and APM telemetry.

---

## 🔗 The 3 Client Applications

```text
                               ┌──────────────────────────────────────────────┐
                               │               github-core-kmp                │
                               │       (The Shared Headless SDK Engine)       │
                               └──────────────────────┬───────────────────────┘
                                                      │
              ┌───────────────────────────────────────┼───────────────────────────────────────┐
              │ (Gradle / Maven Local)                │ (Swift Package Manager / XCFramework) │ (Platform Channel / Dart FFI)
              ▼                                       ▼                                       ▼
   ┌────────────────────────────────┐      ┌────────────────────────────────┐      ┌────────────────────────────────┐
   │    🤖 Android Native App       │      │       🍎 iOS Native App        │      │        📱 Flutter App          │
   │      github-cruise-android     │      │     github-repo-search-ios     │      │    flutter_riverpod_template   │
   ├────────────────────────────────┤      ├────────────────────────────────┤      ├────────────────────────────────┤
   │ • Jetpack Compose UI           │      │ • SwiftUI Presentation         │      │ • Flutter Widgets              │
   │ • AndroidX ViewModel           │      │ • Native @MainActor VM         │      │ • Riverpod AsyncNotifier       │
   │ • Kotlin StateFlow             │      │ • Swift async/await            │      │ • Dart Event Loop              │
   │ • Hilt Dependency Injection    │      │ • Apple ARC Memory Management  │      │ • Platform MethodChannel / FFI │
   └────────────────────────────────┘      └────────────────────────────────┘      └────────────────────────────────┘
```

---

## 1. 🤖 Android Native App: [`github-cruise-android`](https://github.com/dinkar1708/github-cruise-android)

### 📦 Distribution & Dependency
* **Artifact:** Android AAR published to `mavenLocal()` or internal Maven repository.
* **Gradle Dependency:**
  ```kotlin
  dependencies {
      implementation("com.github.core:github-core:1.0.0")
      // or composite build / project reference
  }
  ```

### 🛠️ Architecture & Consumer Pattern
The Android team injects the KMP Use Case directly into an AndroidX `ViewModel` via Hilt. Presentation state is modeled natively with Kotlin `StateFlow` and consumed by Jetpack Compose.

```kotlin
package com.github.cruise.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.core.domain.model.Repository
import com.github.core.domain.usecase.SearchRepositoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val repositories: List<Repository>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchUseCase: SearchRepositoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun search(query: String) {
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                val result = searchUseCase.execute(query = query, page = 1)
                _uiState.value = SearchUiState.Success(result.items)
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
```

---

## 2. 🍎 iOS Native App: [`github-repo-search-ios`](https://github.com/dinkar1708/github-repo-search-ios)

### 📦 Distribution & Dependency
* **Artifact:** `GithubCoreKMP.xcframework` distributed via Swift Package Manager (SPM) or CocoaPods.
* **Xcode Setup:** Add `GithubCoreKMP` Swift package dependency or link `GithubCoreKMP.xcframework`.

### 🛠️ Architecture & Consumer Pattern
The iOS team consumes the pure KMP Use Case within a native Swift `@MainActor` `ObservableObject` (or `@Observable` in iOS 17+), utilizing Swift `async/await` and standard Apple Automatic Reference Counting (ARC) memory management.

```swift
import SwiftUI
import GithubCoreKMP // Shared KMP Engine

@MainActor
class SearchViewModel: ObservableObject {
    @Published var repositories: [Repository] = []
    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil

    // Injected pure KMP Use Case
    private let searchUseCase: SearchRepositoriesUseCase

    init(searchUseCase: SearchRepositoriesUseCase = SearchRepositoriesUseCase()) {
        self.searchUseCase = searchUseCase
    }

    func search(query: String) async {
        guard !query.trimmingCharacters(in: .whitespaces).isEmpty else { return }
        
        self.isLoading = true
        self.errorMessage = nil

        do {
            // Swift async/await seamlessly executes KMP suspend functions
            let result = try await searchUseCase.execute(query: query, page: 1)
            self.repositories = result.items
        } catch {
            self.errorMessage = error.localizedDescription
        }
        
        self.isLoading = false
    }
}
```

---

## 3. 📱 Flutter App: [`flutter_riverpod_template`](https://github.com/dinkar1708/flutter_riverpod_template)

### 📦 Distribution & Dependency
* **Artifact:** Platform Bridge (MethodChannel / Pigeon / Dart FFI) wrapping the compiled Android AAR and iOS Framework.
* **Dart Package:** `github_core_bridge`.

### 🛠️ Architecture & Consumer Pattern
The Flutter team consumes the KMP engine asynchronously through a native Dart Riverpod `AsyncNotifier`, cleanly decoupling Dart's UI isolate and event loop from Kotlin coroutines.

```dart
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:github_core_bridge/github_core_bridge.dart';
import 'package:github_core_bridge/models/repository.dart';

final searchRepositoryProvider =
    AsyncNotifierProvider<SearchNotifier, List<Repository>>(SearchNotifier.new);

class SearchNotifier extends AsyncNotifier<List<Repository>> {
  @override
  Future<List<Repository>> build() async {
    // Initial state: empty list
    return [];
  }

  Future<void> search(String query) async {
    if (query.trim().isEmpty) return;

    state = const AsyncValue.loading();
    state = await AsyncValue.guard(() async {
      // Calls the KMP engine Use Case through the native platform bridge
      final result = await GithubCoreBridge.searchRepositories(
        query: query,
        page: 1,
      );
      return result.items;
    });
  }
}
```

---

## 📊 Summary: Heterogeneous Integration Comparison

| Dimension | 🤖 Android (`github-cruise-android`) | 🍎 iOS (`github-repo-search-ios`) | 📱 Flutter (`flutter_riverpod_template`) |
| :--- | :--- | :--- | :--- |
| **Packaging** | Gradle AAR / Maven | SPM / `GithubCoreKMP.xcframework` | Dart Platform Channel / FFI Plugin |
| **State Layer** | AndroidX `ViewModel` + `StateFlow` | Swift `@MainActor` `ObservableObject` | Riverpod `AsyncNotifier` |
| **UI Framework** | Jetpack Compose | SwiftUI | Flutter Material 3 |
| **Concurrency** | Kotlin Coroutines (`viewModelScope`) | Swift `Task` & `async/await` | Dart `Future` / Event Loop |
| **DI System** | Dagger Hilt | Native Swift Initializer Injection | Riverpod Provider Container |
| **Shared Boundary** | Injects `:core-domain` Use Cases | Calls `:core-domain` Use Cases | Bridges to `:github-core` SDK API |