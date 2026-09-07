//
//  RepositoryRow.swift
//  sample-iOS
//
//  Created by Dinakar Maurya on 2026/09/07.
//

import SwiftUI
import GithubCoreKMP

struct RepositoryRow: View {
    let repo: Repository

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(repo.fullName)
                .font(.headline)
                .fontWeight(.bold)

            if let description = repo.description_, !description.isEmpty {
                Text(description)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .lineLimit(2)
            }

            HStack(spacing: 16) {
                Label("\(repo.stargazersCount)", systemImage: "star.fill")
                    .font(.caption)
                    .foregroundStyle(.orange)

                if let language = repo.language {
                    Text(language)
                        .font(.caption)
                        .fontWeight(.semibold)
                        .foregroundStyle(.blue)
                }
            }
            .padding(.top, 2)
        }
        .padding(.vertical, 4)
    }
}
