//
//  RepoSearchViewModel.swift
//  sample-iOS
//
//  Created by Dinakar Maurya on 2026/09/07.
//

import SwiftUI
import Combine
import GithubCoreKMP

@MainActor
class RepoSearchViewModel: ObservableObject {
    @Published var query: String = "kotlin"
    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil
    @Published var repositories: [Repository] = []

    private let searchUseCase: SearchRepositoriesUseCase

    init(searchUseCase: SearchRepositoriesUseCase? = nil) {
        if let useCase = searchUseCase {
            self.searchUseCase = useCase
        } else {
            let sdk = GithubCoreSdk.companion.create()
            self.searchUseCase = SearchRepositoriesUseCase(repository: sdk.networkClient.apiService)
        }
    }

    func search(searchQuery: String) async {
        let trimmed = searchQuery.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }

        isLoading = true
        errorMessage = nil

        do {
            let result = try await searchUseCase.search(query: trimmed)
            self.repositories = result.items as? [Repository] ?? []
            self.isLoading = false
        } catch {
            self.errorMessage = error.localizedDescription
            self.isLoading = false
        }
    }
}
