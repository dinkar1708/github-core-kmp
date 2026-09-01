package com.github.core.network.mapper

import com.github.core.domain.model.Owner
import com.github.core.domain.model.Repository
import com.github.core.domain.model.SearchResult
import com.github.core.domain.model.User
import com.github.core.network.dto.OwnerDto
import com.github.core.network.dto.RepositoryDto
import com.github.core.network.dto.SearchRepositoriesResponseDto
import com.github.core.network.dto.UserDto

fun OwnerDto.toDomain(): Owner = Owner(
    id = id,
    login = login,
    avatarUrl = avatarUrl,
    htmlUrl = htmlUrl,
    type = type
)

fun RepositoryDto.toDomain(): Repository = Repository(
    id = id,
    name = name,
    fullName = fullName,
    description = description,
    stargazersCount = stargazersCount,
    forksCount = forksCount,
    language = language,
    htmlUrl = htmlUrl,
    owner = owner.toDomain(),
    openIssuesCount = openIssuesCount,
    defaultBranch = defaultBranch,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun SearchRepositoriesResponseDto.toDomain(): SearchResult<Repository> = SearchResult(
    totalCount = totalCount,
    incompleteResults = incompleteResults,
    items = items.map { it.toDomain() }
)

fun UserDto.toDomain(): User = User(
    id = id,
    login = login,
    name = name,
    avatarUrl = avatarUrl,
    bio = bio,
    publicRepos = publicRepos,
    followers = followers,
    following = following,
    htmlUrl = htmlUrl,
    location = location,
    company = company
)
