# 02. Apple iOS SwiftUI Integration Guide

## 🎯 Overview
This guide demonstrates how a native Apple iOS application consumes **`github-core-kmp`** using **SwiftUI**, **Swift Concurrency (`async/await`)**, and **Swift 5.9+ `@Observable`**.

```mermaid
sequenceDiagram
    autonumber
    actor User as iOS User
    participant SwiftUI as SwiftUI Presentation View
    participant VM as SearchViewModel (@Observable)
    participant UC as SearchRepositoriesUseCase (KMP)
    participant Ktor as Ktor Darwin Engine (:core-network)

    User->>SwiftUI: Types query & submits search
    SwiftUI->>VM: await vm.search(query:)
    VM->>VM: isLoading = true; errorMessage = nil
    VM->>UC: try await searchUseCase.execute(query:page:)
    UC->>Ktor: Dispatch NSURLSession Request
    Ktor-->>UC: Return SearchResult<Repository>
    UC-->>VM: Return items
    VM->>VM: repositories = items; isLoading = false
    VM-->>SwiftUI: Swift Observation triggers body re-render
    SwiftUI-->>User: Renders Native SwiftUI List
```

---

## 1. Swift Package Manager (SPM) Setup

Add the local or remote package dependency to `Package.swift`:

```swift
// Package.swift
dependencies: [
    .package(url: "https://github.com/dinkar1708/github-core-kmp", from: "1.0.0")
],
targets: [
    .target(
        name: "iOSApp",
        dependencies: ["GithubCoreKMP"]
    )
]
```

Or link `GithubCoreKMP.xcframework` directly in **Xcode ➔ Target ➔ General ➔ Frameworks, Libraries, and Embedded Content**.

---

## 2. Native Swift Presentation Model (`@Observable`)

```swift
import SwiftUI
import GithubCoreKMP // The shared KMP SDK

@MainActor
@Observable
final class SearchViewModel {
    var repositories: [Repository] = []
    var isLoading: Bool = false
    var errorMessage: String? = nil

    // Injected KMP Use Case
    private let searchUseCase: SearchRepositoriesUseCase

    init(sdk: GithubCoreSdk = GithubCoreSdk.companion.create()) {
        self.searchUseCase = SearchRepositoriesUseCase(repository: sdk.networkClient.apiService)
    }

    func search(query: String) async {
        guard !query.trimmingCharacters(in: .whitespaces).isEmpty else { return }

        self.isLoading = true
        self.errorMessage = nil

        do {
            // Swift async/await seamlessly executes Kotlin suspend functions
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

## 3. Native SwiftUI View Binding

```swift
import SwiftUI

struct SearchScreen: View {
    @State private var viewModel = SearchViewModel()
    @State private var searchText = ""

    var body: some View {
        NavigationStack {
            List {
                if viewModel.isLoading {
                    ProgressView("Searching repositories...")
                } else if let error = viewModel.errorMessage {
                    Text(error).foregroundColor(.red)
                } else {
                    ForEach(viewModel.repositories, id: \.id) { repo in
                        VStack(alignment: .leading) {
                            Text(repo.fullName).font(.headline)
                            Text(repo.repoDescription ?? "").font(.subheadline).foregroundColor(.secondary)
                            Text("⭐ \(repo.stargazersCount)").font(.caption)
                        }
                    }
                }
            }
            .navigationTitle("GitHub Search")
            .searchable(text: $searchText)
            .onSubmit(of: .search) {
                Task {
                    await viewModel.search(query: searchText)
                }
            }
        }
    }
}
```
