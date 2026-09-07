//
//  RepoSearchScreen.swift
//  sample-iOS
//
//  Created by Dinakar Maurya on 2026/09/07.
//

import SwiftUI
import GithubCoreKMP

struct RepoSearchScreen: View {
    @StateObject private var viewModel: RepoSearchViewModel

    init(searchUseCase: SearchRepositoriesUseCase? = nil) {
        _viewModel = StateObject(wrappedValue: RepoSearchViewModel(searchUseCase: searchUseCase))
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 16) {
                // Search Input Bar
                HStack {
                    TextField("Search repositories", text: $viewModel.query)
                        .textFieldStyle(.roundedBorder)
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.never)

                    Button("Search") {
                        Task {
                            await viewModel.search(searchQuery: viewModel.query)
                        }
                    }
                    .buttonStyle(.borderedProminent)
                    .disabled(viewModel.isLoading || viewModel.query.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                }
                .padding(.horizontal)

                // Loading State
                if viewModel.isLoading {
                    Spacer()
                    ProgressView("Searching repositories...")
                    Spacer()
                }
                // Error State
                else if let errorMessage = viewModel.errorMessage {
                    VStack {
                        Spacer()
                        Text(errorMessage)
                            .font(.callout)
                            .foregroundStyle(.red)
                            .padding()
                            .background(Color.red.opacity(0.1))
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                            .padding()
                        Spacer()
                    }
                }
                // Repositories List
                else {
                    List(viewModel.repositories, id: \.id) { repo in
                        RepositoryRow(repo: repo)
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("GitHub Core SDK")
            .task {
                if viewModel.repositories.isEmpty {
                    await viewModel.search(searchQuery: "kotlin")
                }
            }
        }
    }
}

#Preview {
    RepoSearchScreen()
}
